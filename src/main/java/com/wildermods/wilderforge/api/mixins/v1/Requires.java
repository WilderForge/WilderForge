package com.wildermods.wilderforge.api.mixins.v1;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Container annotation for {@link Require}.
 * <p>
 * This is used implicitly by Java to support repeatable {@link Require} annotations
 * on a mixin class. You generally do not need to use this annotation directly.
 * </p>
 * 
 * @deprecated You generally do not need to use this annotation directly. You probably
 * want {@link Require} instead.
 *
 * @see Require
 */
@Target({TYPE, FIELD, METHOD})
@Retention(RUNTIME)
@Deprecated(forRemoval = false)
public @interface Requires {
	Require[] value();
}
