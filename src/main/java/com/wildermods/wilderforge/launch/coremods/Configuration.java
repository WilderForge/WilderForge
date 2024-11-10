package com.wildermods.wilderforge.launch.coremods;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.SerializationException;
import com.wildermods.wilderforge.api.mixins.v1.Cast;
import com.wildermods.wilderforge.api.modLoadingV1.CoremodInfo;
import com.wildermods.wilderforge.api.modLoadingV1.MissingCoremod;
import com.wildermods.wilderforge.api.modLoadingV1.config.Config;
import com.wildermods.wilderforge.api.modLoadingV1.config.ConfigEntry;
import com.wildermods.wilderforge.api.modLoadingV1.config.BadConfigValueEvent.ConfigValueOutOfRangeEvent;
import com.wildermods.wilderforge.api.modLoadingV1.config.BadConfigValueEvent.MissingConfigValueEvent;
import com.wildermods.wilderforge.api.modLoadingV1.config.ConfigEntry.Nullable;
import com.wildermods.wilderforge.api.modLoadingV1.config.ConfigEntry.Range;
import com.wildermods.wilderforge.api.modLoadingV1.config.ConfigEntry.Restart;
import com.wildermods.wilderforge.api.utils.TypeUtil;
import com.wildermods.wilderforge.launch.InternalOnly;
import com.wildermods.wilderforge.launch.WilderForge;
import com.wildermods.wilderforge.launch.exception.ConfigurationError;
import com.wildermods.wilderforge.launch.exception.ConfigurationError.InvalidRangeError;

public class Configuration {

	private static final Path CONFIG_FOLDER = Path.of(".").resolve("mods").resolve("configs");
	private static final HashMap<CoremodInfo, ?> configurations = new HashMap<>();
	
	@InternalOnly
	@SuppressWarnings("unchecked")
	public static void initializeConfigurations() {
		Set<Class<?>> configClasses = WilderForge.getReflectionsHelper().getAllClassesAnnotatedWith(Config.class);
		
		for(Class<?> c : configClasses) {
			Config config = c.getAnnotation(Config.class);
			CoremodInfo coremod = Coremods.getCoremod(config.modId());
			if(coremod instanceof MissingCoremod) {
				throw new ConfigurationError("Class " + c.getCanonicalName() + " is defined as a @Config for the mod " + config.modId() + ", but that mod is missing!");
			}
			try {
				Constructor<?> constructor = c.getDeclaredConstructor();
				constructor.setAccessible(true);
				Object configuration = constructor.newInstance();
				ConfigStatus status = populate(config, coremod, c, Cast.from(configuration));
				if(status.changed()) {
					throw new AssertionError("Configurations already initialized???? They shouldn't have been able to be changed!");
				}
				
				configurations.put(coremod, Cast.from(configuration));
			} catch (NoSuchMethodException e) {
				throw new ConfigurationError("Class " + c.getCanonicalName() + "is a @Config, but it doesn't have a nullary constructor!", e);
			} catch (IllegalAccessException e) {
				throw new ConfigurationError("Nullary constructor for mod " + config.modId() + " is not accessable.", e);
			} catch (Throwable t) {
				throw new ConfigurationError("Couldn't construct configuration object for mod " + config.modId(), t);
			}
		}
	}
	
