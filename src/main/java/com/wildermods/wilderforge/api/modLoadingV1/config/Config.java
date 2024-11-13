package com.wildermods.wilderforge.api.modLoadingV1.config;

import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.wildermods.wilderforge.api.eventV1.bus.EventPriority;
import com.wildermods.wilderforge.api.modLoadingV1.config.ConfigEntry.Nullable;

/**
 * Marks a class as a configuration object for mod settings.
 *
 * <p>This annotation indicates that a class contains configuration entries which can be 
 * read and written to/from a configuration file. All fields within the class that are not 
 * marked as {@code transient} are treated as configuration values, subject to specific rules 
 * on types, required values, and annotations.
 * 
 * A new instance of the configuration class will be constructed by calling the nullary constructor
 * every time the config is changed.
 *
 * <p>Key rules for configuration entries:
 * <ul>
 *     <li>Fields for configuration values must be non-static, primitive types (or boxed primitives), {@link String},
 *     or any Object with a Nullary constructor. All fields defined by the object's class are also considered to
 *     be configuration values and must also follow these rules.
 *     <li>All configuration fields are required by default unless annotated with {@link Nullable}.</li>
 * </ul>
 *
 * <p>If a configuration value is invalid (out of range, incompatible type, missing, etc), 
 * an event is triggered to handle the issue. Events such as {@link BadConfigValueEvent}, 
 * {@link ConfigValueOutOfRangeEvent}, or {@link MissingConfigValueEvent}. These events help notify mods and 
 * allow them to correct the configuration issue.
 * 
 * <p>By default, Wilderforge will attempt to correct bad configuration values at {@link EventPriority#HIGH}
 * if it can. See {@link ConfigEntry#valueCorrectors()} for more information.
 * 
 * <p>Regardless of whether Wilderforge is allowed to correct the bad configuration value, if the value is
 * still bad at {@link EventPriority#HIGHER}, then a ConfigurationError will be thrown unless otherwise specified
 * by the underlying {@link BadConfigValueEvent}
 * 
 * @see ConfigEntry
 * @see ConfigEntry.Range
 * @see ConfigEntry.Nullable
 * @see ConfigEntry.Restart
 * @see ConfigEntry.GUI
 * @see BadConfigValueEvent
 */
@Target(TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Config {
    /**
     * Specifies the mod ID associated with the configuration.
     * This ID is used for event handling and mod-specific configuration corrections.
     *
     * @return the mod ID that owns the configuration.
     */
    public String modid();
}