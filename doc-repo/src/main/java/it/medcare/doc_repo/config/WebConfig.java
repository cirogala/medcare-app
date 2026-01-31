package it.medcare.doc_repo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final MedcareSecurityInterceptor securityInterceptor;

    public WebConfig(MedcareSecurityInterceptor securityInterceptor) {
    	
        this.securityInterceptor = securityInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
    	
        registry.addInterceptor(securityInterceptor)
            .excludePathPatterns(
                "/v3/api-docs/**",
                "/swagger-ui.html",
                "/swagger-ui/**"
            );
    }
}