	private static <C> ConfigStatus populate(Config config, CoremodInfo<C> coremod, Class<C> configClass, C configurationObj) throws IOException, IllegalArgumentException, IllegalAccessException {
		Set<Field> fields = WilderForge.getReflectionsHelper().getAllFieldsInAnnotatedWith(configClass, ConfigEntry.class);
		ConfigStatus ret;
		Path configFile = getConfigFile(coremod);
		JsonValue values = null;
		if(Files.exists(configFile)) {
			values = readConfigFile(configFile);
		}
		
		if(values == null) {
			return new ConfigStatus();
		}
		
		RestartImpl restartInfo = null;
		C previousConfig = Cast.from(configurations.get(coremod));
		boolean changed = false;
		
		for(Field field : fields) {
			if(Modifier.isTransient(field.getModifiers())) {
				continue;
			}
			if(Modifier.isStatic(field.getModifiers())) {
				throw new ConfigurationError("@Config classes cannot have static fields unless they are transient");
			}
			
			field.setAccessible(true);
			
			Class<?> type = field.getType();
			boolean isPrimitiveType = field.getType().isPrimitive() && !TypeUtil.isVoid(type);
			ConfigEntry configEntry = field.getAnnotation(ConfigEntry.class);
			Range range = field.getAnnotation(Range.class);
			Nullable nullable = field.getAnnotation(Nullable.class);
			Restart restart = field.getAnnotation(Restart.class);
			if(restartInfo == null && restart != null) {
				restartInfo = new RestartImpl();
			}
			
			if(isPrimitiveType && nullable != null) {
				throw new ConfigurationError("@Nullable annotation placed on primitive field \"" + field.getName() + "\"");
			}
			
			String name = field.getName();
			LinkedHashSet<String> valueCorrectors = new LinkedHashSet<>();
			valueCorrectors.add(coremod.modId);
			{
				String[] definedCorrectors = {"wilderforge"};
				
				if(configEntry != null) {
					if(!configEntry.name().isBlank()) {
						name = configEntry.name();
					}
					definedCorrectors = configEntry.valueCorrectors();
				}
				valueCorrectors.addAll(List.of(definedCorrectors));
			}
			
			Object prevValue = null;
			Object value = null;
			JsonValue jsonValue = values.get(name);
			
			if(jsonValue == null && nullable == null) {
				MissingConfigValueEvent e = new MissingConfigValueEvent(config, configurationObj, field);
				WilderForge.MAIN_BUS.fire(e);
				value = e.getValue();
			}
			
			if(previousConfig != null) {
				prevValue = field.get(previousConfig);
			}
			
			if(jsonValue != null) {
				switch(jsonValue.type()) {
					case array:
						throw new ConfigurationError("Arrays not yet implemented for configurations");
					case booleanValue:
						value = jsonValue.asBoolean();
						break;
					case doubleValue:
						value = jsonValue.asDouble();
						if(range == null) {
							range = Ranges.getRangeOfType(type);
						}
						if(range.min() != Long.MIN_VALUE || range.max() != Long.MAX_VALUE) {
							throw new ConfigurationError("Integeral range specified on a floating point type");
						}
						Double dval = Cast.from(value);
						if(!Ranges.within(dval, range)) {
							ConfigValueOutOfRangeEvent e = new ConfigValueOutOfRangeEvent(config, configurationObj, field, dval, range);
							WilderForge.MAIN_BUS.fire(e);
							dval = ((Number)e.getValue()).doubleValue();
						}
						break;
					case longValue:
						value = jsonValue.asLong();
						if(range == null) {
							range = Ranges.getRangeOfType(type);
						}
						if(range.minDecimal() != Double.MIN_VALUE || range.maxDecimal() != Double.MAX_VALUE) {
							throw new ConfigurationError("Decimal range specified on integral type");
						}
						Long ival = Cast.from(value);
						if(!Ranges.within(ival, range)) {
							ConfigValueOutOfRangeEvent e = new ConfigValueOutOfRangeEvent(config, configurationObj, field, ival, range);
							WilderForge.MAIN_BUS.fire(e);
							ival = ((Number)e.getValue()).longValue();
						}
						break;
					case nullValue:
						if(nullable != null) {
							value = null;
						}
						else {
							MissingConfigValueEvent e = new MissingConfigValueEvent(config, configurationObj, field);
							WilderForge.MAIN_BUS.fire(e);
							value = e.getValue();
						}
						break;
					case object:
						throw new ConfigurationError("Objects not yet implemented for configurations");
					case stringValue:
						value = jsonValue.asString();
						break;
					default:
						throw new AssertionError("Unknown json type: " + jsonValue.type());
				
				}
			}
			
			if(previousConfig != null) {
				if(!Objects.equals(prevValue,  value)) {
					changed = true;
					if(restart != null) {
						restartInfo.process(restart);
					}
				}
			}
			
			field.set(configurationObj, value);
			
		}
		
		return new ConfigStatus(changed, restartInfo);
	}
	
