package com.flowzapi.flowz_api_builder.config;

import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.codec.RedisCodec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisPassword;
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
    private int redisPort; // שים לב שהפכנו את זה ל-int לנוחות

    @Value("${REDIS_PASS:${spring.data.redis.password:placeholder}}")
    private String redisPassword;

    @Value("${SPRING_DATA_REDIS_SSL_ENABLED:${spring.data.redis.ssl.enabled:false}}")
    private boolean sslEnabled;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        // טריק ה-DNS Resolve: אם המארח הוא לא localhost, ננסה לפתור את הכתובת שלו ל-IP
        String resolvedHost = redisHost;
        if (!"localhost".equals(redisHost)) {
            try {
                resolvedHost = InetAddress.getByName(redisHost).getHostAddress();
                System.out.println("[Redis DNS] Successfully resolved " + redisHost + " to " + resolvedHost);
            } catch (UnknownHostException e) {
                System.err.println("[Redis DNS] Warning: Could not resolve host " + redisHost + ", falling back to raw string.");
            }
        }

        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName(resolvedHost); // כאן עובר ה-IP הפתור או ה-localhost
        redisConfig.setPort(redisPort);
        redisConfig.setPassword(redisPassword);

        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .useSsl() // Upstash מחייב SSL
                .build();

        return new LettuceConnectionFactory(redisConfig, clientConfig);
    }

    @Bean
    public ProxyManager<String> proxyManager() {

        RedisURI redisUri = RedisURI.builder()
                .withHost(redisHost.trim())
                .withPort(redisPort)
                .withPassword(redisPassword.trim().toCharArray())
                .withSsl(sslEnabled)
                .withTimeout(Duration.ofSeconds(15)) // נותן לענן 15 שניות להתייצב
                .build();

        RedisClient redisClient = RedisClient.create(redisUri);

        ClientOptions clientOptions = ClientOptions.builder()
                .socketOptions(SocketOptions.builder()
                        .connectTimeout(Duration.ofSeconds(15))
                        .build())
                .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
                .build();

        redisClient.setOptions(clientOptions);

        StatefulRedisConnection<String, byte[]> connection = redisClient.connect(
                RedisCodec.of(StringCodec.UTF8, new ByteArrayCodec())
        );

        return LettuceBasedProxyManager.builderFor(connection)
                .withExpirationStrategy(ExpirationAfterWriteStrategy.fixedTimeToLive(Duration.ofMinutes(2)))
                .build();
    }

}