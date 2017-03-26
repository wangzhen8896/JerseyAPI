package com.wzsuper.JerseyAPI.Server.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.wzsuper.JerseyAPI.Beans.ServiceInfo;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Register {
	
	String name() default "";
	
	String version() default ServiceInfo.defaultVersion;
	
	boolean stateless() default true;
}
