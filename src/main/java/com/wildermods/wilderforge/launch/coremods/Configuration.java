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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
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
import com.wildermods.wilderforge.api.modLoadingV1.Mod;
import com.wildermods.wilderforge.api.modLoadingV1.config.Config;
import com.wildermods.wilderforge.api.modLoadingV1.config.ConfigEntry;
import com.wildermods.wilderforge.api.modLoadingV1.config.ConfigSavedEvent;
import com.wildermods.wilderforge.api.modLoadingV1.config.BadConfigValueEvent;
import com.wildermods.wilderforge.api.modLoadingV1.config.BadConfigValueEvent.ConfigValueOutOfRangeEvent;
import com.wildermods.wilderforge.api.modLoadingV1.config.BadConfigValueEvent.MissingConfigValueEvent;
import com.wildermods.wilderforge.api.modLoadingV1.config.ConfigEntry.Nullable;
import com.wildermods.wilderforge.api.modLoadingV1.config.ConfigEntry.Range;
import com.wildermods.wilderforge.api.modLoadingV1.config.ConfigEntry.Range.DecimalRange;
import com.wildermods.wilderforge.api.modLoadingV1.config.ConfigEntry.Range.IntegralRange;
import com.wildermods.wilderforge.api.modLoadingV1.config.ConfigEntry.Range.RangeInstance;
import com.wildermods.wilderforge.api.modLoadingV1.config.ConfigEntry.Range.Ranges;
import com.wildermods.wilderforge.api.modLoadingV1.config.ConfigEntry.Restart;
import com.wildermods.wilderforge.api.modLoadingV1.config.ConfigEntry.GUI.CustomBuilder;
import com.wildermods.wilderforge.api.modLoadingV1.config.CustomConfig;
import com.wildermods.wilderforge.api.modLoadingV1.config.ModConfigurationEntryBuilder.ConfigurationContext;
import com.wildermods.wilderforge.api.modLoadingV1.config.ModConfigurationEntryBuilder.ConfigurationFieldContext;
import com.wildermods.wilderforge.api.utils.TypeUtil;

import com.wildermods.wilderforge.launch.InternalOnly;
import com.wildermods.wilderforge.launch.WilderForge;
import com.wildermods.wilderforge.launch.exception.ConfigurationError;
import com.wildermods.wilderforge.launch.exception.EventTargetError;
import com.wildermods.wilderforge.launch.logging.Logger;
import com.worldwalkergames.legacy.context.LegacyViewDependencies;
import com.worldwalkergames.legacy.ui.PopUp;

import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBusFireOrder;
import net.minecraftforge.eventbus.api.IEventListener;

public class Configuration {
	public static final Gson gson;
	private static final Logger LOGGER = new Logger(Configuration.class);
	private static final Path CONFIG_FOLDER = Path.of(".").resolve("mods").resolve("configs");
	private static final HashMap<CoremodInfo, Function<LegacyViewDependencies, ? extends PopUp>> customConfigurations = new HashMap<>();;
	private static final HashMap<CoremodInfo, ?> configurations = new HashMap<>();
	private static final HashMap<CoremodInfo, ?> defaults = new HashMap<>();
	private static boolean ready = false;
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
		ready = true;
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
					ConfigStatus status = populate(config, coremod, c, Cast.from(configuration), Cast.from(defaultConfig));
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
	
