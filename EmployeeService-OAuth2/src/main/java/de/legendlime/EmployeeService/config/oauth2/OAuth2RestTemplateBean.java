package de.legendlime.EmployeeService.config.oauth2;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Conditional;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.stereotype.Component;

import io.opentracing.Tracer;
import io.opentracing.contrib.spring.web.client.TracingRestTemplateInterceptor;

import de.legendlime.EmployeeService.config.ClientHttpRequestFactoryBean;
import de.legendlime.EmployeeService.config.oauth2.OAuth2RestTemplateBean.ServiceAccountEnabled;

@Component
@Conditional(value = { ServiceAccountEnabled.class })
public class OAuth2RestTemplateBean {

	private static final Logger LOG = LoggerFactory.getLogger(OAuth2RestTemplateBean.class);

	private Tracer jaegerTracer;
	
	private OAuth2RestTemplate oAuth2RestTemplate;

	public OAuth2RestTemplateBean(Tracer tracer, ClientHttpRequestFactoryBean factoryBean, OAuth2ProtectedResourceDetails details) {
		
		this.jaegerTracer = tracer;
	    this.oAuth2RestTemplate = new OAuth2RestTemplate(details);

	    LOG.info("Set Client Request Factory for OAuth2RestTemplate");
	    oAuth2RestTemplate.setRequestFactory(factoryBean.getFactory());
	    oAuth2RestTemplate.setInterceptors(Collections.singletonList(new TracingRestTemplateInterceptor(this.jaegerTracer)));

	    oAuth2RestTemplate.getAccessToken();
	    LOG.debug("Get access token for OAuth2RestTemplate");
	}
	

	public OAuth2RestTemplate getoAuth2RestTemplate() {
		return oAuth2RestTemplate;
	}

	public void renew(ClientHttpRequestFactoryBean factoryBean, OAuth2ProtectedResourceDetails details) {
		oAuth2RestTemplate.setRequestFactory(factoryBean.getFactory());
	    LOG.info("Renew OAuth2RestTemplate RequestFactory");
	}

	/**
	 * Condition class to configure OAuth2RestTemplate when both security is enabled
	 * and client credentials property is set for secured microservice to
	 * microservice call.
	 */
	static class ServiceAccountEnabled extends AllNestedConditions {

		ServiceAccountEnabled() {
			super(ConfigurationPhase.PARSE_CONFIGURATION);
		}

		@ConditionalOnProperty(prefix = "rest.security", value = "enabled", havingValue = "true")
		static class SecurityEnabled {
		}

		@ConditionalOnProperty(prefix = "security.oauth2.client", value = "grant-type", havingValue = "client_credentials")
		static class ClientCredentialConfigurationExists {
		}

	}
}
