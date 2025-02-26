package com.wildermods.wilderforge.api.modLoadingV1.config;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import net.minecraftforge.eventbus.api.EventPriority;
import com.wildermods.wilderforge.api.mixins.v1.Cast;
import com.wildermods.wilderforge.api.modLoadingV1.Mod;
import com.wildermods.wilderforge.api.modLoadingV1.config.BadConfigValueEvent.ConfigValueOutOfRangeEvent;
import com.wildermods.wilderforge.api.modLoadingV1.config.ModConfigurationEntryBuilder.ConfigurationUIContext;
import com.wildermods.wilderforge.api.utils.TypeUtil;
import com.wildermods.wilderforge.launch.exception.ConfigurationError;
import com.wildermods.wilderforge.launch.exception.ConfigurationError.InvalidRangeError;
import com.wildermods.wilderforge.launch.InternalOnly;
import com.wildermods.wilderforge.launch.WilderForge;

/**
 * The {@code ConfigEntry} annotation is used to designate a field in a configuration
 * class as a configuration entry. The other 
 * annotations defined herein allow for customization of how configuration entries are
 * handled, named, and displayed in the GUI, as well as the application of any 
 * constraints on the configuration values.
 * 
 * If the annotated field is incorrectly defined, a {@link ConfigurationError} will be thrown
 * when attempting to process the {@link Config}.
 *
 * <h3>Value Correction</h3>
 * <p>If a configuration value read from the config file is determined to be 
 * invalid, a {@link BadConfigValueEvent} (or a subclass thereof) will be 
 * fired on the {@link WilderForge#MAIN_BUS}. All mods will receive the event, 
 * but only mods that are value correctors for the configuration entry 
 * may modify the value.
 * 
 * <p>WilderForge processes value correction at two priorities, first at {@link EventPriority#HIGH}
 * then at {@link EventPriority#HIGHER}.
 * <p>If the value is invalid when Wilderforge receives the event at {@link EventPriority#HIGH},
 * then it will attempt to correct the value if it is in the {@link valueCorrectors()}.
 * <p>If the value is still invalid when Wilderforge recevies the event at {@link EventPriority#HIGHER},
 * then a {@link ConfigurationError} will occur.
 *
 * <p>Value correctors are specified by the {@code modid} defined in the 
 * enclosing {@link Config} annotation, along with any additional mod IDs 
 * provided in the {@link #valueCorrectors()} method. It is a violation of the 
 * specification for any mod not listed as a value corrector to attempt to 
 * modify the configuration value. This specification may be enforced via
 * relevant language constructs in the future. Enforcement of this specification
 * will not be considered a breaking change.
 *
 * @see Config
 * @see BadConfigValueEvent
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigEntry {
	
    /**
     * Specifies a custom name for the configuration entry. If left blank, 
     * the default name derived from the field name will be used.
     * 
     * <p>If there are multiple configuration entries with the same name in
     * the same {@link Config}, a ConfigurationError will be thrown.
     *
     * @return The custom name for the configuration entry.
     */
	public String name() default "";
	
    /**
     * Specifies additional mod IDs that are allowed to correct this 
     * configuration entry if the value is invalid. 
     * 
     * The mod ID defined in the enclosing {@link Config} annotation is always a
     * valid value corrector, even if not supplied here.
     * 
     * By default, wilderforge is allowed to correct the value. To
     * prevent this behavior, you may supply an empty array. 
     * 
     * If you want to add another value corrector while retaining the default 
     * behavior, you must provide an array containing both "wilderforge" 
     * and the new value corrector's mod ID.
     * 
     * The mod that owns the configuration will be presented to configure the
     * value first, then value correctors are processed in the order supplied
     * here. If you override this value, you should typically keep wilderforge
     * as the last value in the array.
     *
     * @return An array of mod IDs permitted to correct the configuration value.
     */
	public String[] valueCorrectors() default WilderForge.modid;
	
	/**
	 * Determines if the values is considered to be changed if the reference has
	 * changed, or the value has changed.
	 * Defaults to {@code false}
	 * 
	 * @return {@code true} if {@code == }should be used to compare, or {@code false} if
	 * {@code Objects.equals} should be used.
	 */
	public boolean strict() default false;
	
	public static final class ValueCorrectors {
		
		private LinkedHashSet<String> valueCorrectors = new LinkedHashSet<String>();
		
		private ValueCorrectors(ConfigEntry entry) {
			valueCorrectors.addAll(List.of(entry.valueCorrectors()));
		}
		
		public Set<String> getValueCorrectors() {
			return Collections.unmodifiableSet(valueCorrectors);
		}
		
		public boolean contains(String modid) {
			return valueCorrectors.contains(modid);
		}
		
		public boolean contains(Mod mod) {
			return contains(mod.modid());
		}
		
		public boolean contains(Config config) {
			return contains(config.modid());
		}
		
		public static ValueCorrectors of(ConfigEntry entry) {
			return new ValueCorrectors(entry);
		}
		
	}
	
	public static class GUI {
		
		private GUI() {}
		
		/**
		 * Indicates that this configuration entry should be excluded from being displayed
		 * or modified in the in-game graphical user interface (GUI).
		 * 
		 * <p>While the annotated field will not appear in the GUI, it is still treated as
		 * part of the configuration. The value of the field will be read from and written
		 * to the configuration file, and it can be accessed programmatically.
		 * 
		 * <p>Use this annotation for configuration options that should not be exposed or
		 * modified through the in-game settings menu, but that still need to be loaded
		 * from the configuration file.
		 * 
		 * <p><b>Note:</b> If you want a field to be completely excluded from the
		 * configuration system (i.e., not read from or written to the config file), use
		 * the {@code transient} modifier instead.
		 */
		@Target(ElementType.FIELD)
		@Retention(RetentionPolicy.RUNTIME)
		public @interface Excluded {}
		
		/**
		 * An annotation that indicates the annotated field contains configuration entries
		 * that should be grouped together in a collapsible dropdown in the user interface.
		 * 
		 * <p>This annotation is used to organize related configuration fields under a collapsible dropdown,
		 * improving the UI's clarity, especially when there are many configuration options to manage.
		 * 
		 * <p>The {@code expanded} parameter controls whether the dropdown is expanded by default.
		 * If set to {@code true}, the dropdown will be open when first displayed; if set to 
		 * {@code false}, the dropdown will be collapsed by default. The default value is {@code false}.
		 * 
		 * <p>Apply this annotation to a field that contains multiple configuration options, 
		 * allowing them to be grouped into a collapsible section for better organization in the UI.
		 * 
		 * <p><b>Note:</b> The annotated field must not be a primitive type or an array type, 
		 * as the configuration options are derived from the type of the field. The field should 
		 * be a class or object that contains other configuration fields.
		 */
		@Target(ElementType.FIELD)
		@Retention(RetentionPolicy.RUNTIME)
		public @interface Dropdown {
		    /**
		     * Specifies whether the dropdown should be expanded by default when the UI is loaded.
		     * 
		     * @return {@code true} if the dropdown should be expanded, {@code false} if collapsed.
		     */
		    boolean expanded() default false;
		}
		
		@Target(ElementType.FIELD)
		@Retention(RetentionPolicy.RUNTIME)
		public @interface Slider{
			float step();
		}
		
		@Target({ElementType.TYPE, ElementType.FIELD})
		@Retention(RetentionPolicy.RUNTIME)
		public @interface CustomBuilder {
			public static final class DefaultConfigurationBuilder implements Function<ConfigurationUIContext, ModConfigurationEntryBuilder> {
				@Override
				public ModConfigurationEntryBuilder apply(ConfigurationUIContext context) {
					return new ModConfigurationEntryBuilder(context);
				}
			};
			
			Class<? extends Function<ConfigurationUIContext, ? extends ModConfigurationEntryBuilder>> value();
		}
		
		
		/**
		 * An annotation used to provide localized names and tooltips for configuration fields.
		 * This annotation allows fields to be displayed with a user-friendly name and a
		 * corresponding tooltip text, both of which can be localized for different languages.
		 *
		 * <p>The {@code localizedName} provides the display name of the field, while the 
		 * {@code localizedTooltip} provides additional information that will appear as a 
		 * tooltip when the user hovers over the field in the GUI. If no tooltip is provided, 
		 * the {@code localizedTooltip} will default to an empty string.
		 *
		 * <p>Parameters:
		 * <ul>
		 *   <li>{@code localizedName} - The localized display name of the field. This name is 
		 *       shown in the GUI and should be translated based on the user's language.</li>
		 * <p>
		 *   <li>{@code localizedTooltip} - The localized tooltip text that appears when the
		 *       user hovers over the field in the GUI. This is optional and defaults to an
		 *       empty string if not provided. Empty tooltips do not show up if hovered over.</li>
		 * </ul>
		 *
		 * <p>Use this annotation for fields that need to be displayed with names and tooltips 
		 * that can be translated into multiple languages for a more user-friendly experience.
		 */
		@Target(ElementType.FIELD)
		@Retention(RetentionPolicy.RUNTIME)
		public @interface Localized {
		    /**
		     * The translation string to derive the name of the field, shown in the GUI.
		     * 
		     * @return The translation string for the field
		     */
		    String nameLocalizer();

		    /**
		     * The localized tooltip text displayed when the user hovers over the field in the GUI.
		     * This is optional and defaults to an empty string if not provided.
		     * 
		     * @return The translation string for tooltip text, or an empty string if no tooltip is provided.
		     */
		    String tooltipLocalizer() default "";
		}
	}
	
	/**
	 * An annotation used to specify minimum and maximum clamping values for fields
	 * in a configuration. This annotation can be applied to fields of both integral
	 * and floating-point types, with appropriate ranges (integral or decimal) used
	 * based on the type of the annotated field.
	 *
	 * <p><strong>Usage Guidelines:</strong>
	 * <ul>
	 *   <li><strong>Integral Types</strong> (e.g., {@code int}, {@code long}):
	 *     <ul>
	 *       <li>Use {@code min} and {@code max} to define the clamping range.</li>
	 *       <li>If {@code minDecimal} or {@code maxDecimal} are specified, a
	 *           {@link ConfigurationError} will be thrown as they are invalid for integral fields.</li>
	 *     </ul>
	 *   </li>
	 *   <li><strong>Floating-Point Types</strong> (e.g., {@code float}, {@code double}):
	 *     <ul>
	 *       <li>Use {@code minDecimal} and {@code maxDecimal} to define the clamping range.</li>
	 *       <li>If {@code min} or {@code max} are specified, a {@link ConfigurationError} will be thrown as they are invalid for floating-point fields.</li>
	 *     </ul>
	 *   </li>
	 * </ul>
	 *
	 * <p><strong>Special Cases for Minimum and Maximum Values:</strong></p>
	 * <p>If a specified minimum or maximum value falls outside the range of the annotated field's type, a 
	 * {@link ConfigurationError} will be thrown, except in the following cases:
	 * <ul>
	 *   <li>{@link Long#MIN_VALUE} for {@link #min()} and {@link Long#MAX_VALUE} for {@link #max()}.</li>
	 *   <li>{@link Double#MIN_VALUE} for {@link #minDecimal()} and {@link Double#MAX_VALUE} for {@link #maxDecimal()}.</li>
	 * </ul>
	 * <p>In these cases, the boundary value will be interpreted as the minimum or maximum for the field's type, without causing a {@link ConfigurationError}.
	 *
	 * <p><strong>Compatibility Note:</strong></p>
	 * <p>While {@code double} types can represent the entire range of {@code float} values, the {@code minDecimal} and 
	 * {@code maxDecimal} settings apply to both {@code float} and {@code double} fields.
	 *
	 * <h3>Event Handling for Out-of-Range Values</h3>
	 * <p>If a configuration value falls outside the specified range, a {@link ConfigValueOutOfRangeEvent} will be triggered 
	 * on the {@link WilderForge#MAIN_BUS}. This allows the mod or other mods to manage the out-of-range value.
	 *
	 * @see ConfigEntry#valueCorrectors()
	 * @see ConfigurationError
	 * @see ConfigValueOutOfRangeEvent
	 */
	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface Range {
		public long min() default Long.MIN_VALUE;
		public long max() default Long.MAX_VALUE;
		public double minDecimal() default Double.MIN_VALUE;
		public double maxDecimal() default Double.MAX_VALUE;
		
		public static class Ranges {
		
			public static final IntegralRange BYTE = new IntegralRange(Byte.MIN_VALUE, Byte.MAX_VALUE);
			public static final IntegralRange SHORT = new IntegralRange(Short.MIN_VALUE, Short.MAX_VALUE);
			public static final IntegralRange CHAR = new IntegralRange(Character.MIN_VALUE, Character.MAX_VALUE);
			public static final IntegralRange INT = new IntegralRange(Integer.MIN_VALUE, Integer.MAX_VALUE);
			public static final IntegralRange LONG = new IntegralRange(Long.MIN_VALUE, Long.MAX_VALUE);
			public static final DecimalRange FLOAT = new DecimalRange(Float.MIN_VALUE, Float.MAX_VALUE);
			public static final DecimalRange DOUBLE = new DecimalRange(Double.MIN_VALUE, Double.MAX_VALUE);
			
			@InternalOnly 
			public static final DecimalRange SLIDER = new DecimalRange(-1000f, 1000f);
			
			@SuppressWarnings("rawtypes")
			public static RangeInstance getRangeOfType(Class c) {
				if(TypeUtil.isInt(c)) {
					return INT;
				}
				else if(TypeUtil.isLong(c)) {
					return LONG;
				}
				else if(TypeUtil.isFloat(c)) {
					return FLOAT;
				}
				else if(TypeUtil.isDouble(c)) {
					return DOUBLE;
				}
				else if(TypeUtil.isShort(c)) {
					return SHORT;
				}
				else if(TypeUtil.isByte(c)) {
					return BYTE;
				}
				else if(TypeUtil.isChar(c)) {
					return CHAR;
				}
				return null;
			}
			
			public static RangeInstance getRangeOfType(Field f) {
				return getRangeOfType(f.getType());
			}
			
			public static RangeInstance getRange(Field f, Number min, Number max) {
				if(TypeUtil.isIntegral(f)) {
					return new IntegralRange(min.longValue(), max.longValue());
				}
				else if(TypeUtil.isDecimal(f)) {
					return new DecimalRange(min.doubleValue(), max.doubleValue());
				}
				throw new IllegalArgumentException(f + "");
			}
			
			public static RangeInstance getRange(Field f) {
				Range range = f.getAnnotation(Range.class);
				if(range == null) {
					range = getRangeOfType(f);
					return Cast.from(range);
				}
				if(TypeUtil.isIntegral(f)) {
					return new IntegralRange(range.min(), range.max());
				}
				else if(TypeUtil.isDecimal(f)) {
					return new DecimalRange(range.minDecimal(), range.maxDecimal());
				}
				throw new IllegalArgumentException(f + "");
			}
			
			public static void validateBounds(Range range) {
				if(!(range instanceof DecimalRange)) {
					if(range.min() > range.max()) {
						throw new InvalidRangeError("Integer range minimum is larger than its maximum");
					}
				}
				if(!(range instanceof IntegralRange)) {
					if(range.minDecimal() > range.maxDecimal()) {
						throw new InvalidRangeError("Decimal range minimum is larger than its maximum");
					}
				}
			}
			
			public static Number getMinimum(Range range) {
				if(range instanceof IntegralRange) {
					return range.min();
				}
				else if(range instanceof DecimalRange) {
					return range.minDecimal();
				}
				else {
					if(range.min() != Long.MIN_VALUE || range.max() != Long.MAX_VALUE) {
						return range.min();
					}
					if(range.minDecimal() != Double.MIN_VALUE || range.maxDecimal() != Double.MAX_VALUE) {
						return range.minDecimal();
					}
				}
				throw new IllegalStateException();
			}
			
			public static Number getMaximum(Range range) {
				if(range instanceof IntegralRange) {
					return range.max();
				}
				else if(range instanceof DecimalRange) {
					return range.maxDecimal();
				}
				else {
					if(range.min() != Long.MIN_VALUE || range.max() != Long.MAX_VALUE) {
						return range.max();
					}
					if(range.minDecimal() != Double.MIN_VALUE || range.maxDecimal() != Double.MAX_VALUE) {
						return range.maxDecimal();
					}
				}
				throw new IllegalStateException();
			}
		}
		
		public static interface RangeInstance extends Range {
			public boolean contains(Number number);
		}
		
		public static final class DecimalRange implements RangeInstance {
			
			private final Range parent;
			
			public DecimalRange(double minDecimal, double maxDecimal) {
				this(new Range() {

					@Override
					public Class<? extends Annotation> annotationType() {
						return null;
					}

					@Override
					@Deprecated
					public long min() {
						return Long.MIN_VALUE;
					}

					@Override
					@Deprecated
					public long max() {
						return Long.MAX_VALUE;
					}

					@Override
					public double minDecimal() {
						return minDecimal;
					}

					@Override
					public double maxDecimal() {
						return maxDecimal;
					}
					
				});
			}
			
			public DecimalRange(Range parent) {
				if(parent instanceof IntegralRange) {
					throw new IllegalArgumentException("Parent of decimal range cannot be an instance of IntegralRage");
				}
				if(!(parent instanceof DecimalRange)) {
					if(parent.min() != Long.MIN_VALUE || parent.max() != Long.MAX_VALUE) {
						throw new IllegalArgumentException("Parent of decimal range cannot have integral bounds set!");
					}
				}
				Ranges.validateBounds(parent);
				this.parent = parent;
			}

			@Override
			public Class<? extends Annotation> annotationType() {
				return null;
			}

			@Override
			@Deprecated
			public long min() throws UnsupportedOperationException {
				throw new UnsupportedOperationException("Should not call min() on a decimal range! Use minDecimal() instead!");
			}

			@Override
			@Deprecated
			public long max() throws UnsupportedOperationException {
				throw new UnsupportedOperationException("Should not call max() on a decimal range! Use maxDecimal() instead!");
			}

			@Override
			public double minDecimal() {
				return parent.minDecimal();
			}

			@Override
			public double maxDecimal() {
				return parent.maxDecimal();
			}
			
			public boolean contains(Number number) {
				double val = number.doubleValue();
				return val >= minDecimal() && val <= maxDecimal();
			}

		}
		
		public static final class IntegralRange implements RangeInstance {

			private final Range parent;
			
			public IntegralRange(long min, long max) {
				this(new Range() {

					@Override
					public Class<? extends Annotation> annotationType() {
						return null;
					}

					@Override
					public long min() {
						return min;
					}

					@Override
					public long max() {
						return max;
					}

					@Override
					@Deprecated
					public double minDecimal() {
						return Double.MIN_VALUE;
					}

					@Override
					@Deprecated
					public double maxDecimal() {
						return Double.MAX_VALUE;
					}
						
				});
			}
			
			public IntegralRange(Range parent) {
				if(parent instanceof DecimalRange) {
					throw new IllegalArgumentException("Parent of integral range cannot be an instance of DecimalRange");
				}
				if(!(parent instanceof IntegralRange)) {
					if(parent.minDecimal() != Double.MIN_VALUE || parent.maxDecimal() != Double.MAX_VALUE) {
						throw new IllegalArgumentException("Parent of integral range cannot have decimal bounds set!");
					}
				}
				Ranges.validateBounds(parent);
				this.parent = parent;
			}
			
			@Override
			public Class<? extends Annotation> annotationType() {
				return null;
			}

			@Override
			public long min() {
				return parent.min();
			}

			@Override
			public long max() {
				return parent.max();
			}

			@Override
			@Deprecated
			public double minDecimal() throws UnsupportedOperationException {
				throw new UnsupportedOperationException("Should not call minDecimal() on a integral range! Use min() instead!");
			}

			@Override
			@Deprecated
			public double maxDecimal() throws UnsupportedOperationException {
				throw new UnsupportedOperationException("Should not call maxDecimal() on a integral range! Use max() instead!");
			}
			
			public boolean contains(Number number) {
				double val = number.longValue();
				return val >= min() && val <= max();
			}
			
		}
		
	}
	
	public static @interface Step {
		public double value();
		
		public static enum Steps implements Step {
			INTEGRAL(1d),
			DECIMAL(0.01d);

			private final double step;
			
			private Steps(double step) {
				this.step = step;
			}
			
			@Override
			public Class<? extends Annotation> annotationType() {
				return null;
			}

			@Override
			public double value() {
				return step;
			}
			
			public static Steps getStepOfType(Class c) {
				if(TypeUtil.isInt(c) || TypeUtil.isLong(c) || TypeUtil.isShort(c) || TypeUtil.isByte(c) || TypeUtil.isChar(c)) {
					return INTEGRAL;
				}
				else if(TypeUtil.isFloat(c) || TypeUtil.isDouble(c)) {
					return DECIMAL;
				}
				throw new IllegalArgumentException(c.getCanonicalName());
			}
			
			public static Steps getStepOfType(Field f) {
				return getStepOfType(f.getType());
			}
		}
	}
	
	/**
	 * An annotation indicating that a configuration field may be intentionally
	 * left as {@code null}. This annotation is used to suppress the default behavior
	 * of firing a {@code MissingConfigValueEvent} when a configuration value is 
	 * {@code null}.
	 *
	 * <p>By default, when a configuration value from the received JSON is {@code null},
	 * it is usually considered unintended. In such cases, a {@link BadConfigValueEvent} 
	 * is fired on the {@link WilderForge#MAIN_BUS}, allowing mods 
	 * or Wilderforge to correct or replace the null value.
	 *
	 * <p>A value from the JSON configuration may be null if, for instance, a new version
	 * of a mod introduces additional configuration options that are not present in an 
	 * older version of the mod's config file. In such scenarios, fields corresponding to 
	 * these new options may be absent and read as {@code null}.
	 *
	 * <p>Applying this annotation to a field indicates that the field is intentionally
	 * nullable, preventing the {@code BadConfigValueEvent} from being fired and allowing 
	 * the value to remain {@code null}.
	 * 
	 * <p>If this annotation is used on an Object field, then all of its fields are
	 * also considered Nullable.
	 * 
	 * <p>If this annotation is used on a primitive field, then a ConfigurationError will
	 * be thrown.
	 *
	 * <p>Use this annotation for configuration values where {@code null} is an acceptable
	 * or meaningful state, and no automatic correction should be attempted.
	 * 
	 * @deprecated Not yet implemented, might be removed. Not yet considered API.
	 */
	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	@Deprecated
	@InternalOnly
	public static @interface Nullable {}
	
	/**
	 * An annotation indicating that a configuration field requires a restart when changed.
	 * This could occur if the field is modified either through the in-game GUI or by making
	 * changes to the configuration file on the filesystem, followed by a reload.
	 *
	 * <p>When a field marked with this annotation is modified, a restart is required to
	 * apply the change properly. By default, the restart is immediate but prompts the user
	 * before proceeding. If {@code prompt} is set to {@code false}, the game will restart
	 * immediately without prompting the user.
	 *
	 * <p>In the in-game GUI, the text for fields annotated with {@code @Restart} will be
	 * highlighted in red. Hovering over the field will display a localized tooltip
	 * indicating that a restart is required if the value is changed. If {@code prompt} is
	 * set to {@code false}, the tooltip will also state that the restart will take effect
	 * immediately.
	 *
	 * <p>Parameters:
	 * <ul>
	 *   <li>{@code immediate} - Determines whether the restart should be immediate.
	 *   Defaults to {@code true}.</li>
	 *   <li>{@code prompt} - Specifies whether the user should be prompted before the restart.
	 *   If set to {@code false}, the restart will occur immediately. Defaults to {@code true}.</li>
	 * </ul>
	 *
	 * <p>Use this annotation for configuration options that necessitate restarting the
	 * application or system to take effect, ensuring the user is aware and can confirm the
	 * restart if prompted.
	 * 
	 * @deprecated Not yet implemented, might be removed. Not yet considered API.
	 */
	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	@Deprecated
	@InternalOnly
	public static @interface Restart {
		/**
		 * Indicates whether the restart should be immediate when the field is changed,
		 * or whether the game is allowed to continue to run after the change is made.
		 * Defaults to {@code true}.
		 *
		 * @return {@code true} if the restart should be immediate, {@code false} otherwise
		 */
		public boolean immediate() default true;

		/**
		 * Specifies whether the user should be prompted before initiating the restart.
		 * If set to {@code false}, the game will restart immediately without prompting.
		 * Defaults to {@code true}.
		 *
		 * @return {@code true} if the user should be prompted, {@code false} otherwise
		 */
		public boolean prompt() default true;
	}
	
}
