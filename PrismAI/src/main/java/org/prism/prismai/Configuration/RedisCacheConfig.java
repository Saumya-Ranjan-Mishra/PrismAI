package org.prism.prismai.Configuration;

import java.time.Duration;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

@Component
@EnableCaching
public class RedisCacheConfig {

  @Bean
  public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {

    RedisCacheConfiguration configuration = RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofHours(1))
        .serializeKeysWith(RedisSerializationContext.SerializationPair
            .fromSerializer(new StringRedisSerializer()));

    return RedisCacheManager.builder(connectionFactory)
        .cacheDefaults(configuration)
        .build();
  }
}
