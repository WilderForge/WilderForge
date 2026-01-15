package com.wildermods.wilderforge.api.mixins.v1;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.wildermods.wilderforge.api.modLoadingV1.Mod;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;


/**
 * Declares a mod dependency requirement for a mixin class or element.
 * <p>
 * This annotation can be applied multiple times to a single element to
 * require multiple mods. The mixin plugin will only load the annotated element
 * if <em>all</em> specified coremod requirements are satisfied.
 * </p>
 * 
 * If a mixin class is annotated and its requirements are not satisfied, then
 * the mixin is not applied, even if elements declared within are unannotated
 * or have their requirements satisfied.
 * 
 * <p>
 * Each {@link Require} refers to a {@link Mod} instance. For mixins, the
 * {@link Mod#version()} value may be:
 * <ul>
 *     <li>a single version string (e.g., "1.16.5")</li>
 *     <li>a version range string parseable by the fabric.mod.json specification (e.g., ">=1.16.0 <1.17.0")</li>
 *     <li>{@link #ANY} — matches any version of the mod</li>
 *     <li>{@link #ABSENT} — matches if the mod is <em>not</em> present</li>
 * </ul>
 * </p>
 * <p>
 * This annotation is repeatable; the container annotation is {@link Requires}.
 * </p>
 *
 * @see Mod
 * @see Requires
 */
@Target({TYPE, FIELD, METHOD})
@Retention(RUNTIME)
@Repeatable(Requires.class)
@SuppressWarnings("deprecation")
public @interface Require {

	/**
	 * Special version string that matches any version of the mod
	 */
	public static final String ANY = "ANY";
	
	/**
	 * Special version string that matches only if the mod is not present
	 */
	public static final String ABSENT = "ABSENT";
	
	public Mod value();
	
	/**
	 * @since 0.7.0.0
	 */
	boolean crash() default false;
	
}
