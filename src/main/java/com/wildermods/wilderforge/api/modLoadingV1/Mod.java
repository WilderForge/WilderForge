package com.wildermods.wilderforge.api.modLoadingV1;

import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import com.wildermods.wilderforge.api.mixins.v1.Require;

/**
 * Defines a mod identity and provides metadata for use across WilderForge systems.
 *
 * <p>
 * The {@code @Mod} annotation serves two distinct roles within WilderForge:
 * </p>
 *
 * <ul>
 *   <li>
 *     <strong>Mod Declaration:</strong><br>
 *     When applied directly to a class, {@code @Mod} identifies that class as belonging
 *     to a specific mod. During startup, WilderForge automatically detects and registers
 *     these annotated classes on the main event bus:
 *     <pre>{@code
 *     MAIN_BUS.register(annotatedClazz);
 *     }</pre>
 *     This allows mod event handlers to be declared purely through annotation, without
 *     needing explicit registration code.
 *   </li>
 *	<p/>
 *   <li>
 *     <strong>Conditional Mixin Requirement:</strong><br>
 *     When used inside a {@link Require @Require} annotation, {@code @Mod} represents 
 *     a dependency or condition that determines whether a mixin is applied at runtime.
 *     <ul>
 *       <li>If the specified coremod is <em>present</em> and matches the declared version or
 *       version range, the mixin will be applied.</li>
 *       <li>If the specified coremod does not satisfy the version condition, the mixin 
 *       will <em>not</em> be applied.</li>
 *       <li>Special values such as {@code Require.ANY} and {@code Require.ABSENT}
 *       can be used to instead match any version or the absence of the mod,
 *       respectively.</li>
 *     </ul>
 *     This allows mixins to load only when compatible mods are present or to avoid
 *     applying when conflicting mods are detected.
 *   </li>
 * </ul>
 *
 * <p>
 * The annotation is retained at runtime to support reflection-based discovery
 * for both event registration and conditional mixin logic.
 * </p>
 *
 * @see Require
 */
@Target(TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Mod {
	/**
	 * The unique mod identifier. Should match the mod id
	 * defined in fabric.mod.json or mod.json
	 * @return the mod's id
	 */
	public String modid();
	
	/**
	 * The version or version range associated with the mod.
	 * <p>
	 * When applied to a class, this should be the mod’s current version.
	 * When used within a {@link Require @Require} annotation, this string 
	 * determines when the mixin should load:
	 * </p>
	 * <ul>
	 *   <li>A specific version (e.g. {@code "1.16.1"})</li>
	 *   <li>A version range (e.g. {@code ">=1.16.0 <1.17.0"})</li>
	 *   <li>{@link Require#ANY ANY} to match any version</li>
	 *   <li>{@link Require#ABSENT ABSENT} to match when the mod is not loaded</li>
	 * </ul>
	 *
	 * @see Require
	 *
	 * @return the version string or version range
	 */
	public String version();
}
