package com.wildermods.wilderforge.launch;

import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.wildermods.wilderforge.api.eventV1.bus.EventBus;
import com.wildermods.wilderforge.api.eventV1.launch.PreInitializationEvent;
import com.worldwalkergames.legacy.LegacyDesktop;
import com.worldwalkergames.legacy.Version;

public final class Main {
	public static final Logger LOGGER = LogManager.getLogger(Main.class);
	static ReflectionsHelper reflectionsHelper;
	public static final EventBus EVENT_BUS = new EventBus();

	public static void main(String[] args) throws IOException {
		ClassLoader loader = checkClassloader();
		
		setupReflectionsHelper(loader);
		
		setupEventBusses(loader);
		
		loadCoremods(loader);
		
		EVENT_BUS.fire(new PreInitializationEvent());
		launchGame(args);
	}
	
	private static final ClassLoader checkClassloader() throws VerifyError {
		ClassLoader classloader = LegacyDesktop.class.getClassLoader();
		if(!(classloader.getClass().getName().equals("cpw.mods.modlauncher.TransformingClassLoader"))) { //Do not use instanceof or cast the classLoader, instanceof will always return false, and you will get ClassCastExceptions due to differing classLoaders
			System.out.println(LegacyDesktop.class.getClassLoader().getClass().getName());
			throw new VerifyError("Incorrect classloader. Mixins are not loaded. " + LegacyDesktop.class.getClassLoader());
		}
		System.out.println("Correct classloader detected.");
		Version.PATCHLINE = "WilderForge 0.0.0.0";
		return classloader;
	}
	
	private static final void setupReflectionsHelper(ClassLoader classLoader) {
		reflectionsHelper = new ReflectionsHelper(classLoader);
	}
	
	private static final void setupEventBusses(ClassLoader classLoader) {
		
	}
	
	private static final void loadCoremods(ClassLoader classLoader) {
		Coremods.loadCoremods(classLoader);
	}
	
	private static final void launchGame(String[] args) {
		LegacyDesktop.main(args);
	}
	
	public static ReflectionsHelper getReflectionsHelper() {
		return reflectionsHelper;
	}
	
}
