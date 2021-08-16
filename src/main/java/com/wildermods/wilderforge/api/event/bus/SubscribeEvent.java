package com.wildermods.wilderforge.api.event.bus;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(METHOD)
public @interface SubscribeEvent {
	boolean acceptCancelled() default false;
	int priority() default 0;
}
