package com.wildermods.wilderforge.mixins.vanillafixes;

import org.lwjgl.PointerBuffer;
import org.lwjgl.util.nfd.NativeFileDialog;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.worldwalkergames.legacy.ui.menu.ImportLegacyDialog;
import static com.wildermods.wilderforge.api.mixins.v1.Descriptor.*;

@Mixin(ImportLegacyDialog.class)
public class ImportLegacyDialogVanillaFixMixin {

	@WrapOperation(
		method = "update",
		at = @At(
			value = "INVOKE",
			target = "Lorg/lwjgl/PointerBuffer;free()" + VOID
		)
	)
	public void dontAllowVanillaToFreeBuffer(PointerBuffer pointerBuffer, Operation<Void> original) {
		//NO-OP
	}
	
	@WrapOperation (
		method = "update",
		at = @At(
			target = "Lorg/lwjgl/util/nfd/NativeFileDialog;nNFD_Free(" + LONG + ")" + VOID,
			value = "INVOKE"
		)
	)
	public void dontAllowVanillaToFreeNative(long address, Operation<Void> origninal) {
		//NO-OP
	}
	
	@Inject(
		method = "update",
		at = @At(
			target = "Lcom/worldwalkergames/legacy/ui/menu/ImportLegacyDialog;fileDialogFinished", 
			value = "INVOKE"
		)
	)
	public void fixMemory(CallbackInfo c, @Local PointerBuffer pointerBuffer) {
		NativeFileDialog.nNFD_Free(pointerBuffer.get(0));
		//pointerBuffer.free();
		//don't return, continue with native implementation
	}
}