	@InternalOnly
	public static <C> C getConfig(CoremodInfo<C> c) {
		return Cast.from(configurations.get(c));
	}
	
	private static Path getConfigFile(CoremodInfo<?> c) {
		return CONFIG_FOLDER.resolve(c.modId + ".config.json");
	}
	
	private static JsonValue readConfigFile(Path path) throws IOException {
		JsonReader json = new JsonReader();
		JsonValue configJson = json.parse(Files.newBufferedReader(path));
		if(!configJson.isObject()) {
			throw new SerializationException("Config " + path  + " is not a json object?!");
		}
		return configJson;
	}
	
	private record ConfigStatus(boolean changed, RestartImpl restart) implements Restart {
		
		public ConfigStatus() {
			this(false);
		}
		
		public ConfigStatus(boolean changed) {
			this(changed, null);
		}
		
		public ConfigStatus(RestartImpl restart) {
			this(true, restart);
		}

		@Override
		public Class<? extends Annotation> annotationType() {
			return null;
		}

		@Override
		public boolean immediate() {
			return restart != null && restart.immediate();
		}

		@Override
		public boolean prompt() {
			return restart != null && restart.prompt();
		}

		@Override
		public boolean strict() {
			return restart != null && restart.strict();
		}
		
	}
	
	private static class RestartImpl implements Restart {

		boolean immediate = false;
		boolean prompt = true;
		
		void process(Restart restart) {
			if(restart.immediate()) {
				immediate = true;
			}
			if(!restart.prompt()) {
				prompt = false;
			}
		}
		
		@Override
		public Class<? extends Annotation> annotationType() {
			return null;
		}
		
		@Override
		public boolean strict() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean immediate() {
			return immediate;
		}

		@Override
		public boolean prompt() {
			return prompt;
		}
		
	}
	
	private static enum Ranges implements Range {
		
		BYTE(Byte.MIN_VALUE, Byte.MAX_VALUE),
		SHORT(Short.MIN_VALUE, Short.MAX_VALUE),
		CHAR(Character.MIN_VALUE, Character.MAX_VALUE),
		INT(Integer.MIN_VALUE, Integer.MAX_VALUE),
		LONG(Long.MIN_VALUE, Long.MAX_VALUE),
		FLOAT(Float.MIN_VALUE, Float.MAX_VALUE),
		DOUBLE(Double.MIN_VALUE, Double.MAX_VALUE);

		private long min = Long.MIN_VALUE;
		private long max = Long.MAX_VALUE;
		private double minDecimal = Double.MIN_VALUE;
		private double maxDecimal = Double.MAX_VALUE;
		
		private Ranges(long min, long max) {
			this.min = min;
			this.max = max;
		}
		
		private Ranges(double minDecimal, double maxDecimal) {
			this.minDecimal = minDecimal;
			this.maxDecimal = maxDecimal;
		}
		
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
		public double minDecimal() {
			return minDecimal;
		}

		@Override
		public double maxDecimal() {
			return maxDecimal;
		}
		
		public static boolean within(Number number, Range range) {
			if(number instanceof Float || number instanceof Double) {
				double dval = number.doubleValue();
				return dval >= range.minDecimal() && dval <= range.maxDecimal();
			}
			else {
				long ival = number.longValue();
				return ival >= range.min() && ival <= range.max();
			}
		}
		
		public static void validateRange(Range range) {
			if(range.min() >= range.max()) {
				throw new InvalidRangeError("Integer range minimum is larger than or equal to it's maximum");
			}
			if(range.minDecimal() >= range.maxDecimal()) {
				throw new InvalidRangeError("Decimal range minimum is larger than or equal to it's maximum");
			}
		}
		
		public static Ranges getRangeOfType(Class c) {
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
			throw new IllegalArgumentException(c.getCanonicalName());
		}
		
	}
	
}
