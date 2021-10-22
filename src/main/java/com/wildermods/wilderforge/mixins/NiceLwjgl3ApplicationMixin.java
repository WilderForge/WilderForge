package com.wildermods.wilderforge.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.badlogic.gdx.ApplicationLogger;
import com.badlogic.gdx.backends.lwjgl3.NiceLwjgl3Application;
import com.wildermods.wilderforge.launch.logging.LoggerOverrider;

@Mixin(value = NiceLwjgl3Application.class, remap = false)
public class NiceLwjgl3ApplicationMixin {
	private @Shadow ApplicationLogger applicationLogger;
	
	/**
	 * Set the game's logger to be WilderForge's logger
	 */
	@Inject(method = "setApplicationLogger(Lcom/badlogic/gdx/ApplicationLogger;)V", at = @At("HEAD"), cancellable = true)
	private void setApplicationLogger(ApplicationLogger applicationLogger, CallbackInfo callback) {
		this.applicationLogger = new LoggerOverrider();
		callback.cancel();
	}
}
