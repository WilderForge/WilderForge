package com.wildermods.wilderforge.mixins.vanillafixes;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.wildermods.wilderforge.api.mixins.v1.Descriptor.*;

import com.worldwalkergames.legacy.ui.DialogFrame;
import com.worldwalkergames.legacy.ui.menu.NewPlayerDialog;
import com.worldwalkergames.ui.NiceLabel;

/**
 * Fixes WilderForge#50, a vanilla issue where upon attempting to create a new profile 
 * with an invalid name, the UI would become bugged.
 */
@Mixin(NewPlayerDialog.class)
public class NewPlayerDialogVanillaFixMixin {

	private @Shadow DialogFrame frame;
	private @Shadow NiceLabel validation;

/*
	@Inject(
			at = @At(
			value = "INVOKE_ASSIGN",
			target = "Lcom/worldwalkergames/legacy/ui/DialogFrame;"
		),
		method = "build()V",
		cancellable = false
	)
	private void enableFrameDebugging(CallbackInfo c) {
		frame.setDebug(true);
	}
*/
	
	@Inject(
		at = @At(
			value = "TAIL"
		),
		method = "build()" + VOID,
		cancellable = false,
		require = 1
	)
	private void enableLabelDebugging(CallbackInfo c) {
		//validation.setDebug(true);
		validation.setWrap(false);
	}
	
/*	
	@Inject(
		at = @At("HEAD"),
		method = "showValidationError(Ljava/lang/String;)V",
		cancellable = false,
		require = 1
	)
	private void debugValidationError(String error, CallbackInfo c) {
		System.out.println("Showing validation error: " + error);
		System.out.println("Label preferred Height: " + validation.getPrefHeight());
		System.out.println("Label height: " + validation.getHeight());
		System.out.println("Label max height: " + validation.getMaxHeight());
		System.out.println("Label scale Y: " + validation.getScaleY());
	}
*/
}
