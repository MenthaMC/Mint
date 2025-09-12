package dev.bacteriawa.mint.config.annotation;

import dev.bacteriawa.mint.config.ConfigCategory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Config {
    String name();

    ConfigCategory category();

    String[] comments() default {};
}
