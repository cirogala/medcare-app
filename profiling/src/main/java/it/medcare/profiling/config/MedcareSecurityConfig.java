package it.medcare.profiling.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import it.medcare.profiling.enums.RoleType;

@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MedcareSecurityConfig {

    boolean enableAnonymous() default false;
    RoleType[] allowedRoles() default {};
}

