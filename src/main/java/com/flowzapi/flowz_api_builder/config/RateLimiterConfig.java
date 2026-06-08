package com.flowzapi.flowz_api_builder.config;


import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.codec.RedisCodec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RateLimiterConfig {

    @Value("${spring.data.redis.host:localhost}") // אם אין הוסט, ישים localhost
    private String redisHost;

    @Value("${spring.data.redis.port:6379}") // אם אין פורט, ישים 6379
    private int redisPort;

    @Value("${spring.data.redis.password:placeholder_password}") // סיסמת דמי לטסטים
    private String redisPassword;

    @Bean
    public ProxyManager<String> proxyManager() {
        // 1. בניית RedisURI מפורש עם הגדרת SSL (SslEnabled) קשיחה עבור Upstash
        RedisURI redisUri = RedisURI.builder()
                .withHost(redisHost)
                .withPort(redisPort)
                .withPassword(redisPassword.toCharArray())
                .withSsl(true) // מכריח שימוש בחיבור מוצפן ומאובטח החוצה לענן
                .build();

        // 2. יצירת ה-RedisClient בצורה עצמאית כדי לעקוף בעיות DNS של Docker
        RedisClient redisClient = RedisClient.create(redisUri);

        // 3. פתיחת חיבור שבו המפתח (Key) הוא String, והערך (Value) הוא byte[] (הקוד המקורי שלך)
        StatefulRedisConnection<String, byte[]> connection = redisClient.connect(
                RedisCodec.of(StringCodec.UTF8, new ByteArrayCodec())
        );

        // 4. בניית ה-ProxyManager עם החיבור המשולב
        return LettuceBasedProxyManager.builderFor(connection)
                .withExpirationStrategy(ExpirationAfterWriteStrategy.fixedTimeToLive(Duration.ofMinutes(2)))
                .build();
    }
}
