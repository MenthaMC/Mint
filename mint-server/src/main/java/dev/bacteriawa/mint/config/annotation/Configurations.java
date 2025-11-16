package dev.bacteriawa.mint.config.annotation;

import dev.bacteriawa.mint.config.ConfigurationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Configurations {
    String name();

    ConfigurationType type();

    boolean deprecated() default false;

    String[] comments() default {};
}
