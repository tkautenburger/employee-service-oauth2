package de.legendlime.EmployeeService.config.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import de.legendlime.EmployeeService.domain.CachedDepartment;


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
    public RedisTemplate<String, CachedDepartment> redisTemplate() {
        RedisTemplate<String, CachedDepartment> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory());
        return template;
	}
}
