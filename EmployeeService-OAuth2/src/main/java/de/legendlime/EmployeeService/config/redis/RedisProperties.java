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
	
	private String redisServer1 = DEFAULT_SERVER;
	private int redisPort1 = DEFAULT_PORT;
	private String redisServer2;
	private int redisPort2 = DEFAULT_PORT;
	private String redisServer3;
	private int redisPort3 = DEFAULT_PORT;
	private String password;
	
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public String getRedisServer1() {
		return redisServer1;
	}
	public void setRedisServer1(String redisServer1) {
		this.redisServer1 = redisServer1;
	}
	public int getRedisPort1() {
		return redisPort1;
	}
	public void setRedisPort1(int redisPort1) {
		this.redisPort1 = redisPort1;
	}
	public String getRedisServer2() {
		return redisServer2;
	}
	public void setRedisServer2(String redisServer2) {
		this.redisServer2 = redisServer2;
	}
	public int getRedisPort2() {
		return redisPort2;
	}
	public void setRedisPort2(int redisPort2) {
		this.redisPort2 = redisPort2;
	}
	public String getRedisServer3() {
		return redisServer3;
	}
	public void setRedisServer3(String redisServer3) {
		this.redisServer3 = redisServer3;
	}
	public int getRedisPort3() {
		return redisPort3;
	}
	public void setRedisPort3(int redisPort3) {
		this.redisPort3 = redisPort3;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}

}
