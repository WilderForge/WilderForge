package com.wildermods.wilderforge.mixins.vanillafixes;

import org.spongepowered.asm.mixin.injection.At;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import com.codedisaster.steamworks.SteamPublishedFileID;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.wildermods.jrsync.RSyncPattern;
import com.wildermods.jrsync.RSyncPattern.FalsePattern;
import com.wildermods.wilderforge.api.modLoadingV1.ModDeploymentInfo;
import com.worldwalkergames.legacy.game.mods.ModInfo;
import com.worldwalkergames.logging.ALogger;
import com.worldwalkergames.scratchpad.steamWorkshop.UploadToSteamWorkshopDialog;

/**
 * Intercepts the {@link UploadToSteamWorkshopDialog#updateItem(SteamPublishedFileID)} method to introduce
 * a secure and configurable deployment step before publishing a mod to the Steam Workshop.
 * <p>
 * <strong>Background:</strong> In the original implementation of {@code updateItem}, Wildermyth automatically
 * and recursively packaged <em>all</em> files within a mod's root directory for upload to Steam Workshop.
 * This behavior unintentionally included hidden or sensitive directories such as {@code .git}, potentially
 * compromising credentials.
 * </p>
 * 
 * <p> See <a href="https://github.com/WilderForge/WilderForge/security/advisories/GHSA-hvvr-f5m5-jwjx">GHSA-hvvr-f5m5-jwjx</a>
 *
 * <p>
 * <strong>Security Fix:</strong> This mixin adds an intermediate deployment process that:
 * </p>
 * <ul>
 *   <li>Creates a temporary deployment directory at {@code ./deploy/[modid]}.</li>
 *   <li>Combines the default ignore list from {@code /assets/wilderforge/api/.deployignore} with
 *       an optional {@code .deployignore} file from the mod’s root directory.</li>
 *   <li>Recursively copies only non-ignored files from {@code ./mods/[modid]} to the deployment directory.</li>
 *   <li>Prevents inclusion of sensitive or unwanted files (e.g., {@code .git}, {@code .idea}, build artifacts, etc.).</li>
 *   <li>Passes the sanitized deployment directory to the original {@code updateItem} call for publishing.</li>
 * </ul>
 *
 * <p>
 * <strong>Process Overview:</strong>
 * </p>
 * <ol>
 *   <li>Verifies that no deployment is already in progress.</li>
 *   <li>Deletes any existing deployment directory for the same mod.</li>
 *   <li>Loads default and mod-specific ignore rules, compiling each into {@link Pattern} objects.</li>
 *   <li>Walks the original mod directory tree using {@link Files#walkFileTree(Path, java.nio.file.FileVisitor)}:</li>
 *   <ul>
 *     <li>Skips directories or files matching any ignore pattern.</li>
 *     <li>Copies all remaining files to the deployment directory, preserving relative paths.</li>
 *   </ul>
 *   <li>Invokes the original {@code updateItem} method with the sanitized deployment folder as content root.</li>
 * </ol>
 *
 * <p>
 * <strong>Failure Handling:</strong>
 * </p>
 * <ul>
 *   <li>If any {@link IOException} occurs, it is wrapped in an {@link UncheckedIOException} and rethrown.</li>
 *   <li>Invalid or unreadable ignore files cause a {@link NoSuchElementException} with clear diagnostics.</li>
 *   <li>All file tree traversal exceptions are logged through {@link ALogger} for visibility.</li>
 * </ul>
 *
 * <p>
 * <strong>Security Impact:</strong>
 * This patch mitigates CVE-style exposure described in
 * <a href="https://cwe.mitre.org/data/definitions/527.html">CWE-527</a> (Exposure of Version-Control Repository)
 * and related CWE-522 and CWE-212 issues, ensuring that version control metadata, tokens, and other sensitive
 * development files are excluded from distribution.
 * </p>
 *
 * @param publishedFieldID the {@link SteamPublishedFileID} associated with the mod being updated
 * @param original         the wrapped {@code updateItem} operation to be invoked after deployment setup
 * 
 * @throws IllegalStateException if a deployment is already in progress or stops prematurely
 * @throws UncheckedIOException  if an I/O error occurs during deployment setup or file copying
 *
 * @see Security Advisory: <a href="https://github.com/WilderForge/WilderForge/security/advisories/GHSA-hvvr-f5m5-jwjx">GHSA-hvvr-f5m5-jwjx</a>
 * @see <a href="https://cwe.mitre.org/data/definitions/527.html">CWE-527: Exposure of Version-Control Repository</a>
 * @see <a href="https://cwe.mitre.org/data/definitions/212.html">CWE-212: Improper Removal of Sensitive Information Before Storage or Transfer</a>
 * @see <a href="https://cwe.mitre.org/data/definitions/522.html">CWE-522: Insufficiently Protected Credentials</a>
 * @see UploadToSteamWorkshopDialog#updateItem(SteamPublishedFileID)
 * @see ModDeploymentInfo
 * @see Pattern
 * @see <a href="https://github.com/WilderForge/WilderForge/blob/master/src/main/resources/assets/wilderforge/api/.deployignore"> .deployignore</a>
 */

@Mixin(UploadToSteamWorkshopDialog.class)
public class SteamWorkshopUploadSecurityFixMixin {

	private @Final @Shadow ALogger LOGGER;
	private @Shadow ModInfo selectedModInfo;
	private @Unique ModDeploymentInfo deployInfo;
	
