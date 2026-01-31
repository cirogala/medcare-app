package it.medcare.billing.config;

import java.util.Arrays;

import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import io.jsonwebtoken.Claims;
import it.medcare.billing.enums.RoleType;
import it.medcare.billing.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MedcareSecurityInterceptor implements HandlerInterceptor {

    private final JwtService jwtService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        System.out.println("INTERCEPTOR HIT → " + request.getMethod() + " " + request.getRequestURI());

        String path = request.getRequestURI();
        if (path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui")) {
            return true;
        }

        if (!(handler instanceof HandlerMethod hm)) {
            return true;
        }

        MedcareSecurityConfig security = hm.getMethodAnnotation(MedcareSecurityConfig.class);
        if (security == null) {
            security = hm.getBeanType().getAnnotation(MedcareSecurityConfig.class);
        }

        if (security != null && security.enableAnonymous()) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        try {
            String token = authHeader.substring(7);
            Claims claims = jwtService.parseToken(token);

            RoleType role = RoleType.valueOf(claims.get("role", String.class));

            if (security != null && security.allowedRoles().length > 0) {
                boolean authorized = Arrays.asList(security.allowedRoles()).contains(role);
                if (!authorized) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    return false;
                }
            }

            return true;
        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
    }
}