	private static <C> ConfigStatus populate(Config config, CoremodInfo coremod, Class<C> configClass, C configurationObj, C defaultConfigurationObj) throws Exception {
		Set<Field> fields = new LinkedHashSet<>();
		fields.addAll(List.of(configClass.getDeclaredFields()));
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
			boolean isPrimitiveType = TypeUtil.representsPrimitive(type) && !TypeUtil.isVoid(type);
			ConfigEntry configEntry = field.getAnnotation(ConfigEntry.class);
			if(configEntry == null) {
				configEntry = new DefaultConfigEntry(config, field);
			}
			boolean strict = configEntry.strict();
			RangeInstance typeRange;
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
			if(nullable == null) {
				Object defaultValue = field.get(defaultConfigurationObj);
				if(defaultValue == null) {
					throw new NullPointerException("Default value for field " + field.getName() + " in config for " + config.modid() + " is null and is not @Nullable");
				}
			}
			
			String name = field.getName();
			LinkedHashSet<String> valueCorrectors = new LinkedHashSet<>();
			valueCorrectors.add(coremod.modId);
			{
				String[] definedCorrectors = configEntry.valueCorrectors();
				valueCorrectors.addAll(List.of(definedCorrectors));
			}
			
			Object prevValue = null;
			Object value = null;
			JsonValue jsonValue = values.get(name);
			
			if(jsonValue == null && nullable == null) {
				MissingConfigValueEvent e = new MissingConfigValueEvent(config, configEntry, configurationObj, field);
				fireInCorrectorOrder(e);
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
						{
							typeRange = Ranges.getRangeOfType(type);
							double rawVal = jsonValue.asDouble();
							
							if(TypeUtil.isDouble(field)) {
								value = jsonValue.asDouble();
							}
							else if(TypeUtil.isFloat(field)) {
								value = jsonValue.asFloat();
							}
							
							if(uRange == null) {
								uRange = typeRange;
							}
							try {
								dRange = new DecimalRange(uRange);
								if(!typeRange.contains(dRange.minDecimal())) {
									throw new ConfigurationError("minDecimal definition (" + dRange.minDecimal() + ") is out of range for type " + type);
								}
								if(!typeRange.contains(dRange.maxDecimal())) {
									throw new ConfigurationError("maxDecimal definition (" + dRange.maxDecimal() + ") is out of range for type " + type);
								}
							}
							catch(Throwable t) {
								throw new ConfigurationError("Invalid @Range for field " + field.getName(), t);
							}
							if(value != null) {
								Double dval = ((Number)value).doubleValue();
								if(!dRange.contains(rawVal)) {
									ConfigValueOutOfRangeEvent e = new ConfigValueOutOfRangeEvent(config, configEntry, configurationObj, field, rawVal, dRange);
									fireInCorrectorOrder(e);
									dval = ((Number)e.getValue()).doubleValue();
								}
								value = dval;
							}
						}
						break;
					case longValue:
						{
							typeRange = Ranges.getRangeOfType(type);
							long rawVal = jsonValue.asLong();
							
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
								uRange = typeRange;
							}
							try {
								iRange = new IntegralRange(uRange);
								if(!typeRange.contains(iRange.min())) {
									throw new ConfigurationError("min definition (" + iRange.min() + ") is out of range for type " + type);
								}
								if(!typeRange.contains(iRange.max())) {
									throw new ConfigurationError("max definition (" + iRange.max() + ") is out of range for type " + type);
								}
							}
							catch(Throwable t) {
								throw new ConfigurationError("Invalid @Range for field " + field.getName(), t);
							}
							
							if(value != null) {
								Long ival;
								if(TypeUtil.isChar(field)) {
									ival = (long) ((Character)value).charValue();
								}
								else {
									ival = ((Number)value).longValue();
								}
								if(!iRange.contains(rawVal)) {
									ConfigValueOutOfRangeEvent e = new ConfigValueOutOfRangeEvent(config, configEntry, configurationObj, field, rawVal, iRange);
									fireInCorrectorOrder(e);
									ival = ((Number)e.getValue()).longValue();
								}
								value = ival;
							}
						}
						break;
					case nullValue:
						if(nullable != null) {
							value = null;
						}
						else {
							MissingConfigValueEvent e = new MissingConfigValueEvent(config, configEntry, configurationObj, field);
							fireInCorrectorOrder(e);
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
				if((!strict && !Objects.equals(prevValue,  value)) || (strict && (prevValue != value))) {
					changed = true;
					if(restart != null) {
						restartInfo.process(restart);
					}
				}
			}
			
			if(TypeUtil.isIntegral(type)) {
				setIntegralField(configurationObj, field, TypeUtil.asIntegralPrimitive(value));
			}
			else if(TypeUtil.isDecimal(type)) {
				setDecimalField(configurationObj, field, TypeUtil.asDecimalPrimitive(value));
			}
			else {
				field.set(configurationObj, value);
			}
			
			LOGGER.log("Set " + field.getName() + " to " + value, config.modid());
		}
		
