package com.wildermods.wilderforge.api.modLoadingV1;

import java.nio.file.Path;

import com.badlogic.gdx.files.FileHandle;
import com.worldwalkergames.legacy.game.mods.ModInfo;

import com.wildermods.wilderforge.mixins.vanillafixes.SteamWorkshopUploadSecurityFixMixin;

/**
 * A wrapper around {@link ModInfo} representing a mod being deployed to a
 * sanitized deployment folder.
 * <p>
 * This class is part of the security fix for the Wildermyth in-game mod
 * publishing system.
 *
 * <p>
 * {@code ModDeploymentInfo} preserves the original mod folder in
 * {@link #originalFolder} and redefines {@link #folder} to point to
 * {@code ./deploy/[modid]}. During deployment, only files that are allowed
 * (i.e., not matched by ignore patterns in {@code .deployignore}) are copied
 * to the deployment folder. This ensures that only safe, intended content is
 * published to the Steam Workshop.
 * </p>
 *
 * @see ModInfo
 * @see SteamWorkshopUploadSecurityFixMixin
 */
public class ModDeploymentInfo extends ModInfo {

	/**
	 * The original folder where the mod resides, typically under {@code ./mods/[modid]}.
	 */
	public final transient FileHandle originalFolder;
	
	/**
	 * Constructs a new {@code ModDeploymentInfo} based on an existing {@link ModInfo}.
	 * <p>
	 * Copies all metadata fields from the given parent {@code ModInfo}, but
	 * replaces {@link #folder} with a new {@link FileHandle} pointing to
	 * {@code ./deploy/[modid]}. The original folder path is stored in
	 * {@link #originalFolder}.
	 * </p>
	 *
	 * <p>
	 * This ensures that the deployment process can safely create a sanitized
	 * copy of the mod for upload, avoiding exposure of sensitive files.
	 * </p>
	 *
	 * @param parent the mod being deployed
	 * 
	 * @throws IllegalArgumentException if the ModInfo provided is itself an instance of ModDeploymentInfo
	 */
	public ModDeploymentInfo(ModInfo parent) {
		if(parent instanceof ModDeploymentInfo) {
			throw new IllegalArgumentException("Cannot create a ModDeploymentInfo from another ModDeploymentInfo!");
		}
		this.modId = parent.modId;
		this.modSteamWorkshopId = parent.modSteamWorkshopId;
		this.originalFolder = parent.folder;
		this.folder = new FileHandle(Path.of(".").resolve("deploy").resolve(originalFolder.name()).toFile());
		this.modLocation = parent.modLocation;
		this.dataVersion = parent.dataVersion;
		this.modVersion = parent.modVersion;
		this.modVersionMinor = parent.modVersionMinor;
		this.loadOrder = parent.loadOrder;
		this.author = parent.author;
		this.name = parent.name;
		this.blurb = parent.blurb;
		this.visibility = parent.visibility;
		this.tags = parent.tags;
		this.url = parent.url;
		this.wikiNameOverride = parent.wikiNameOverride;
		this.alwaysOn = parent.alwaysOn;
		this.showInStoryDialog = parent.showInStoryDialog;
		this.showInModConfig = parent.showInModConfig;
		this.listInCredits = parent.listInCredits;
		this.customCreditLines = parent.customCreditLines;
	}
	
	
}
