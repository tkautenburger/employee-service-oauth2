package de.legendlime.EmployeeService.config.redis;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@Configuration
@ConfigurationProperties(prefix = "legendlime.redis")
public class RedisProperties {
	
	private static final int DEFAULT_PORT = 6379;
	private static final String DEFAULT_SERVER = "localhost"; 
	
	private boolean enabled = false;
	
	private String redisServer = DEFAULT_SERVER;
	private int redisPort = DEFAULT_PORT;
	private String password;
	
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public String getRedisServer() {
		return redisServer;
	}
	public void setRedisServer(String redisServer) {
		this.redisServer = redisServer;
	}
	public int getRedisPort() {
		return redisPort;
	}
	public void setRedisPort(int redisPort) {
		this.redisPort = redisPort;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}

}
