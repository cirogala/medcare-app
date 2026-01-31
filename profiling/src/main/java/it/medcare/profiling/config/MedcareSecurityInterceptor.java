package it.medcare.profiling.config;

import java.util.Arrays;

import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import io.jsonwebtoken.Claims;
import it.medcare.profiling.enums.RoleType;
import it.medcare.profiling.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class MedcareSecurityInterceptor implements HandlerInterceptor {

    private final JwtService jwtService;
    
    public MedcareSecurityInterceptor(JwtService jwtService) {
    	
    	this.jwtService = jwtService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String path = request.getRequestURI();

        if (path.startsWith("/error")) {
            return true;
        }

        if (path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui")) {
            return true;
        }

        if (!(handler instanceof HandlerMethod)) {
        	
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;

        MedcareSecurityConfig security = handlerMethod.getMethodAnnotation(MedcareSecurityConfig.class);

        if (security == null) {
        	
            security = handlerMethod.getBeanType().getAnnotation(MedcareSecurityConfig.class);
        }

        // endpoint pubblico
        if (security != null && security.enableAnonymous()) {
        	
            return true;
        }

        // header auth
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        	
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            
            return false;
        }

        String token = authHeader.substring(7);

        Claims claims;
        
        try {
        	
            claims = jwtService.parseToken(token);
            
        } catch (Exception e) {
        	
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            
            return false;
        }

        // check sui ruoli
        if (security != null && security.allowedRoles().length > 0) {

            RoleType userRole = extractRoleFromClaims(claims);

            boolean authorized = Arrays.stream(security.allowedRoles()).anyMatch(r -> r == userRole);

            if (!authorized) {
            	
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                
                return false;
            }
        }

        return true;
    }

    private RoleType extractRoleFromClaims(Claims claims) {
    	
        return RoleType.valueOf(claims.get("role", String.class));
    }
}
