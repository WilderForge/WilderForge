package com.wildermods.wilderforge.launch;

import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.wildermods.wilderforge.launch.WilderForge.EVENT_BUS;
import com.wildermods.wilderforge.api.modLoadingV1.event.PreInitializationEvent;
import com.worldwalkergames.legacy.LegacyDesktop;
import com.worldwalkergames.legacy.Version;

@InternalOnly
public final class Main {
	public static final Logger LOGGER = LogManager.getLogger(Main.class);
	static ReflectionsHelper reflectionsHelper;

	public static void main(String[] args) throws IOException {
		ClassLoader loader = checkClassloader();
		
		setupReflectionsHelper(loader);
		
		loadCoremods(loader);
		
		EVENT_BUS.fire(new PreInitializationEvent());
		launchGame(args);
	}
	
	private static final ClassLoader checkClassloader() throws VerifyError {
		ClassLoader classloader = LegacyDesktop.class.getClassLoader();
		if(!(classloader.getClass().getName().equals("cpw.mods.modlauncher.TransformingClassLoader"))) { //Do not use instanceof or cast the classLoader, instanceof will always return false, and you will get ClassCastExceptions due to differing classLoaders
			Main.LOGGER.info("Classloader: " + LegacyDesktop.class.getClassLoader().getClass().getName());
			throw new VerifyError("Incorrect classloader. Mixins are not loaded. " + LegacyDesktop.class.getClassLoader());
		}
		Main.LOGGER.info("Correct classloader detected.");
		Version.PATCHLINE = "WilderForge 0.0.0.0";
		return classloader;
	}
	
	private static final void setupReflectionsHelper(ClassLoader classLoader) {
		reflectionsHelper = new ReflectionsHelper(classLoader);
	}
	
	private static final void loadCoremods(ClassLoader classLoader) {
		Coremods.loadCoremods(classLoader);
	}
	
	private static final void launchGame(String[] args) {
		LegacyDesktop.main(args);
	}
	
	@InternalOnly
	public static ReflectionsHelper getReflectionsHelper() {
		return reflectionsHelper;
	}
	
}
