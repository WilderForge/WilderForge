package com.wildermods.wilderforge.mixins;

import com.llamalad7.mixinextras.sugar.Local;

import static com.wildermods.wilderforge.api.mixins.v1.Descriptor.*;
import com.wildermods.wilderforge.api.modLoadingV1.CoremodInfo;
import com.wildermods.wilderforge.launch.logging.Logger;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.worldwalkergames.legacy.game.campaign.model.GameSettings;
import com.worldwalkergames.legacy.ui.menu.NewGameDialog;

@Debug(export = true)
@Mixin(value = NewGameDialog.class, remap = false)
public class NewGameDialogMixin {
	
	private static @Unique Logger LOGGER = new Logger(NewGameDialog.class);
	private @Shadow GameSettings gameSettings;
	
	@Inject(
		at = @At(
			value = "INVOKE",
			target = GDX_ARRAY + "add("+ OBJECT + ")" + VOID
		),
		slice = @Slice(
			from = @At(
				value = "INVOKE",
				target = 
				"Lcom/worldwalkergames/legacy/ui/menu/NewGameDialogData;"
				+ "getAdditionalMods()"
				+ GDX_ARRAY
			),
			to = @At("TAIL")
		),
		//locals = LocalCapture.PRINT,
		locals = LocalCapture.CAPTURE_FAILHARD,
		method = "applyGameSettings()" + VOID,
		require = 1,
		allow = 1,
		cancellable = false
	)
	/**
	 * Makes it so that all coremods are considered locked by the game's mod API
	 * 
	 * @param c
	 */
	private void onApplyGameSettings(CallbackInfo c, @Local GameSettings.ModEntry e) {
		if(e.info instanceof CoremodInfo) {
			LOGGER.info("Locking coremod " + e.modId);
			gameSettings.lockedMods.add(e.modId);
		}
	}
	
}
