package com.flowzapi.flowz_api_builder.config;

import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager; // ודא שזה ה-import המקורי שלך
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.SslOptions;
import io.lettuce.core.api.StatefulRedisConnection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;

@Configuration
public class RateLimiterConfig {

    @Value("${REDIS_HOST:${spring.data.redis.host:localhost}}")
    private String redisHost;

    @Value("${REDIS_PORT:${spring.data.redis.port:6379}}")
    private int redisPort;

    @Value("${REDIS_PASS:${spring.data.redis.password:placeholder}}")
    private String redisPassword;

    @Value("${SPRING_DATA_REDIS_SSL_ENABLED:${spring.data.redis.ssl.enabled:false}}")
    private boolean sslEnabled;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        String resolvedHost = redisHost;
        if (!"localhost".equals(redisHost) && !"127.0.0.1".equals(redisHost)) {
            try {
                resolvedHost = InetAddress.getByName(redisHost).getHostAddress();
                System.out.println("[Redis DNS] Successfully resolved " + redisHost + " to " + resolvedHost);
            } catch (UnknownHostException e) {
                System.err.println("[Redis DNS] Warning: Could not resolve host " + redisHost + ", falling back to raw string.");
            }
        }

        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName(resolvedHost);
        redisConfig.setPort(redisPort);
        redisConfig.setPassword(redisPassword);

        LettuceClientConfiguration.LettuceClientConfigurationBuilder clientConfigBuilder =
                LettuceClientConfiguration.builder();

        if (sslEnabled) {
            clientConfigBuilder.useSsl();
            System.out.println("[Redis Config] SSL is enabled for this connection.");
        }

        return new LettuceConnectionFactory(redisConfig, clientConfigBuilder.build());
    }

    @Bean
    public RedisClient redisClient() {
        String resolvedHost = redisHost;
        if (!"localhost".equals(redisHost) && !"127.0.0.1".equals(redisHost)) {
            try {
                resolvedHost = InetAddress.getByName(redisHost).getHostAddress();
            } catch (UnknownHostException e) {
                System.err.println("[Redis DNS] Could not resolve host " + redisHost);
            }
        }

        // בניית ה-URI עם הפורט והאוטנטיקציה
        RedisURI redisUri = RedisURI.builder()
                .withHost(resolvedHost)
                .withPort(redisPort) // כאן יתקבל 6379
                .withAuthentication("default", redisPassword)
                .withTimeout(Duration.ofSeconds(10))
                .build();

        // אם SSL מופעל (חובה עבור Upstash מחוץ לשרת המקומי)
        if (sslEnabled) {
            redisUri.setSsl(true);
            redisUri.setVerifyPeer(false); // מונע בעיות של אימות תעודות SSL בסביבות ענן כמו Railway
        }

        RedisClient client = RedisClient.create(redisUri);

        // הגדרת אופציות SSL מורחבות עבור הטרנספורט של Netty
        if (sslEnabled) {
            SslOptions sslOptions = SslOptions.builder()
                    .jdkSslProvider()
                    .build();

            ClientOptions clientOptions = ClientOptions.builder()
                    .sslOptions(sslOptions)
                    .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
                    .build();

            client.setOptions(clientOptions);
        }

        return client;
    }

    @Bean
    public ProxyManager<String> proxyManager(RedisClient redisClient) {
        // פתיחת החיבור בצורה ישירה ומפורשת עם הטיפוס המדויק ש-Bucket4j צריך
        StatefulRedisConnection<String, byte[]> nativeConnection =
                redisClient.connect(io.lettuce.core.codec.RedisCodec.of(
                        io.lettuce.core.codec.StringCodec.UTF8,
                        io.lettuce.core.codec.ByteArrayCodec.INSTANCE
                ));

        return LettuceBasedProxyManager.builderFor(nativeConnection)
                .withExpirationStrategy(io.github.bucket4j.distributed.ExpirationAfterWriteStrategy.fixedTimeToLive(Duration.ofSeconds(60)))
                .build();
    }
}