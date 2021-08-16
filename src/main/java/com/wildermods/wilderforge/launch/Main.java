package com.wildermods.wilderforge.launch;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.wildermods.wilderforge.launch.coremods.WilderForge;
import com.wildermods.wilderforge.launch.coremods.Wildermyth;
import com.wildermods.wilderforge.launch.exception.DuplicateCoremodException;

import com.worldwalkergames.legacy.LegacyDesktop;
import com.worldwalkergames.legacy.Version;

public class Main {
	public static final Logger LOGGER = LogManager.getLogger(Main.class);
	private static ReflectionsHelper reflectionsHelper;

	public static void main(String[] args) throws IOException {
		checkClassloader();
		
		discoverCoremods();
		
		//do modloading stuff
		
		launchGame(args);
	}
	
	private static final void checkClassloader() throws VerifyError {
		ClassLoader classloader = LegacyDesktop.class.getClassLoader();
		if(!(classloader.getClass().getName().equals("cpw.mods.modlauncher.TransformingClassLoader"))) {
			System.out.println(LegacyDesktop.class.getClassLoader().getClass().getName());
			throw new VerifyError("Incorrect classloader. Mixins are not loaded. " + LegacyDesktop.class.getClassLoader());
		}
		System.out.println("Correct classloader detected.");
		Version.PATCHLINE = "WilderForge 0.0.0.0";
	}
	
	private static final void discoverCoremods() throws IOException {
		ClassLoader classLoader = Main.class.getClassLoader();
		
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
										throw new DuplicateCoremodException(coremod);
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
	
	private static final void launchGame(String[] args) {
		LegacyDesktop.main(args);
	}
	
	public static ReflectionsHelper getReflectionsHelper() {
		return reflectionsHelper;
	}
	
}
