package com.wildermods.wilderforge.launch.coremods;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import com.badlogic.gdx.utils.SerializationException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSerializer;
import com.wildermods.wilderforge.api.mixins.v1.Cast;
import com.wildermods.wilderforge.api.modLoadingV1.CoremodInfo;
import com.wildermods.wilderforge.api.modLoadingV1.MissingCoremod;
import com.wildermods.wilderforge.api.modLoadingV1.config.Config;
import com.wildermods.wilderforge.api.modLoadingV1.config.ConfigEntry;
import com.wildermods.wilderforge.api.modLoadingV1.config.BadConfigValueEvent.ConfigValueOutOfRangeEvent;
import com.wildermods.wilderforge.api.modLoadingV1.config.BadConfigValueEvent.MissingConfigValueEvent;
import com.wildermods.wilderforge.api.modLoadingV1.config.ConfigEntry.Nullable;
import com.wildermods.wilderforge.api.modLoadingV1.config.ConfigEntry.Range;
import com.wildermods.wilderforge.api.modLoadingV1.config.ConfigEntry.Range.DecimalRange;
import com.wildermods.wilderforge.api.modLoadingV1.config.ConfigEntry.Range.IntegralRange;
import com.wildermods.wilderforge.api.modLoadingV1.config.ConfigEntry.Range.Ranges;
import com.wildermods.wilderforge.api.modLoadingV1.config.ConfigEntry.Restart;
import com.wildermods.wilderforge.api.modLoadingV1.config.ConfigEntry.GUI.CustomBuilder;
import com.wildermods.wilderforge.api.modLoadingV1.config.CustomConfig;
import com.wildermods.wilderforge.api.modLoadingV1.config.ModConfigurationEntryBuilder.ConfigurationFieldContext;
import com.wildermods.wilderforge.api.utils.TypeUtil;

import com.wildermods.wilderforge.launch.InternalOnly;
import com.wildermods.wilderforge.launch.WilderForge;
import com.wildermods.wilderforge.launch.exception.ConfigurationError;
import com.wildermods.wilderforge.launch.logging.Logger;
import com.worldwalkergames.legacy.context.LegacyViewDependencies;
import com.worldwalkergames.legacy.ui.PopUp;

public class Configuration {
	public static final Gson gson;
	private static final Logger LOGGER = new Logger(Configuration.class);
	private static final Path CONFIG_FOLDER = Path.of(".").resolve("mods").resolve("configs");
	private static final HashMap<CoremodInfo, Function<LegacyViewDependencies, ? extends PopUp>> customConfigurations = new HashMap<>();;
	private static final HashMap<CoremodInfo, ?> configurations = new HashMap<>();
	private static final HashMap<CoremodInfo, ?> defaults = new HashMap<>();
	static {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(Character.class, (JsonSerializer<Character>)(src, typeOfSrc, context) -> {
			if(src == null) {
				return null;
			}
			return context.serialize((int) src.charValue());
		});
		gsonBuilder.setPrettyPrinting();
		gson = gsonBuilder.create();
	}
	
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
					Object defaultConfig = constructor.newInstance();
					Object configuration = constructor.newInstance();
					defaults.put(coremod, Cast.from(defaultConfig));
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
		Set<Field> fields = new LinkedHashSet<>();
		fields.addAll(List.of(configClass.getDeclaredFields()));
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
			Range uRange = field.getAnnotation(Range.class);
			IntegralRange iRange;
			DecimalRange dRange;
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
						
						if(TypeUtil.isDouble(field)) {
							value = jsonValue.asDouble();
						}
						else if(TypeUtil.isFloat(field)) {
							value = jsonValue.asFloat();
						}
						
						if(uRange == null) {
							uRange = Ranges.getRangeOfType(type);
						}
						try {
							dRange = new DecimalRange(uRange);
						}
						catch(Throwable t) {
							throw new ConfigurationError("Invalid @Range", t);
						}
						if(value != null) {
							Double dval = ((Number)value).doubleValue();
							if(dRange.contains(dval)) {
								ConfigValueOutOfRangeEvent e = new ConfigValueOutOfRangeEvent(config, configurationObj, field, dval, dRange);
								WilderForge.MAIN_BUS.fire(e);
								dval = ((Number)e.getValue()).doubleValue();
							}
						}
						break;
					case longValue:
						
