package com.dair.cais.common.config;

import org.springframework.cache.annotation.Cacheable;
import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Cacheable
public @interface CustomCacheable {
    String[] cacheNames() default {};
    String key() default "";
}