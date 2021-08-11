package com.wildermods.wilderforge.launch.services;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;

import cpw.mods.gross.Java9ClassLoaderUtil;
import cpw.mods.modlauncher.TransformingClassLoader;
import cpw.mods.modlauncher.api.ILaunchHandlerService;
import cpw.mods.modlauncher.api.ITransformingClassLoader;
import cpw.mods.modlauncher.api.ITransformingClassLoaderBuilder;

public class WilderLauncherService implements ILaunchHandlerService {

	public WilderLauncherService() {

	}
	
	@Override
	public String name() {
		return "wilderforge";
	}

	@Override
	public void configureTransformationClassLoader(ITransformingClassLoaderBuilder builder) {
		grossClassTransformerHackery(); //gross classtransformer hack, THIS COULD BREAK IN FUTURE MODLAUNCHER RELEASES
		for (final URL url : Java9ClassLoaderUtil.getSystemClassPathURLs()) {
			try {
				System.out.println(url);
				builder.addTransformationPath(new File(url.toURI()).toPath());
			} catch (Throwable t) {
				throw new IOError(new Error("Could not start WilderLauncher service!", t));
			}
		}
		for(final URI uri : getJarsInModsFolder()) {
			builder.addTransformationPath(new File(uri).toPath());
		}
	}

	@Override
	public Callable<Void> launchService(String[] arguments, ITransformingClassLoader launchClassLoader) {
		return () -> {
			Class<?> mainClass = launchClassLoader.getInstance().loadClass("com.wildermods.wilderforge.launch.Main");
			final Method mainMethod = mainClass.getMethod("main", String[].class);
			mainMethod.invoke(null, new Object[] {arguments});
			return null;
		};
	}
	
	private HashSet<URI> getJarsInModsFolder() {
		HashSet<URI> uris = new HashSet<URI>();
		Collection<File> modFiles = FileUtils.listFiles(new File("./mods"), new String[] {".jar"}, false);
		for(File f : modFiles) {
			if(f.isDirectory()) {
				System.out.println("Ignoring directory " + f);
			}
			try {
				JarURLConnection jarConnection = (JarURLConnection) f.toURI().toURL().openConnection();
				JarFile jar = jarConnection.getJarFile();
				Enumeration<JarEntry> entries = jar.entries();
				boolean hasModJson = false;
				while(entries.hasMoreElements()) {
					JarEntry e = entries.nextElement();
					if(e.getName().equals("mod.json")) {
						hasModJson = true;
						break;
					}
				}
				if(hasModJson) {
					uris.add(f.toURI());
				}
				else {
					System.out.println("No mod.json detected in " + f);
				}
			} catch (IOException e) {
				System.out.println("Could not load " + f);
				e.printStackTrace();
			}
		}
		return uris;
	}
	
	@SuppressWarnings("unchecked")
	private void grossClassTransformerHackery() {
		try {
			Field skipPrefixField = TransformingClassLoader.class.getDeclaredField("SKIP_PACKAGE_PREFIXES");
			skipPrefixField.setAccessible(true);
			List<String> disallowedPrefixList = (List<String>) skipPrefixField.get(null);
			Field listArray = disallowedPrefixList.getClass().getDeclaredField("a");
			listArray.setAccessible(true);
			String[] disallowedPrefixes = (String[]) listArray.get(disallowedPrefixList);
			disallowedPrefixes = ArrayUtils.remove(disallowedPrefixes, 1);
			listArray.set(disallowedPrefixList, disallowedPrefixes);
			System.out.println(Arrays.toString(disallowedPrefixList.toArray()));
			if(disallowedPrefixList.contains(".javax")) {
				throw new LinkageError("Modlauncher probably changed SKIP_PACKAGE_PREFIXES", new IllegalStateException("Removal of \".javax\" from disallowed packages failed."));
			}
		}
		catch(NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			throw new AssertionError(e);
		}
	}
	
}