						if(TypeUtil.isLong(field)) {
							value = jsonValue.asLong();
						}
						else if(TypeUtil.isInt(field)) {
							value = jsonValue.asInt();
						}
						if(TypeUtil.isChar(field)) {
							value = jsonValue.asChar();
						}
						else if(TypeUtil.isShort(field)) {
							value = jsonValue.asShort();
						}
						else if(TypeUtil.isByte(field)) {
							value = jsonValue.asByte();
						}
						
						
						if(uRange == null) {
							uRange = Ranges.getRangeOfType(type);
						}
						try {
							iRange = new IntegralRange(uRange);
						}
						catch(Throwable t) {
							throw new ConfigurationError("Invalid @Range", t);
						}
						
						if(value != null) {
							Long ival;
							if(TypeUtil.isChar(field)) {
								ival = (long) ((Character)value).charValue();
							}
							else {
								ival = ((Number)value).longValue();
							}
							if(iRange.contains(ival)) {
								ConfigValueOutOfRangeEvent e = new ConfigValueOutOfRangeEvent(config, configurationObj, field, ival, iRange);
								WilderForge.MAIN_BUS.fire(e);
								ival = ((Number)e.getValue()).longValue();
							}
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
			LOGGER.log("Set " + field.getName() + " to " + value);
		}
		
		return new ConfigStatus(changed, restartInfo);
	}
	
	@InternalOnly
	public static Object getDefaultConfig(Config c) {
		CoremodInfo coremod = getCoremod(c);
		if(coremod instanceof MissingCoremod) {
			return null;
		}
		Object config = defaults.get(c);
		return config;
	}
	
	@InternalOnly
	public static Object getConfig(Config c) {
		CoremodInfo coremod = getCoremod(c);
		if(coremod instanceof MissingCoremod) {
			return null;
		}
		Object config = configurations.get(c);
		return config;
	}
	
	@SuppressWarnings("rawtypes")
	public static void saveConfig(Config c, Object configObject, HashMap<ConfigurationFieldContext, ConfigurationFieldContext> fields) throws IOException {
		
		try {
		
			Path configFile = getConfigFile(c);
			Files.createDirectories(configFile.getParent());
			Object newConfigObject;
			
			try {
				Constructor configConstructor = configObject.getClass().getDeclaredConstructor();
				configConstructor.setAccessible(true);
				newConfigObject = configConstructor.newInstance();
			} catch (Throwable t) {
				throw new ConfigurationError("Unable to create configuration object for mod " + c.modid(), t);
			}
			
			try (BufferedWriter writer = Files.newBufferedWriter(configFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
				
				LinkedHashMap<String, Object> jsonMap = new LinkedHashMap<>();
				for(ConfigurationFieldContext context : fields.keySet()) {
					jsonMap.put(context.getField().getName(), context.obtainVal());
					context.getField().set(newConfigObject, context.obtainVal());
				}
				writer.append(gson.toJson(jsonMap));
			}
			
			
			
			if(configurations.put(getCoremod(c), Cast.from(newConfigObject)) == null) {
				if(getConfig(c) != newConfigObject) {
					throw new AssertionError("wtf");
				}
				throw new AssertionError();
			}
		}
		catch(Throwable t) {
			throw new ConfigurationError("Unable to save configuration for mod " + c.modid());
		}
		
	}
	
	@InternalOnly
	public static Function<LegacyViewDependencies, ? extends PopUp> getCustomConfigPopUp(CoremodInfo c) {
		return customConfigurations.get(c);
	}
	
	private static Path getConfigFile(Config c) {
		return CONFIG_FOLDER.resolve(c.modid() + ".config.json");
	}
	
	private static CoremodInfo getCoremod(Config c) {
		if(c instanceof CoremodInfo) {
			return Cast.from(c);
		}
		return Coremods.getCoremod(c.modid());
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
		public boolean immediate() {
			return immediate;
		}

		@Override
		public boolean prompt() {
			return prompt;
		}
		
	}
	
}
