package de.legendlime.EmployeeService.config.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;


@Configuration
public class RedisCacheConfig {
	
	@Autowired
	RedisProperties redisProperties;
	
	@Bean
	@ConditionalOnProperty(prefix = "legendlime.redis", name = "enabled", havingValue = "true")
	public JedisConnectionFactory jedisConnectionFactory() {
		// build stand alone Redis configuration
		RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration(
				redisProperties.getRedisServer(), redisProperties.getRedisPort());
		redisConfig.setPassword(redisProperties.getPassword());
		// use TLS protocol for Redis connection
		JedisClientConfiguration jedisConfig = JedisClientConfiguration.builder().useSsl().build(); 

		JedisConnectionFactory jedisConnFactory = new JedisConnectionFactory(redisConfig, jedisConfig);
		return jedisConnFactory;
    }

    @Bean
	@ConditionalOnProperty(prefix = "legendlime.redis", name = "enabled", havingValue = "true")
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<String, Object>();
        template.setConnectionFactory(jedisConnectionFactory());
        return template;
	}
}
