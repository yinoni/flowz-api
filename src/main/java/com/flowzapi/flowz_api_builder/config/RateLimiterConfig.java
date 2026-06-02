package com.flowzapi.flowz_api_builder.config;


import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.codec.RedisCodec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import java.time.Duration;

@Configuration
public class RateLimiterConfig {

    @Bean
    public ProxyManager<String> proxyManager(LettuceConnectionFactory lettuceConnectionFactory) {
        // 1. שליפת ה-RedisClient המקורי מתוך ה-Connection Factory של ספרינג
        RedisClient redisClient = (RedisClient) lettuceConnectionFactory.getNativeClient();

        // 2. 🔥 התיקון: פתיחת חיבור שבו המפתח (Key) הוא String, והערך (Value) הוא byte[]
        StatefulRedisConnection<String, byte[]> connection = redisClient.connect(
                RedisCodec.of(StringCodec.UTF8, new ByteArrayCodec())
        );

        // 3. בניית ה-ProxyManager עם החיבור המשולב - עכשיו הוא יחזיר בדיוק ProxyManager<String>
        return LettuceBasedProxyManager.builderFor(connection)
                .withExpirationStrategy(ExpirationAfterWriteStrategy.fixedTimeToLive(Duration.ofMinutes(2)))
                .build();
    }
}