package com.wildermods.wilderforge.launch;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.function.Function;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import com.wildermods.provider.services.CrashLogService;
import com.wildermods.wilderforge.launch.WilderForge.WildermythOptions;
import com.wildermods.wilderforge.api.mixins.v1.Cast;
import com.wildermods.wilderforge.api.modLoadingV1.CoremodInfo;
import com.wildermods.wilderforge.api.modLoadingV1.Mod;
import com.wildermods.wilderforge.api.modLoadingV1.config.BadConfigValueEvent.ConfigValueOutOfRangeEvent;
import com.wildermods.wilderforge.api.modLoadingV1.config.BadConfigValueEvent.MissingConfigValueEvent;
import com.wildermods.wilderforge.api.modLoadingV1.config.ConfigEntry.Range.DecimalRange;
import com.wildermods.wilderforge.api.modLoadingV1.config.ConfigEntry.Range.IntegralRange;
import com.wildermods.wilderforge.api.modLoadingV1.config.ConfigEntry.Range.RangeInstance;
import com.wildermods.wilderforge.api.modLoadingV1.config.ConfigEntry.Range.Ranges;
import com.wildermods.wilderforge.api.modLoadingV1.config.ConfigEntry.ValueCorrectors;
import com.wildermods.wilderforge.api.modLoadingV1.config.CustomConfig;
import com.wildermods.wilderforge.api.modLoadingV1.event.PostInitializationEvent;
import com.wildermods.wilderforge.api.modLoadingV1.event.PreInitializationEvent;

import com.wildermods.wilderforge.launch.coremods.Configuration;
import com.wildermods.wilderforge.launch.coremods.Coremods;
import com.wildermods.wilderforge.launch.exception.ConfigurationError;
import com.wildermods.wilderforge.launch.logging.CrashInfo;
import com.wildermods.wilderforge.launch.logging.Logger;
import com.worldwalkergames.legacy.LegacyDesktop;
import com.worldwalkergames.legacy.context.LegacyViewDependencies;
import com.worldwalkergames.legacy.control.ClientControl;
import com.worldwalkergames.legacy.control.HostInfo;
import com.worldwalkergames.legacy.ui.MainScreen;
import com.worldwalkergames.legacy.ui.menu.OptionsDialog;

import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.CustomValue.CvArray;
import net.fabricmc.loader.api.metadata.CustomValue.CvType;
import net.minecraftforge.eventbus.api.BusBuilder;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Mod(modid = WilderForge.modid, version = "@wilderForgeVersion@")
@CustomConfig(modid = "wildermyth", popup = WildermythOptions.class)
public final class WilderForge {
	
	@InternalOnly
	public static final Logger LOGGER = new Logger("WilderForge");
	
	public static final String modid = "wilderforge";
	
	@InternalOnly
	private static ReflectionsHelper reflectionsHelper;
	
	@InternalOnly
	private static LegacyDesktop mainApp;
	
	@InternalOnly
	private static LegacyViewDependencies dependencies;
	
	@InternalOnly
	private static HostInfo host;
	
	@InternalOnly
	private static MainScreen mainScreen;
	
	@InternalOnly
	private static ClientControl clientControl;
	
	public static final IEventBus MAIN_BUS = BusBuilder.builder().build();
	public static final IEventBus NETWORK_BUS = BusBuilder.builder().build();
	public static final IEventBus RENDER_BUS = BusBuilder.builder().build();
	
	@InternalOnly
	public static ReflectionsHelper getReflectionsHelper() {
		if(reflectionsHelper == null) {
			reflectionsHelper = new ReflectionsHelper(WilderForge.class.getClassLoader());
		}
		return reflectionsHelper;
	}
	
	@InternalOnly
	public static void setup(LegacyDesktop mainApp) {
		WilderForge.mainApp = mainApp;
	}

