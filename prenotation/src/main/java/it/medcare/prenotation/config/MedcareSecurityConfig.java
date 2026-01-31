package it.medcare.prenotation.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import it.medcare.prenotation.enums.RoleType;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface MedcareSecurityConfig {

    boolean enableAnonymous() default false;

    RoleType[] allowedRoles() default {};
}