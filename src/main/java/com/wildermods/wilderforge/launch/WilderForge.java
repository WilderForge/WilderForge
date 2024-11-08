package com.wildermods.wilderforge.launch;

import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import com.wildermods.provider.services.CrashLogService;
import com.wildermods.wilderforge.api.eventV1.bus.EventBus;
import com.wildermods.wilderforge.api.eventV1.bus.EventPriority;
import com.wildermods.wilderforge.api.eventV1.bus.SubscribeEvent;
import com.wildermods.wilderforge.api.mixins.v1.Cast;
import com.wildermods.wilderforge.api.modLoadingV1.CoremodInfo;
import com.wildermods.wilderforge.api.modLoadingV1.Mod;
import com.wildermods.wilderforge.api.modLoadingV1.event.PostInitializationEvent;
import com.wildermods.wilderforge.api.modLoadingV1.event.PreInitializationEvent;
import com.wildermods.wilderforge.api.netV1.client.ClientMessageEvent;
import com.wildermods.wilderforge.api.netV1.server.ServerBirthEvent;
import com.wildermods.wilderforge.api.netV1.server.ServerDeathEvent;
import com.wildermods.wilderforge.api.netV1.server.ServerEvent;
import com.wildermods.wilderforge.launch.coremods.Coremods;
import com.wildermods.wilderforge.launch.logging.CrashInfo;
import com.wildermods.wilderforge.launch.logging.Logger;

import com.worldwalkergames.legacy.context.LegacyViewDependencies;
import com.worldwalkergames.legacy.control.ClientControl;
import com.worldwalkergames.legacy.control.HostInfo;
import com.worldwalkergames.legacy.ui.MainScreen;

import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.CustomValue.CvArray;
import net.fabricmc.loader.api.metadata.CustomValue.CvType;

@Mod(modid = "wilderforge", version = "@WILDERFORGE_VERSION@")
public final class WilderForge {
	
	@InternalOnly
	public static final Logger LOGGER = new Logger("WilderForge");
	
	@InternalOnly
	private static ReflectionsHelper reflectionsHelper;
	
	@InternalOnly
	private static LegacyViewDependencies dependencies;
	
	@InternalOnly
	private static HostInfo host;
	
	@InternalOnly
	private static MainScreen mainScreen;
	
	@InternalOnly
	private static ClientControl clientControl;
	
	public static final EventBus MAIN_BUS = new EventBus("MAIN");
	public static final EventBus NETWORK_BUS = new EventBus("NETWORK");
	public static final EventBus RENDER_BUS = new EventBus("RENDER");
	
	@InternalOnly
	public static ReflectionsHelper getReflectionsHelper() {
		if(reflectionsHelper == null) {
			reflectionsHelper = new ReflectionsHelper(WilderForge.class.getClassLoader());
		}
		return reflectionsHelper;
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
					throw new Error("Error reading custom values for mod " + coremod.name + " (" + coremod.modId + ")");
				}
			}
		}
		catch(Throwable t) {
			throw new Error("Error prepping mods for PreInitialization sequence!");
		}
		
		Set<Class<?>> modClasses = getReflectionsHelper().getAllClassesAnnotatedWith(Mod.class);
		
		for(Class<?> clazz : modClasses) {
			Mod mod = getReflectionsHelper().getAnnotation(Mod.class, clazz);
			WilderForge.LOGGER.log("Registered " + clazz + " in the MAIN_BUS via annotation scanning.", mod.modid());
			MAIN_BUS.register(clazz);
		}
		
		NETWORK_BUS.register(WilderForge.class);
		RENDER_BUS.register(WilderForge.class);
		
		MAIN_BUS.fire(new PreInitializationEvent());
		
		if(WilderForge.dependencies == null) {
			WilderForge.dependencies = dependencies;
		}
		else {
			throw new IllegalStateException();
		}

		
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
					c.doThreadDump(true);
				}
				String message = "Manually Triggered Debug Crash";
				if(c != null && c.isDumpingThreads()) {
					message = message + " With Thread Dump Enabled";
				}
				message = message + " CTRL + ALT + SHIFT +";
				message = message + ((c == null || !c.isDumpingThreads()) ? " C)" : " F1 + C)");
				throw new Error(message);
			}
		});
		
		MAIN_BUS.fire(new PostInitializationEvent());
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
	
	@SubscribeEvent(priority = EventPriority.LOW)
	public static void onServerEvent(ServerEvent e) {
		if(e instanceof ServerBirthEvent) {
			initServer(e.getHost());
		}
		else if (e instanceof ServerDeathEvent) {
			killServer(e.getHost());
		}
	}
	
	@SubscribeEvent(priority = EventPriority.LOW)
	public static void onUnhandledClientMessage(ClientMessageEvent.PostVanillaChecks e) {
		if(e.getMessage().to.match("wilderforge.event.cancelled")) {
			e.getClient().setWaiting(false);
			e.setHandled();
		}
	}
	
	public static LegacyViewDependencies getViewDependencies() {
		return dependencies;
	}
	
}