	@InternalOnly
	public static void init(LegacyViewDependencies dependencies) {
		
		try {
			for(CoremodInfo coremod : Coremods.getAllCoremods()) {
				try {
					CustomValue val = coremod.getMetadata().getCustomValue("annotatedClasses");
					if(val == null) {
						LOGGER.warn(coremod + " has no annotatedClasses");
						continue;
					}
					String[] classes;
					if(val.getType() == CvType.STRING) {
						classes = new String[] {val.getAsString()};
					}
					else if(val.getType() == CvType.ARRAY) {
						CvArray values = val.getAsArray();
						classes = new String[values.size()];
						for(int i = 0; i < classes.length; i++) {
							CustomValue arrayVal = values.get(i);
							if(arrayVal.getType() == CvType.STRING) {
								classes[i] = arrayVal.getAsString();
							}
							else {
								throw new IllegalArgumentException("Value in annotatedClasses array is not a String! Actual type: " + arrayVal.getType().name());
							}
						}
					}
					else {
						throw new IllegalArgumentException("annotatedClasses must be a String or array of strings! Actual type: " + val.getType().name());
					}
					for(String clazz : classes) {
						WilderForge.class.getClassLoader().loadClass(clazz);
					}
				}
				catch(Throwable t) {
					throw new Error("Error reading custom values for mod " + coremod.name + " (" + coremod.modId + ")", t);
				}
			}
		}
		catch(Throwable t) {
			throw new Error("Error prepping mods for PreInitialization sequence!", t);
		}
		
		Set<Class<?>> modClasses = getReflectionsHelper().getAllClassesAnnotatedWith(Mod.class);
		
		for(Class<?> clazz : modClasses) {
			Mod mod = getReflectionsHelper().getAnnotation(Mod.class, clazz);
			WilderForge.LOGGER.log("Registered " + clazz + " in the MAIN_BUS via annotation scanning.", mod.modid());
			MAIN_BUS.register(clazz);
		}
		
		NETWORK_BUS.register(WilderForge.class);
		RENDER_BUS.register(WilderForge.class);
		
		MAIN_BUS.fire(new PreInitializationEvent(mainApp, dependencies));
		
		if(WilderForge.dependencies == null) {
			WilderForge.dependencies = dependencies;
		}
		else {
			throw new IllegalStateException();
		}
		
		Configuration.initializeConfigurations();

		
		dependencies.globalInputProcessor.anyKeyDown.add(WilderForge.class, () -> {
			if(
				Gdx.input.isKeyJustPressed(Input.Keys.C)
				&& (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT))
				&& (Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.ALT_RIGHT))
			) {
				CrashLogService crashService = CrashLogService.obtain();
				CrashInfo c = null;
				if(crashService instanceof CrashInfo) {
					c =  Cast.from(crashService);
				}
				if(Gdx.input.isKeyPressed(Input.Keys.F1) && c != null) {
					c.initializeThreadDump();
				}
				String message = "Manually Triggered Debug Crash";
				if(c != null && c.isDumpingThreads()) {
					message = message + " With Thread Dump Enabled";
				}
				message = message + " (CTRL + ALT + SHIFT +";
				message = message + ((c == null || !c.isDumpingThreads()) ? " C)" : " F1 + C)");
				throw new Error(message);
			}
		});
		
		MAIN_BUS.fire(new PostInitializationEvent(mainApp, dependencies));
	}
	
	@InternalOnly
	public static void setMainScreen(MainScreen ui) {
		if(mainScreen != null) {
			throw new IllegalStateException("Main Screen is already set!");
		}
		if(ui == null) {
			throw new IllegalArgumentException("Main Screen instance cannot be null.", new NullPointerException());
		}
		mainScreen = ui;
		clientControl = (ClientControl) ((ClientContexted)(Object)ui).getControl();
	}
	
	public static ClientControl getClientControl() {
		return clientControl;
	}
	
	@InternalOnly
	private static void initServer(HostInfo host) {
		if(WilderForge.host == null) {
			WilderForge.host = host;
		}
		else {
			throw new IllegalStateException("Host initialized twice?! This shouldn't happen!");
		}
	}
	
	@InternalOnly
	private static void killServer(HostInfo host) {
		if(WilderForge.host != host) {
			throw new IllegalStateException("Host mismatch?!");
		}
		WilderForge.host = null;
	}
	
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onPreInit(PreInitializationEvent e) {
		LOGGER.fatal("PRE-INIT");
	}
	
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onConfigValueOutOfRange(ConfigValueOutOfRangeEvent e) {
		if(ValueCorrectors.of(e.getConfigEntry()).contains(modid)) {
			RangeInstance range = e.getRange();
			
			Number minimum = Ranges.getMinimum(range);
			Number maximum = Ranges.getMaximum(range);
			
			if(range.contains(e.getValue())) {
				return;
			}
			
			if(range instanceof IntegralRange) {
				e.setValue(MathUtils.clamp(e.getValue().doubleValue(), minimum.doubleValue(), maximum.doubleValue()));
			}
			else if(range instanceof DecimalRange) {
				e.setValue(MathUtils.clamp(e.getValue().longValue(), minimum.longValue(), maximum.longValue()));
			}
			else {
				throw new AssertionError();
			}
		}
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHER)
	public static void onConfigValueStillOutOfRange(ConfigValueOutOfRangeEvent e) {
		RangeInstance range = e.getRange();
		
		Number minimum = Ranges.getMinimum(range);
		Number maximum = Ranges.getMaximum(range);
		
		if(range.contains(e.getValue())) {
			return;
		}
		
		throw new ConfigurationError(e.getValue() + " is out of range for field " + e.getFieldToSet().getName() + " in mod configuration for " + e.getConfigAnnotation().modid() + ". Range is " + minimum + " to " + maximum + ", inclusive.");
	}
	
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onConfigValueMissing(MissingConfigValueEvent e) {
		if(e.getValue() == null && ValueCorrectors.of(e.getConfigEntry()).contains(modid)) {
			Field field = e.getFieldToSet();
			try {
				Object defaultConfig = Configuration.getDefaultConfig(e.getConfigAnnotation());
				if(defaultConfig == null) {
					throw new AssertionError();
				}
				Object defaultValue = e.getFieldToSet().get(Configuration.getDefaultConfig(e.getConfigAnnotation()));
				e.setValue(defaultValue);
			} catch (Throwable t) {
				throw new ConfigurationError("Value " + field.getName() + " in mod " + e.getConfigAnnotation().modid() + " was not set, and wilderforge could not set it to the default value.", t);
			}
		}
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHER)
	public static void onConfigValueStillMissing(MissingConfigValueEvent e) {
		if(e.getValue() == null) {
			throw new ConfigurationError("Value " + e.getFieldToSet().getName() + " in mod " + e.getConfigAnnotation().modid() + " is not set but is required to be set.");
		}
	}
	
	@InternalOnly
	public static LegacyViewDependencies getViewDependencies() {
		return dependencies;
	}
	
	static final class WildermythOptions implements Function<LegacyViewDependencies, OptionsDialog> {
		
		@Override
		public OptionsDialog apply(LegacyViewDependencies t) {
			return new OptionsDialog(t);
		}
		
	}
	
}
