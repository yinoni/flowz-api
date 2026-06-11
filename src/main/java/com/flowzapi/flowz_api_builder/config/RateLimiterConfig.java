package com.flowzapi.flowz_api_builder.config;

import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.SslOptions;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import io.lettuce.core.resource.DnsResolver;
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

    /**
     * רכיב ה-ClientResources שמגדיר מנגנון DNS חסין.
     * מונע מ-Netty לנסות לבצע פתרון הפוך (Reverse Lookup) שמייצר את ה- <unresolved>.
     */
    @Bean(destroyMethod = "shutdown")
    public ClientResources clientResources() {
        return DefaultClientResources.builder()
                .dnsResolver(new DnsResolver() {
                    @Override
                    public InetAddress[] resolve(String host) throws UnknownHostException {
                        // אם זה השרת המרוחק שלנו, נפתור אותו ידנית ל-IP קשיח וסגור
                        if (redisHost.equals(host) && !"localhost".equals(host) && !"127.0.0.1".equals(host)) {
                            InetAddress rawAddress = InetAddress.getByName(host);
                            // יצירת אובייקט InetAddress שמכיל רק את הבייטים של ה-IP - חסין מ-Reverse DNS
                            InetAddress secureAddress = InetAddress.getByAddress(host, rawAddress.getAddress());
                            System.out.println("[Redis DNS] Securely resolved " + host + " to " + secureAddress.getHostAddress());
                            return new InetAddress[]{ secureAddress };
                        }
                        // לכל מקרה אחר (כמו localhost), נשתמש ברזולבר הסטנדרטי של ה-JVM
                        return InetAddress.getAllByName(host);
                    }
                })
                .build();
    }

    @Bean
    public LettuceConnectionFactory redisConnectionFactory(ClientResources clientResources) {
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName(redisHost); // משתמשים ב-Host המקורי, ה-Resolver כבר יטפל בו
        redisConfig.setPort(redisPort);
        redisConfig.setPassword(redisPassword);

        LettuceClientConfiguration.LettuceClientConfigurationBuilder clientConfigBuilder =
                LettuceClientConfiguration.builder()
                        .clientResources(clientResources); // הזרקת ה-Resources המוגנים

        if (sslEnabled) {
            clientConfigBuilder.useSsl();
            System.out.println("[Redis Factory] SSL enabled.");
        }

        return new LettuceConnectionFactory(redisConfig, clientConfigBuilder.build());
    }

    @Bean
    public RedisClient redisClient(ClientResources clientResources) {
        RedisURI redisUri = RedisURI.builder()
                .withHost(redisHost)
                .withPort(redisPort)
                .withAuthentication("default", redisPassword)
                .withTimeout(Duration.ofSeconds(10))
                .build();

        if (sslEnabled) {
            redisUri.setSsl(true);
            redisUri.setVerifyPeer(false); // עוקף בעיות אימות תעודות בענן
        }

        // יצירת הלקוח עם ה-ClientResources המכילים את ה-DnsResolver המותאם
        RedisClient client = RedisClient.create(clientResources, redisUri);

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