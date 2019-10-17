package de.legendlime.EmployeeService.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.util.concurrent.RateLimiter;

@Configuration
public class RateLimitingConfig {
	
	@Bean
	RateLimiter rateLimiter(RateLimitingProperties prop) {
		return RateLimiter.create(prop.getAverage());
	}

}
