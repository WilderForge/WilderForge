package com.wildermods.wilderforge.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import java.util.Arrays;

import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.utils.Array;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.wildermods.wilderforge.api.modLoadingV1.CoremodInfo;
import com.wildermods.wilderforge.api.modLoadingV1.MissingCoremod;
import static com.wildermods.wilderforge.api.mixins.v1.Descriptor.*;
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
		at = @At("RETURN"), method = "retrieveAllMods("+ BOOLEAN + BOOLEAN +")" + GDX_ARRAY, require = 1)
	/*
	 * Lets Wildermyth's mod engine know about coremods
	 */
	private void retrieveAllMods(boolean excludeBuiltInMods, boolean excludeSteamWorkshopMods, CallbackInfoReturnable<Array<ModInfo>> c) {
		Array<ModInfo> modInfos = c.getReturnValue();
		modInfos.addAll(Coremods.getAllCoremods());
	}
	
	@WrapMethod(
		method = "loadModInfo",
		require = 1
	)
	/*
	 * Lets Wildermyth load resources from coremods
	 */
	private ModInfo loadModInfo(String modId, boolean logIfMissing, Operation<ModInfo> original) {
		ModInfo found = original.call(modId, false);
		if(found == null) {
			WilderForge.LOGGER.info("Attempting to load coremod " + modId, vanillaLoader);
			CoremodInfo coremod = Coremods.getCoremod(modId);
			if(!(coremod instanceof MissingCoremod)) {
				WilderForge.LOGGER.info("Coremod " + modId + " is " + coremod + " " + coremod.getMetadata().getVersion(), vanillaLoader);
				WilderForge.LOGGER.info("Coremod " + coremod + " is loaded.", vanillaLoader);
				found = coremod;
			}
			else {
				WilderForge.LOGGER.warn("No coremod of modid '" + modId + "' was found.", vanillaLoader);
			}
		}
		if(found == null) {
			WilderForge.LOGGER.warn("unable to load mod " + modDebugName(modId), vanillaLoader);
		}
		return found;
	}
	
	@Inject(
		at = @At(
			value = "HEAD"
		),
		method = "applyGameSettings("
			+ GDX_ARRAY
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
	
	private @Shadow String modDebugName(String modid) {return null;};
	
}
