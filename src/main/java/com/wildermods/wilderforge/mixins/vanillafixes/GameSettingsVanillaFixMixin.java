package com.wildermods.wilderforge.mixins.vanillafixes;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.worldwalkergames.engine.EntitiesCollection;
import com.worldwalkergames.legacy.game.campaign.model.GameSettings;

@Mixin(GameSettings.class)
public class GameSettingsVanillaFixMixin {

	/**
	 * Fixes a vanilla issue where {@link GameSettings.getPerilAndOdesEnabled} would not check if the scenario
	 * info in the provided EntitiesCollection was null before attempting to access fields within the scenarioinfo
	 * 
	 * @param entities The entity collection
	 * @param c
	 */
	@Inject(
		at = @At("HEAD"),
		method = "getPerilAndOdesEnabled(Lcom/worldwalkergames/engine/EntitiesCollection;)Z",
		cancellable = true,
		require = 1
	)
	private static void fixGetPerilAndOdesEnabled(EntitiesCollection entities, CallbackInfoReturnable<Boolean> c) {
		if(GameSettings.getScenarioInfo(entities) == null) {
			c.setReturnValue(false); //if no entities collection in the scenario, return false
		}
		//otherwise continue with the vanilla implementation
	}
	
}
