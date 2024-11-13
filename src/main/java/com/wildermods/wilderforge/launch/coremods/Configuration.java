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
import java.util.function.Function;

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
import com.wildermods.wilderforge.api.modLoadingV1.config.ConfigEntry.Range.Ranges;
import com.wildermods.wilderforge.api.modLoadingV1.config.ConfigEntry.Restart;
import com.wildermods.wilderforge.api.modLoadingV1.config.ConfigEntry.GUI.CustomBuilder;
import com.wildermods.wilderforge.api.modLoadingV1.config.CustomConfig;
import com.wildermods.wilderforge.api.utils.TypeUtil;
import com.wildermods.wilderforge.launch.InternalOnly;
import com.wildermods.wilderforge.launch.WilderForge;
import com.wildermods.wilderforge.launch.exception.ConfigurationError;
import com.worldwalkergames.legacy.context.LegacyViewDependencies;
import com.worldwalkergames.legacy.ui.PopUp;

public class Configuration {
	private static final Path CONFIG_FOLDER = Path.of(".").resolve("mods").resolve("configs");
	private static final HashMap<CoremodInfo, Function<LegacyViewDependencies, ? extends PopUp>> customConfigurations = new HashMap<>();;
	private static final HashMap<CoremodInfo, ?> configurations = new HashMap<>();
	
	@InternalOnly
	@SuppressWarnings("unchecked")
	public static void initializeConfigurations() {
		try {
			Set<Class<?>> customConfigs = WilderForge.getReflectionsHelper().getAllClassesAnnotatedWith(CustomConfig.class);
			Set<Class<?>> configClasses = WilderForge.getReflectionsHelper().getAllClassesAnnotatedWith(Config.class);
			
			for(Class<?> c : customConfigs) {
				CustomConfig config = c.getAnnotation(CustomConfig.class);
				CoremodInfo coremod = Coremods.getCoremod(config.modid());
				if(coremod instanceof MissingCoremod) {
					throw new ConfigurationError("Class " + c.getCanonicalName() + " is defined as a @CustomConfig for the mod " + config.modid() + ", but that mod is missing!");
				}
				try {
					Constructor<? extends Function<LegacyViewDependencies, ? extends PopUp>> constructor = config.popup().getDeclaredConstructor();
					constructor.setAccessible(true);
					Function<LegacyViewDependencies, ? extends PopUp> function = constructor.newInstance();
					customConfigurations.put(coremod, function);
				} catch (NoSuchMethodException e) {
					throw new ConfigurationError("Class " + config.popup() + " is a custom configuration popup function, but it doesn't have a nullary constructor!");
				} catch (Throwable t) {
					throw new ConfigurationError("Couldn't construct configuration function object for mod " + config.modid() + ". Unable to construct class " + config.popup().getCanonicalName(), t);
				}
			}
			
			for(Class<?> c : configClasses) {
				Config config = c.getAnnotation(Config.class);
				CustomBuilder builder = c.getAnnotation(CustomBuilder.class);
				final String logTag = "Configuration/" + config.modid();
				WilderForge.LOGGER.log("Found configuration class " + c.getCanonicalName(), logTag);
				CoremodInfo coremod = Coremods.getCoremod(config.modid());
				if(coremod instanceof MissingCoremod) {
					throw new ConfigurationError("Class " + c.getCanonicalName() + " is defined as a @Config for the mod " + config.modid() + ", but that mod is missing!");
				}
				try {
					if(customConfigs.contains(c)) {
						throw new ConfigurationError("Mod " + config.modid() + " cannot have @Config and @CustomConfig definitions!");
					}
					Constructor<?> constructor = c.getDeclaredConstructor();
					constructor.setAccessible(true);
					Object configuration = constructor.newInstance();
					ConfigStatus status = populate(config, coremod, c, Cast.from(configuration));
					if(status.changed()) {
						throw new AssertionError("Configurations already initialized???? They shouldn't have been able to be changed!");
					}
					
					if(builder != null) {
						Constructor builderFuncConstructor;
						try {
							builderFuncConstructor = c.getDeclaredConstructor();
							builderFuncConstructor.setAccessible(true);
							builderFuncConstructor.newInstance();
						}
						catch(NoSuchMethodException e) {
							throw new ConfigurationError("Custom builder function " + c.getCanonicalName() + " must have a nullary constructor!", e);
						}
						catch(IllegalAccessException e) {
							throw new ConfigurationError("Nullary constructor for builder " + c.getCanonicalName() + " is not accessable.", e);
						}
					}
					
					configurations.put(coremod, Cast.from(configuration));
					WilderForge.LOGGER.log("Placed configuration in configuration map", logTag);
				} catch (NoSuchMethodException e) {
					throw new ConfigurationError("Class " + c.getCanonicalName() + "is a @Config, but it doesn't have a nullary constructor!", e);
				} catch (IllegalAccessException e) {
					throw new ConfigurationError("Nullary constructor for mod " + config.modid() + " is not accessable.", e);
				} catch (Throwable t) {
					throw new ConfigurationError("Couldn't construct configuration object for mod " + config.modid(), t);
				}
			}
		}
		catch(ConfigurationError ce) {
			throw ce;
		}
		catch(Throwable t) {
			throw new ConfigurationError("Couldn't initilaize configurations", t);
		}
	}
	
	private static <C> ConfigStatus populate(Config config, CoremodInfo coremod, Class<C> configClass, C configurationObj) throws IOException, IllegalArgumentException, IllegalAccessException {
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
	public static Object getConfig(Config c) {
		if(!(c instanceof CoremodInfo)) {
			c = Coremods.getCoremod(c.modid());
			if(c instanceof MissingCoremod) {
				return null;
			}
		}
		return configurations.get(c);
	}
	
	@InternalOnly
	public static Function<LegacyViewDependencies, ? extends PopUp> getCustomConfigPopUp(CoremodInfo c) {
		return customConfigurations.get(c);
	}
	
	private static Path getConfigFile(CoremodInfo c) {
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
	
}
