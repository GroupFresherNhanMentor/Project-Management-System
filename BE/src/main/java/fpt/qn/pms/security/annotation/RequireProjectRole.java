package fpt.qn.pms.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import fpt.qn.pms.jooq.enums.ProjectRole;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(RequireProjectRoles.class)
public @interface RequireProjectRole {
    ProjectRole[] value() default {ProjectRole.PM};
}
