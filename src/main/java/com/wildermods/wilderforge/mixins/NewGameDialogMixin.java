package com.wildermods.wilderforge.mixins;

import com.llamalad7.mixinextras.sugar.Local;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.worldwalkergames.legacy.game.campaign.model.GameSettings;
import com.worldwalkergames.legacy.ui.menu.NewGameDialog;
import com.worldwalkergames.legacy.ui.menu.NewGameDialogData;

@Debug(export = true)
@Mixin(value = NewGameDialog.class, remap = false)
public class NewGameDialogMixin {
	
	private @Shadow NewGameDialogData data;
	
	@Inject(
		at = @At(
			value = "INVOKE",
			target = "Lcom/badlogic/gdx/utils/Array;add(Ljava/lang/Object;)V"
		),
		slice = @Slice(
			from = @At(
				value = "INVOKE",
				target = 
				"Lcom/worldwalkergames/legacy/ui/menu/NewGameDialogData;"
				+ "getAdditionalMods()"
				+ "Lcom/badlogic/gdx/utils/Array;"
			),
			to = @At("TAIL")
		),
		//locals = LocalCapture.PRINT,
		method = "applyGameSettings()V",
		require = 1,
		allow = 1,
		cancellable = false,
		locals = LocalCapture.PRINT
	)
	/**
	 * Makes it so that all coremods are considered locked by the game's mod API
	 * 
	 * @param c
	 */
	private void onApplyGameSettings(CallbackInfo c, @Local GameSettings.ModEntry e) {
		
	}
	
}