		return new ConfigStatus(changed, restartInfo);
	}
	
	private static void setIntegralField(Object target, Field field, long value) throws Exception {

		RangeInstance definedRange = Ranges.getRange(field);
		if(!definedRange.contains(value)) { //check to make sure the default range is within explicitly defined @Range
			throw new ConfigurationError("Default config value for " + field.getName() + " is (" + value + "), which is out of range (min: " + definedRange.min() + ", max: " + definedRange.max() + ")");
		}
		
		Class<?> fieldType = field.getType();

		if (fieldType == byte.class) {
			field.set(target, (byte) value);
		}
		else if (fieldType == Byte.class) {
			field.set(target, Byte.valueOf((byte) value)); // Explicit boxing
		}
		else if (fieldType == short.class) {
			field.set(target, (short) value);
		}
		else if (fieldType == Short.class) {
			field.set(target, Short.valueOf((short) value)); // Explicit boxing
		}
		else if (fieldType == int.class) {
			field.set(target, (int) value);
		}
		else if (fieldType == Integer.class) {
			field.set(target, Integer.valueOf((int) value)); // Explicit boxing
		}
		else if (fieldType == char.class) {
			field.set(target, (char) value);
		}
		else if (fieldType == Character.class) {
			field.set(target, Character.valueOf((char) value)); // Explicit boxing
		}
		else if (fieldType == long.class) {
			field.set(target, value);
		}
		else if (fieldType == Long.class) {
			field.set(target, Long.valueOf(value)); // Explicit boxing
		}
		else {
			throw new IllegalArgumentException("Field is not an integral type: " + fieldType);
		}
	}
	
	private static void setDecimalField(Object target, Field field, double value) throws Exception {
		
		RangeInstance definedRange = Ranges.getRange(field);
		if(!definedRange.contains(value)) {
			throw new ConfigurationError("Default config value for " + field.getName() + " is (" + value + "), which is out of range (min: " + definedRange.minDecimal() + ", max: " + definedRange.maxDecimal() + ")");
		}
		
		Class<?> fieldType = field.getType();
		
		if(fieldType == float.class) {
			field.set(target, (float)value);
		}
		else if(fieldType == Float.class) {
			field.set(field, Float.valueOf((float) value)); // Explicit Boxing
		}
		else if(fieldType == double.class) {
			field.set(target, value);
		}
		else if(fieldType == Double.class) {
			field.set(field, Double.valueOf(value)); //Explicit Boxing
		}
		else {
			throw new IllegalArgumentException("Field is not a decimal type: " + field);
		}
	}
	
	@InternalOnly
	public static Object getDefaultConfig(Config c) {
		isReady();
		CoremodInfo coremod = getCoremod(c);
		if(coremod instanceof MissingCoremod) {
			return null;
		}
		for(Entry<CoremodInfo, ?> entry : defaults.entrySet()) {
			System.out.println("Hash of default entry " + entry.getKey() + ": " + entry.getKey().hashCode());
			System.out.println("Equals: " + Objects.equals(entry.getKey(), coremod));
		}
		System.out.println("Hash we are looking for: " + coremod.hashCode());
		Object config = defaults.get(coremod);
		return config;
	}
	
	@InternalOnly
	public static Object getConfig(Config c) {
		isReady();
		CoremodInfo coremod = getCoremod(c);
		if(coremod instanceof MissingCoremod) {
			return null;
		}
		Object config = configurations.get(coremod);
		return config;
	}
	
	@SuppressWarnings("rawtypes")
	public static void saveConfig(Config c, Object configObject, HashMap<ConfigurationFieldContext, ConfigurationFieldContext> fields) throws IOException {
		isReady();
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
		
		WilderForge.MAIN_BUS.fire(new ConfigSavedEvent(new ConfigurationContext(c, configObject)));
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
	
	public static ConfigEntry getConfigEntry(Config config, Field f) {
		ConfigEntry entry = f.getAnnotation(ConfigEntry.class);
		if(entry == null) {
			entry = new DefaultConfigEntry(config, f);
		}
		return entry;
	}
	
	private static JsonValue readConfigFile(Path path) throws IOException {
		JsonReader json = new JsonReader();
		JsonValue configJson = json.parse(Files.newBufferedReader(path));
		if(!configJson.isObject()) {
			throw new SerializationException("Config " + path  + " is not a json object?!");
		}
		return configJson;
	}
	
	private static void isReady() throws IllegalStateException {
		if(!ready) {
			throw new IllegalStateException("Cannot access configurations until after initialization!");
		}
	}
	
	private static <T extends BadConfigValueEvent> T fireInCorrectorOrder(BadConfigValueEvent e) {
		return Cast.from(WilderForge.MAIN_BUS.fire(e, new CorrectorFiringOrder()));
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
	
	public static final class DefaultConfigEntry implements ConfigEntry {

		private final String name;
		private final String[] valueCorrectors;
		
		public DefaultConfigEntry(Config config, Field f) {
			this.name = f.getName();
			this.valueCorrectors = new String[] {config.modid(), WilderForge.modid};
		}
		
		@Override
		public Class<? extends Annotation> annotationType() {
			return null;
		}

		@Override
		public String name() {
			return name;
		}

		@Override
		public String[] valueCorrectors() {
			return valueCorrectors;
		}

		@Override
		public boolean strict() {
			return false;
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
	
	public static final class CorrectorFiringOrder implements IEventBusFireOrder {
		
		@Override
		public List<IEventListener> reorder(Event event, List<IEventListener> listeners) {
			if (event instanceof BadConfigValueEvent e) {
				List<String> correctors = new ArrayList<>(Arrays.asList(e.getConfigEntry().valueCorrectors()));
				boolean wilderforgeMissing = !correctors.contains("wilderforge");

				// Ensure "wilderforge" is always at the end if it wasn't already present
				if (wilderforgeMissing) {
					correctors.add("wilderforge");
				}

				LinkedHashMap<String, List<IEventListener>> groupedListeners = new LinkedHashMap<>();

				// Step 1: Group listeners by modid
				for (IEventListener listener : listeners) {
					Mod mod = listener.listeningMethod().getDeclaringClass().getAnnotation(Mod.class);
					if (mod == null) {
						throw new EventTargetError("Method " + listener.listeningMethod().getName() 
							+ " in " + listener.listeningMethod().getDeclaringClass().getName() 
							+ " has been registered in a class that is not annotated with @Mod!");
					}
					groupedListeners.computeIfAbsent(mod.modid(), k -> new ArrayList<>()).add(listener);
				}

				// Step 2: Sort each group's listeners by event priority
				for (List<IEventListener> group : groupedListeners.values()) {
					group.sort(Comparator.comparingInt(l -> l.subscribeInfo().priority()));
				}

				// Step 3: Merge groups based on `correctors` order
				List<IEventListener> ret = new ArrayList<>();
				for (String corrector : correctors) {
					if (groupedListeners.containsKey(corrector)) {
						if (wilderforgeMissing && corrector.equals("wilderforge")) {
							/* Only include `wilderforge` listeners with priority HIGHER
							 * ensures that even if "wilderforge" is not explicitly registered, 
							 * it can still intercept uncaught events (at EventPriority.HIGHER) 
							 * and crash the game with a detailed error message.
							 */
							for (IEventListener listener : groupedListeners.get(corrector)) {
								if (listener.subscribeInfo().priority() == EventPriority.HIGHER) {
									ret.add(listener);
								}
							}
						} else {
							// Normal behavior for all other correctors
							ret.addAll(groupedListeners.get(corrector));
						}
					}
				}

				return ret;
			}
			return listeners;
		}
	}
	
}
