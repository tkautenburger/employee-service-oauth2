package de.legendlime.EmployeeService.config.opa;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@Configuration
@ConfigurationProperties(prefix = "legendlime.opa")
public class OPAProperties {
	
	private boolean enabled = false;
	private String opaUrl;
	
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public String getOpaUrl() {
		return opaUrl;
	}
	public void setOpaUrl(String opaUrl) {
		this.opaUrl = opaUrl;
	}
	

}
