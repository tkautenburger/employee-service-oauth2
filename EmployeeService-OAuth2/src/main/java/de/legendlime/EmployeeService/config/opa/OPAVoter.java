package de.legendlime.EmployeeService.config.opa;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;

import de.legendlime.EmployeeService.config.client.RestTemplateBean;

public class OPAVoter implements AccessDecisionVoter<Object> {

    private RestTemplateBean restTemplateBean;	
    private String opaUrl;

    public OPAVoter(String opaUrl, RestTemplateBean restTemplateBean) {
        this.opaUrl = opaUrl;
        this.restTemplateBean = restTemplateBean;
    }

    @Override
    public boolean supports(ConfigAttribute attribute) {
        return true;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return true;
    }

   @Override
    public int vote(Authentication authentication, Object obj, Collection<ConfigAttribute> attrs) {

        if (!(obj instanceof FilterInvocation)) {
            return ACCESS_ABSTAIN;
        }

        FilterInvocation filter = (FilterInvocation) obj;
        Map<String, String> headers = new HashMap<String, String>();

        for (Enumeration<String> headerNames = filter.getRequest().getHeaderNames(); headerNames.hasMoreElements();) {
            String header = headerNames.nextElement();
            headers.put(header, filter.getRequest().getHeader(header));
        }

        String[] path = filter.getRequest().getRequestURI().replaceAll("^/|/$", "").split("/");

        Map<String, Object> input = new HashMap<String, Object>();
        input.put("auth", authentication);
        input.put("method", filter.getRequest().getMethod());
        input.put("path", path);
        input.put("headers", headers);

        HttpEntity<?> request = new HttpEntity<>(new OPADataRequest(input));
        OPADataResponse response = restTemplateBean.getRestTemplate()
        		.postForObject(this.opaUrl, request, OPADataResponse.class);

        if (!response.getResult()) {
            return ACCESS_DENIED;
        }

        return ACCESS_GRANTED;
    }

}
