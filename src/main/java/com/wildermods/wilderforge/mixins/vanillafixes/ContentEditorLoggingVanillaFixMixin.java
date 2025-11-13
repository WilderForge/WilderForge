package com.wildermods.wilderforge.mixins.vanillafixes;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.wildermods.wilderforge.api.mixins.v1.Descriptor;
import com.wildermods.wilderforge.api.mixins.v1.Require;
import com.wildermods.wilderforge.api.modLoadingV1.Mod;
import com.worldwalkergames.legacy.editor.ContentEditor;
import com.worldwalkergames.logging.ALogger;

@Mixin(ContentEditor.class)
@Require(@Mod(modid = "wildermyth", version = "<1.16+560")) //patched in 1.16+560
public class ContentEditorLoggingVanillaFixMixin {

	@WrapOperation(
		method = "saveAll",
		at = @At(
			value = "INVOKE",
			target = "Lcom/worldwalkergames/logging/ALogger;"
					+ "log4("
						+ Descriptor.STRING
						+ Descriptor.ARRAY_OF + Descriptor.OBJECT
					+ ")" + Descriptor.VOID
		)
	)
	public void fixIncorrectLogging(ALogger instance, String message, Object[] args, Operation<Void> original, @Local RuntimeException caught) {
		if(args == null) {
			args = new Object[1];
		}
		if(args.length == 0) {
			args = new Object[1];
		}
		if(args[0] == null) {
			args[0] = caught;
		}
		original.call(instance, message, args);
	}
	
}
