package it.medcare.doc_repo.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import it.medcare.doc_repo.enums.RoleType;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface MedcareSecurityConfig {

    boolean enableAnonymous() default false;

    RoleType[] allowedRoles() default {};
}
