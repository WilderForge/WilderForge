package com.wildermods.wilderforge.mixins;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import static org.spongepowered.asm.mixin.injection.At.Shift.BY;

import java.util.Arrays;

import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.utils.Array;

import com.wildermods.wilderforge.launch.LoadStatus;
import com.wildermods.wilderforge.launch.coremods.Coremod;
import com.wildermods.wilderforge.launch.coremods.Coremods;
import com.wildermods.wilderforge.launch.coremods.MissingCoremod;

import com.worldwalkergames.legacy.game.campaign.model.GameSettings;
import com.worldwalkergames.legacy.game.campaign.model.GameSettings.ModEntry;
import com.worldwalkergames.legacy.game.mods.ModInfo;
import com.worldwalkergames.legacy.server.context.ServerDataContext;
import com.worldwalkergames.legacy.server.context.ServerDataContext.LoadingProgressFrameCallback;

@Mixin(value = ServerDataContext.class, remap = false)
public abstract class ServerDataContextMixin {

	private static @Unique Logger LOGGER = LogManager.getLogger(ServerDataContext.class);
	public @Shadow Files files;
	protected @Shadow Array<GameSettings.ModEntry> activeMods;
	
	@Inject(
		at = @At("RETURN"), method = "retrieveAllMods(ZZ)Lcom/badlogic/gdx/utils/Array;", require = 1, cancellable = true)
	/*
	 * Lets Wildermyth's mod engine know about coremods
	 */
	private void retrieveAllMods(boolean excludeBuiltInMods, boolean excludeSteamWorkshopMods, CallbackInfoReturnable<Array<ModInfo>> c) {
		Array<ModInfo> modInfos = c.getReturnValue();
		for(Coremod coremod : Coremods.getCoremodsByStatus(LoadStatus.LOADED)) {
			modInfos.add(coremod.getCoremodInfo());
		}
		c.setReturnValue(modInfos);
	}
	
	@Inject(
			at = @At(
				value = "HEAD"
			), 
			method = "loadModInfo("
				+ "Ljava/lang/String;"
				+ "Z"
			+ ")Lcom/worldwalkergames/legacy/game/mods/ModInfo;",
			require = 1)
	/*
	 * Always log if any mods or coremods are missing
	 */
	private void loadModInfoHead(String modId, boolean logIfMissing, CallbackInfoReturnable<ModInfo> c) {
		logIfMissing = true;
	}
	
	@Inject(
			at = @At(
				value = "TAIL", 
				shift = BY,
				by = -5
			), 
			method = "loadModInfo("
				+ "Ljava/lang/String;"
				+ "Z"
			+ ")Lcom/worldwalkergames/legacy/game/mods/ModInfo;",
			require = 1,
			cancellable = true)
	/*
	 * Lets Wildermyth load resources from coremods
	 */
	private void loadModInfoTail(String modId, boolean logIfMissing, CallbackInfoReturnable<ModInfo> c) {
		Coremod coremod = Coremods.getCoremod(modId, true);
		if(!(coremod instanceof MissingCoremod)) {
			LoadStatus loadStatus = Coremods.getStatus(coremod);
			if(loadStatus == LoadStatus.LOADED) {
				LOGGER.info("Coremod " + coremod + " is loaded.");
				c.setReturnValue(coremod.getCoremodInfo());
			}
			else {
				LOGGER.warn("Coremod " + coremod + " is not loaded. Its status is (" + loadStatus + ")");
			}
		}
		else {
			LOGGER.warn("No coremod of modid '" + modId + "' was found.");
		}
	}
	
	@Inject(
		at = @At(
			value = "HEAD"
		),
		method = "applyGameSettings("
			+ "Lcom/badlogic/gdx/utils/Array;"
			+ "Lcom/worldwalkergames/legacy/server/context/ServerDataContext$LoadingProgressFrameCallback;"
		+ ")V",
		require = 1
		
	)
	/*
	 * Adds coremods to campaigns
	 */
	private synchronized void applyGameSettings(Array<ModEntry> requested, LoadingProgressFrameCallback progressFrameCallback, CallbackInfo c) {
		if(requested != null) {
			LOGGER.info("Requesting: " + Arrays.toString(requested.items));
			for(Coremod coremod : Coremods.getCoremodsByStatus(LoadStatus.LOADED)) {
				ModEntry entry = new ModEntry(coremod.getCoremodInfo());
				if(!requested.contains(entry, false)) {
					requested.add(entry);
				}
			}
		}
		else {
			LOGGER.warn("Requesting NO MODS");
		}
	}
	
}
