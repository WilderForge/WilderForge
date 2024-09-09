package com.wildermods.wilderforge.mixins.vanillafixes;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.wildermods.wilderforge.api.mixins.v1.Descriptor.*;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.Shadow;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.wildermods.wilderforge.api.modLoadingV1.VersionHelper;
import com.wildermods.wilderforge.launch.WilderForge;
import com.wildermods.wilderforge.launch.coremods.Coremods;
import com.wildermods.wilderforge.launch.logging.LogLevel;
import com.worldwalkergames.logging.ALogger;
import com.worldwalkergames.util.OSUtil;

import net.fabricmc.loader.api.Version;

/**
 * Patches arbitrary code execution vulnerabilities in OSUtil
 */
@Debug(export = true)
@Mixin(OSUtil.class)
public class OSUtilSecurityFixMixin {

	private static @Final @Shadow ALogger LOGGER;
	
	@ModifyVariable(
		at = @At("HEAD"),
		method = "openBrowser(" + STRING + ")" + VOID,
		require = 1
	)
	private static String patchOpenBrowser(String url) throws IOException { 
		if(shouldPatchOpenBrowser()) {
			return checkURL(url).toASCIIString();
		}
		else {
			return url;
		}
	}
	
	@WrapMethod(
		method = "openBrowser("+ STRING +")" + VOID
	)
	private static void catchOpenBrowser(String url, Operation<Void> original) {
		if(shouldPatchOpenBrowser()) {
			try {
				original.call(url);
			}
			catch(Throwable t) { //Cannot catch IOException directly, won't compile
				if(!(t instanceof IOException)) {
					throw t;
				}
				IOException e = (IOException) t;
				LOGGER.log5("unable to open a browser on {0} for url {1} with error {2}", System.getProperty("os.name"), url, e);
			}
		}
		else {
			original.call(url);
		}
	}
	
	@ModifyVariable(
		at = @At("HEAD"),
		method = "showFile(" + STRING +")" + BOOLEAN,
		require = 1
	)
	private static final String patchShowFile(String absolutePath) throws IOException {
		if(shouldPatchShowFile()) {
			return checkPath(absolutePath).toASCIIString();
		}
		else {
			return absolutePath;
		}
	}
	
	@WrapMethod(
		method = "showFile(" + STRING + ")" + BOOLEAN
	)
	private static boolean catchShowFile(String absolutePath, Operation<Boolean> original) {
		if(shouldPatchShowFile()) {
			try {
				return original.call(absolutePath);
			}
			catch(Throwable t) { //Cannot catch IOException directly, won't compile
				if(!(t instanceof IOException)) {
					throw t;
				}
				IOException e = (IOException) t;
				LOGGER.log5("unable to open a file viewer on {0} for path {1} with error {2}", System.getProperty("os.name"), absolutePath, e);
				return false;
			}
		}
		else {
			return original.call(absolutePath);
		}
	}

	/**
	 * Checks to see if a given string can represent a syntactically 
	 * valid https URI.
	 * 
	 * @param url a https url string.
	 * @throws IOException if the uri/url isn't a valid https url
	 * @return a URI representing the https url
	 */
	private static @Unique final URI checkURL(String url) throws IOException {
		URI toOpen;
		try {
			toOpen = new URI(url);
		} catch (URISyntaxException e) {
			throw new IOException(e);
		}
		String protocol = toOpen.getScheme();
		if(!"https".equals(protocol)) { //There isn't really a need to allow any other protocol...
			throw new IOException(new URISyntaxException(url, "Invalid url protocol: " + protocol));
		}
		if(toOpen.toString().contains(" && ")) { //Shouldn't be possible, but just in case:
			throw new IOException(new URISyntaxException(url, "Illegal Character Sequence"));
		}
		return toOpen;
	}
	
	/**
	 * Checks to see if a given string can represent a syntactically 
	 * valid file path URI, and that the file exists on the filesystem.
	 * 
	 * Note that the result of this method is immediately outdated. 
	 * If this method indicates the file exists, there is no 
	 * guarantee that the file still exists, only that it existed
	 * as this method was called.
	 * 
	 * @param a path string. May or may not be an absolute path.
	 * @throws IOException if the path isn't valid
	 * @return a URI representing a file that exists on the user's system
	 */
	private static @Unique final URI checkPath(String pathString) throws IOException {
		Path path = Path.of(pathString).toAbsolutePath();
		URI toOpen = path.toUri();
		String protocol = toOpen.getScheme();
		if(!"file".equals(protocol)) {
			throw new IOException(new URISyntaxException(toOpen.toString(), "Invalid file protocol: " + protocol));
		}
		
		if(!Files.exists(path)) { //nonexistant files cannot be opened
			throw new FileNotFoundException("" + path);
		}
		
		if(Files.isSymbolicLink(path)) {
			throw new IOException("Cannot open symbolic links: " + path);
		}
		
		if(toOpen.toString().contains(" && ")) { //Shouldn't be possible, but just in case:
			throw new IOException(new URISyntaxException(toOpen.toString(), "Illegal Character Sequence"));
		}

		return toOpen;

	}
	
	private static @Unique final Version patchedOpenBrowser = VersionHelper.parseVersion("1.16+549");
	
	private static @Unique final boolean shouldPatchOpenBrowser() {
		boolean patch = VersionHelper.compareVersionIncludingBuild(Coremods.getCoremod("wildermyth").getMetadata().getVersion(), patchedOpenBrowser) < 0;
		WilderForge.LOGGER.log(LogLevel.FATAL, patchedOpenBrowser);
		WilderForge.LOGGER.log(LogLevel.FATAL, Coremods.getCoremod("wildermyth").getMetadata().getVersion());
		WilderForge.LOGGER.log(VersionHelper.compareVersionIncludingBuild(Coremods.getCoremod("wildermyth").getMetadata().getVersion(), patchedOpenBrowser) < 0);
		WilderForge.LOGGER.log(LogLevel.FATAL, "Patching openBrowser(): " + patch);
		return patch;
	}

	private static @Unique final boolean shouldPatchShowFile() {
		return true;
	}
}
