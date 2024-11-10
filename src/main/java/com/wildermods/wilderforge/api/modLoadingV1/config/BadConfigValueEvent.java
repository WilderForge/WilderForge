package com.wildermods.wilderforge.api.modLoadingV1.config;

import java.lang.reflect.Field;

import org.jetbrains.annotations.Nullable;

import com.wildermods.wilderforge.api.eventV1.Event;
import com.wildermods.wilderforge.api.modLoadingV1.config.ConfigEntry.Range;
import com.wildermods.wilderforge.launch.exception.ConfigurationError;

/**
 * Represents an event triggered when there is an issue with a configuration value.
 * This can occur when a configuration value is out of range, incompatible, missing,
 * or otherwise invalid.
 * 
 * If the value is not corrected, then a {@link ConfigurationError} will occur unless
 * otherwise specified.
 */
public class BadConfigValueEvent extends Event {

	private final Config configAnnotation;

	private final Object configuration;


	protected Field fieldToSet;


	protected Object valueToInsert;

	/**
	 * Constructs a new {@code BadConfigValueEvent}.
	 *
	 * @param configAnnotation The configuration annotation associated with this event.
	 * @param configuration	The configuration object where the field exists.
	 * @param fieldToSet	   The field that was attempted to be set, which may be null if a value was found in the configuration file
	 * but there is no corresponding field in the configuration object.
	 * @param valueToInsert	The value that was attempted to be set in the field, which may be null.
	 */
	public BadConfigValueEvent(Config configAnnotation, Object configuration, Field fieldToSet, @Nullable Object valueToInsert) {
		super(false);
		this.configAnnotation = configAnnotation;
		this.configuration = configuration;
		this.fieldToSet = fieldToSet;
		this.valueToInsert = valueToInsert;
	}
	
	/**
	 * @return The configuration annotation associated with this event.
	 */
	public final Config getConfigAnnotation() {
		return configAnnotation;
	}
	
	/**
	 * @return The configuration object containing the field where the bad value was attempted to be set.
	 */
	public final Object getConfigObject() {
		return configuration;
	}
	
	/**
	 * @return The field in the configuration object that was targeted for setting the value.
	 * This may be null if a corresponding field in the configuration object cannot be found
	 */
	public final @Nullable Field getFieldToSet() {
		return fieldToSet;
	}
	
	/**
	 * @return The value that was attempted to be inserted into the field.
	 * This may be null in the case of a missing configuration value.
	 */
	public final @Nullable Object getValue() {
		return valueToInsert;
	}
	
	/**
	 * Sets the field in the configuration object that corresponds to this configuration value.
	 * If the field is found, it is made accessible.
	 *
	 * @param field The field to associate with this configuration value.
	 */
	protected void setField(Field field) {
		this.fieldToSet = field;
		field.setAccessible(true);
	}
	
	/**
	 * @param valueToInsert the corrected value to insert. Overwrites the bad value.
	 */
	protected void setValue(Object valueToInsert) {
		this.valueToInsert = valueToInsert;
	}

	/**
	 * Represents an event triggered when a configuration value is outside of the defined range.
	 * 
	 * This problem is corrected by Wilderforge by default by clamping the value to the range's
	 * min or max value, whichever is closest.
	 * 
	 * If you do not want this behavior, modify {@link ConfigEntry#valueCorrectors()} to not
	 * have the mod ID "wilderforge".
	 */
	public static class ConfigValueOutOfRangeEvent extends BadConfigValueEvent {

		/**
		 * The valid range that the configuration value must fall within.
		 */
		private final Range range;

		/**
		 * Constructs a new {@code ConfigValueOutOfRangeEvent}.
		 *
		 * @param configAnnotation The configuration annotation associated with this event.
		 * @param configuration	The configuration object where the field exists.
		 * @param fieldToSet	   The field that was attempted to be set.
		 * @param valueToInsert	The out-of-range value attempted to be set.
		 * @param range			The acceptable range for the configuration value.
		 */
		public ConfigValueOutOfRangeEvent(Config configAnnotation, Object configuration, Field fieldToSet, Number valueToInsert, Range range) {
			super(configAnnotation, configuration, fieldToSet, valueToInsert);
			this.range = range;
		}
		
		
		public Range getRange() {
			return range;
		}
		
		@Override
		public void setValue(Object valueToInsert) {
			super.setValue(valueToInsert);
		}
	}

	/**
	 * Represents an event triggered when a configuration value is of an incompatible type.
	 */
	public static class ConfigValueOfIncompatibleTypeEvent extends BadConfigValueEvent {

		/**
		 * Constructs a new {@code ConfigValueOfIncompatibleTypeEvent}.
		 *
		 * @param configAnnotation The configuration annotation associated with this event.
		 * @param configuration	The configuration object where the field exists.
		 * @param fieldToSet	   The field that was attempted to be set.
		 * @param valueToInsert	The value with an incompatible type attempted to be set.
		 */
		public ConfigValueOfIncompatibleTypeEvent(Config configAnnotation, Object configuration, Field fieldToSet, Object valueToInsert) {
			super(configAnnotation, configuration, fieldToSet, valueToInsert);
		}
		
		@Override
		public void setValue(Object valueToInsert) {
			super.setValue(valueToInsert);
		}
	}

	/**
	 * Represents an event triggered when a required configuration value is missing.
	 */
	public static class MissingConfigValueEvent extends BadConfigValueEvent {

		/**
		 * Constructs a new {@code MissingConfigValueEvent}.
		 *
		 * @param configAnnotation The configuration annotation associated with this event.
		 * @param configuration	The configuration object where the field exists.
		 * @param fieldToSet	   The field that was missing a required value.
		 */
		public MissingConfigValueEvent(Config configAnnotation, Object configuration, Field fieldToSet) {
			super(configAnnotation, configuration, fieldToSet, null);
		}
		
		@Override
		public void setValue(Object valueToInsert) {
			super.setValue(valueToInsert);
		}
	}
	
	/**
	 * Represents an event triggered when a configuration value is found in the configuration
	 * file, but there is no corresponding field in the configuration object.
	 * 
	 * If this problem is not corrected, a warning message is logged instead of throwing a ConfigurationError.
	 */
	public static class MissingConfigFieldEvent extends BadConfigValueEvent {

		/**
		 * The name of the configuration value that does not correspond to a field in the configuration object.
		 */
		private final String valueName;

		/**
		 * Constructs a new {@code MissingConfigFieldEvent}.
		 *
		 * @param configAnnotation The configuration annotation associated with this event.
		 * @param configuration	The configuration object that is missing the field.
		 * @param valueName		The name of the value in the configuration file.
		 * @param valueToInsert	The value to be inserted into the configuration.
		 */
		public MissingConfigFieldEvent(Config configAnnotation, Object configuration, String valueName, Object valueToInsert) {
			super(configAnnotation, configuration, null, valueToInsert);
			this.valueName = valueName;
		}

		/**
		 * Returns the name of the configuration value that does not correspond to a field in the configuration object.
		 *
		 * @return The name of the configuration value.
		 */
		public String getValueName() {
			return valueName;
		}
		
		@Override
		public void setField(Field field) {
			super.setField(field);
		}
		
		@Override
		public void setValue(Object valueToInsert) {
			super.setValue(valueToInsert);
		}
	}
}
