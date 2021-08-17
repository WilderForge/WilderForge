package com.wildermods.wilderforge.launch;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager.Log4jMarker;

import com.wildermods.wilderforge.api.event.bus.EventBus;
import com.wildermods.wilderforge.api.event.launch.PreInitializationEvent;
import com.wildermods.wilderforge.launch.exception.CoremodNotFoundError;
import com.wildermods.wilderforge.launch.exception.DuplicateCoremodError;

import com.worldwalkergames.legacy.LegacyDesktop;
import com.worldwalkergames.legacy.Version;

public class Main {
	public static final Logger LOGGER = LogManager.getLogger(Main.class);
	private static ReflectionsHelper reflectionsHelper;
	public static final EventBus EVENT_BUS = new EventBus();

	public static void main(String[] args) throws IOException {
		ClassLoader loader = checkClassloader();
		
		setupReflectionsHelper(loader);
		
		setupEventBusses(loader);
		
		discoverCoremodJsons(loader);
		
		validateCoremods(loader);
		
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
	
	private static final void discoverCoremodJsons(ClassLoader classLoader) throws IOException{

		try {
			Field jarField = classLoader.getClass().getDeclaredField("specialJars");
			jarField.setAccessible(true);
			URL[] jarLocs = (URL[]) jarField.get(classLoader);
			LoadableCoremod.dependencyGraph.addVertex(new Wildermyth());
			LoadableCoremod.dependencyGraph.addVertex(new WilderForge());
			for(URL url : jarLocs) {
				if(url.toString().contains("/mods/")) {
					try {
						URL url2 = new URL("jar:" + url.toString() + "!/");
						LOGGER.debug("Opening " + url2);
						URLConnection urlConnection = url2.openConnection();
						Main.LOGGER.info(urlConnection);
						if(urlConnection instanceof JarURLConnection) {
							JarCoremod jarCoremod = new JarCoremod((JarURLConnection)urlConnection);
							for(Coremod coremod : LoadableCoremod.dependencyGraph.vertexSet()) {
								if(coremod instanceof Dependency) {
									continue; //Instances of dependency are coremods which have been declared, but have not been found yet
								}
								else {
									if(coremod.equals(jarCoremod)) {
										throw new DuplicateCoremodError(coremod);
									}
								}
							}
							LoadableCoremod.dependencyGraph.edgesOf(jarCoremod);
							LoadableCoremod.dependencyGraph.addVertex(new JarCoremod((JarURLConnection)urlConnection));

						}
						else {
							LOGGER.error(url2 + " is not a JarURLConnection. (" + urlConnection.getClass().getName() + ")");
						}
					} catch (IOException e) {
						LOGGER.catching(e);
					}
				}
				else {
					LOGGER.debug("Skipping non-mod url: " + url);
				}
			}
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			throw new AssertionError(e);
		}
	}
	
	private static final void validateCoremods(ClassLoader classLoader) {
		Logger logger = LogManager.getLogger("Coremod Validator");
		for(Coremod coremod : LoadableCoremod.dependencyGraph.vertexSet()) {
			for(LoadableCoremod.DependencyEdge edge : LoadableCoremod.dependencyGraph.outgoingEdgesOf(coremod)) {
				Log4jMarker marker = new Log4jMarker(coremod.value());
				Coremod dep = LoadableCoremod.dependencyGraph.getEdgeTarget(edge);
				if(edge.isRequired()) {
					if(dep instanceof LoadableCoremod) {
						logger.info(marker, "Found required dependency " + dep.value());
					}
					else {
						System.out.println(dep.getClass().getName());
						throw new CoremodNotFoundError(coremod, dep);
					}
				}
				else {
					if(dep instanceof LoadableCoremod) {
						logger.info(marker, "Found optional dependency " + dep.value());
					}
					else {
						logger.info(marker, "Did not find optional dependency " + dep.value());
					}
				}
			}
		}
		Set<Class<?>> classes = reflectionsHelper.getAllClassesAnnotatedWith(com.wildermods.wilderforge.api.Coremod.class);
		Main.LOGGER.info("Found " + classes.size() + " classes annotated with @Coremod:");
		for(Class<?> clazz : classes) {
			Main.LOGGER.info("@Coremod(" + clazz.getAnnotation(com.wildermods.wilderforge.api.Coremod.class).value() + ") is " + clazz.getCanonicalName());
		}
	}
	
	private static final void launchGame(String[] args) {
		LegacyDesktop.main(args);
	}
	
	public static ReflectionsHelper getReflectionsHelper() {
		return reflectionsHelper;
	}
	
}
