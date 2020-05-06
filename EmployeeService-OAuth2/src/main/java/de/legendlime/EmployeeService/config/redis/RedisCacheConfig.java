package de.legendlime.EmployeeService.config.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;


@Configuration
public class RedisCacheConfig {
	
	@Autowired
	RedisProperties redisProperties;
	
	@Bean
	@ConditionalOnProperty(prefix = "legendlime.redis", name = "enabled", havingValue = "true")
	public JedisConnectionFactory jedisConnectionFactory() {
		RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration(
				redisProperties.getRedisServer(), redisProperties.getRedisPort());
		redisConfig.setPassword(redisProperties.getPassword());
		JedisConnectionFactory jedisConnFactory = new JedisConnectionFactory(redisConfig);
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
