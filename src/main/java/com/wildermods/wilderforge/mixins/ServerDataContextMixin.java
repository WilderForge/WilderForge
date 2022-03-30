package com.wildermods.wilderforge.mixins;

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
import com.wildermods.wilderforge.api.modLoadingV1.CoremodInfo;
import com.wildermods.wilderforge.api.modLoadingV1.MissingCoremod;
import com.wildermods.wilderforge.launch.WilderForge;
import com.wildermods.wilderforge.launch.coremods.Coremods;
import com.wildermods.wilderforge.launch.logging.Logger;
import com.worldwalkergames.legacy.game.campaign.model.GameSettings;
import com.worldwalkergames.legacy.game.campaign.model.GameSettings.ModEntry;
import com.worldwalkergames.legacy.game.mods.ModInfo;
import com.worldwalkergames.legacy.server.context.ServerDataContext;
import com.worldwalkergames.legacy.server.context.ServerDataContext.LoadingProgressFrameCallback;

@Mixin(value = ServerDataContext.class, remap = false)
public abstract class ServerDataContextMixin {

	private static final String vanillaLoader = "VanillaLoader";
	private static @Unique Logger LOGGER = new Logger(ServerDataContext.class);
	public @Shadow Files files;
	protected @Shadow Array<GameSettings.ModEntry> activeMods;
	
	
	@Inject(
		at = @At("RETURN"), method = "retrieveAllMods(ZZ)Lcom/badlogic/gdx/utils/Array;", require = 1)
	/*
	 * Lets Wildermyth's mod engine know about coremods
	 */
	private void retrieveAllMods(boolean excludeBuiltInMods, boolean excludeSteamWorkshopMods, CallbackInfoReturnable<Array<ModInfo>> c) {
		Array<ModInfo> modInfos = c.getReturnValue();
		modInfos.addAll(Coremods.getAllCoremods());
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
		WilderForge.LOGGER.info("Attempting to load coremod " + modId, vanillaLoader);
		CoremodInfo coremod = Coremods.getCoremod(modId);
		if(!(coremod instanceof MissingCoremod)) {
			WilderForge.LOGGER.info("Coremod " + modId + " is " + coremod + " " + coremod.getMetadata().getVersion(), vanillaLoader);
			WilderForge.LOGGER.info("Coremod " + coremod + " is loaded.", vanillaLoader);
			c.setReturnValue(coremod);
		}
		else {
			WilderForge.LOGGER.warn("No coremod of modid '" + modId + "' was found.", vanillaLoader);
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
			WilderForge.LOGGER.info("Requesting: " + Arrays.toString(requested.items), vanillaLoader);
			for(CoremodInfo coremod : Coremods.getAllCoremods()) {
				ModEntry entry = new ModEntry(coremod);
				if(!requested.contains(entry, false)) {
					requested.add(entry);
				}
			}
		}
		else {
			WilderForge.LOGGER.warn("Requesting NO MODS", vanillaLoader);
		}
	}
	
}
