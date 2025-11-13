package com.wildermods.wilderforge.api.mixins.v1;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.wildermods.wilderforge.api.modLoadingV1.Mod;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;


/**
 * Declares a mod dependency requirement for a mixin class.
 * <p>
 * This annotation can be applied multiple times to a single mixin class to
 * require multiple mods. The mixin plugin will only load the annotated mixin
 * if <em>all</em> specified coremod requirements are satisfied.
 * </p>
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
@Target(TYPE)
@Retention(RUNTIME)
@Repeatable(Requires.class)
@SuppressWarnings("deprecation")
public @interface Require {

	/**
	 * Speical version string that matches any version of the mod
	 */
	public static final String ANY = "ANY";
	
	/**
	 * Special version string that matches only if the mod is not present
	 */
	public static final String ABSENT = "ABSENT";
	
	public Mod value();
	
}
