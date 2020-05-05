package de.legendlime.EmployeeService.config.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;


@Configuration
public class RedisCacheConfig {
	
	@Autowired
	RedisProperties redisProperties;
	
	@Bean
	@ConditionalOnProperty(prefix = "legendlime.redis", name = "enabled", havingValue = "true")
	public JedisConnectionFactory jedisConnectionFactory() {
		RedisClusterConfiguration redisCluster = new RedisClusterConfiguration();
		// for production environments use RedisClusterConfiguration instead of RedisStandaloneConfiguration
		if (redisProperties.getRedisServer1() != null ) {
			RedisNode node1 = new RedisNode(redisProperties.getRedisServer1(), redisProperties.getRedisPort1());
			redisCluster.addClusterNode(node1);
		}
		if (redisProperties.getRedisServer2() != null) {
			RedisNode node2 = new RedisNode(redisProperties.getRedisServer2(), redisProperties.getRedisPort2());
			redisCluster.addClusterNode(node2);
		}
		if (redisProperties.getRedisServer3() != null) {
			RedisNode node3 = new RedisNode(redisProperties.getRedisServer3(), redisProperties.getRedisPort3());
			redisCluster.addClusterNode(node3);
		}
		JedisConnectionFactory jedisConnFactory = new JedisConnectionFactory(redisCluster);
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