	@WrapMethod(
		method = "updateItem"
	)
	private @Unique void setupDeployment(SteamPublishedFileID publishedFieldID, Operation<Void> original) {
		LOGGER.log2("Deploying " + selectedModInfo.modId);
		if(deployInfo != null) {
			throw new IllegalStateException("Already deploying?!"); //sanity check
		}
		deployInfo = new ModDeploymentInfo(selectedModInfo);
		
		Path originalDir = deployInfo.originalFolder.file().toPath();
		
		try {
			Path deployDir = Files.createDirectories(deployInfo.folder.file().toPath());
			Path deployIgnore = originalDir.resolve(".deployignore");
			
			/*
			 * Delete deployment dir if it exists
			 */
			if(Files.exists(deployDir)) {
				Files.walkFileTree(deployDir, new FileVisitor<Path>() {

					@Override
					public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
						Files.deleteIfExists(file);
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
						LOGGER.log4("" + exc);
						return FileVisitResult.TERMINATE;
					}

					@Override
					public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
						Files.deleteIfExists(dir);
						return FileVisitResult.CONTINUE;
					}
					
				});
			}
			
			/*
			 * setup .deployignore by combining the default from wilderforge and the mod's custom .deployignore
			 */
			List<String> ignores = new ArrayList<>();
			
			try(InputStream in = getClass().getResourceAsStream("/assets/wilderforge/api/.deployignore")) {
				if(in == null) {
					throw new NoSuchElementException("/assets/wilderforge/api/.deployignore");
				}
				
				BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
				reader.lines().forEach(ignores::add);
			}
			
			if(Files.exists(deployIgnore) && Files.isRegularFile(deployIgnore)) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(deployIgnore), StandardCharsets.UTF_8));
				reader.lines().forEach(ignores::add);
			}
			else {
				LOGGER.log3("No .deployignore found for mod " + selectedModInfo.modId + ": " + deployIgnore);
			}
			
			List<RSyncPattern> ignorePatterns = new ArrayList<>();
			for(String s : ignores) {
				LOGGER.log1(s);
				RSyncPattern pattern = RSyncPattern.compile(s);
				if(!(pattern instanceof FalsePattern)) {
					ignorePatterns.add(pattern);
				}
			}
			
			/**
			 * copy the files that are not ignored to the deploy directory
			 */
			
			Files.walkFileTree(originalDir, new FileVisitor<Path>() {
				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
					for (RSyncPattern pattern : ignorePatterns) {
						if (pattern.matches(dir)) {
							LOGGER.log1("Not including directory: " + dir);
							return FileVisitResult.SKIP_SUBTREE;
						}
					}

					// Compute target directory relative to the original mod folder
					Path relative = originalDir.relativize(dir);
					Path targetDir = deployDir.resolve(relative);
					Files.createDirectories(targetDir);

					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					for (RSyncPattern pattern : ignorePatterns) {
						if (pattern.matches(file)) {
							LOGGER.log1("Not including file: " + file);
							return FileVisitResult.CONTINUE;
						}
					}

					// Compute relative path and copy to deployment folder
					Path relative = originalDir.relativize(file);
					Path target = deployDir.resolve(relative);

					Files.createDirectories(target.getParent());
					LOGGER.log1("Copying: " + file + " -> " + target);
					Files.copy(file, target);

					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
					LOGGER.log4("Failed to visit file: " + file + " (" + exc + ")");
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					if (exc != null) {
						LOGGER.log4("Error after visiting directory: " + dir + " (" + exc + ")");
					}
					return FileVisitResult.CONTINUE;
				}
			});
			
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		
		/**
		 * Call the updateItem method to publish
		 */
		original.call(publishedFieldID);
		
		if(deployInfo == null) {
			throw new IllegalStateException("Deployment stopped early?!"); //sanity check
		}
		deployInfo = null;
	}
	
	/**
	 * Redirects {@link UploadToSteamWorkshopDialog#selectedModInfo} field accesses
	 * during the deployment process to use sanitized deployment metadata.
	 * <p>
	 * <strong>Purpose:</strong> After {@link #setupDeployment(SteamPublishedFileID, Operation)} creates
	 * a temporary deployment copy of the mod in {@code ./deploy/[modid]}, this method ensures that
	 * the Steam Workshop upload logic references only to the sanitized copy rather than the original mod folder.
	 * </p>
	 *
	 * <p>
	 * This redirection is applied via {@link WrapOperation} to all field access sites within
	 * {@code updateItem}. The returned {@link ModInfo} is a {@link ModDeploymentInfo}, which points to the sanitized
	 * transparently substituting it for the original.
	 * </p>
	 *
	 * <p>
	 * <strong>Security Relevance:</strong>
	 * By substituting {@code selectedModInfo} with the sanitized deployment version, the upload system
	 * never accesses or packages the original directory structure. This ensures that excluded files
	 * (e.g., {@code .git}, development artifacts, credentials) remain private.
	 * </p>
	 *
	 * @param thiz     the {@link UploadToSteamWorkshopDialog} instance whose field access is being intercepted
	 * @param original the original field access operation
	 * @return the {@link ModDeploymentInfo} instance associated with the steam workshop upload operation
	 *
	 * @see #setupDeployment(SteamPublishedFileID, Operation)
	 * @see com.wildermods.wilderforge.api.modLoadingV1.ModDeploymentInfo
	 * @see UploadToSteamWorkshopDialog#selectedModInfo
	 */
	@WrapOperation(
		method = "updateItem",
		at = @At(
			value = "FIELD",
			target = "Lcom/worldwalkergames/scratchpad/steamWorkshop/UploadToSteamWorkshopDialog;"
						+ "selectedModInfo:Lcom/worldwalkergames/legacy/game/mods/ModInfo;"
		)
	)
	private @Unique ModInfo useDeployInfo(UploadToSteamWorkshopDialog thiz, Operation<ModInfo> original) {
		return deployInfo;
	}
	
}
