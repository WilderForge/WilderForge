package com.wildermods.wilderforge.api.modLoadingV1;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(TYPE)
public @interface Coremod {
	/**
	 * The mod id
	 */
	String value();

}
