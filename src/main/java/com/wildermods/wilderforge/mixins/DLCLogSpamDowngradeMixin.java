package com.wildermods.wilderforge.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.wildermods.wilderforge.api.mixins.v1.Descriptor;
import com.worldwalkergames.legacy.server.context.ServerDataContext;
import com.worldwalkergames.logging.ALogger;

@Mixin(ServerDataContext.class)
public class DLCLogSpamDowngradeMixin {
	
	@WrapOperation(
		method = { 
			"isDLCInstalledArmorsAndSkins", 
			"isDLCInstalledArmorsAndSkinsInternal",
			"isDLCInstalledArmorsAndSkinsItch",
			
			"isDLCInstalledOmenroad",
			"isDLCInstalledOmenroadInternal",
			"isDLCInstalledOmenroadItch"
		},
		at = @At(
			value = "INVOKE",
			target = 
				"Lcom/worldwalkergames/logging/ALogger;"
					+ "log4("
						+ Descriptor.STRING
						+ Descriptor.ARRAY_OF + Descriptor.OBJECT
					+ ")" + Descriptor.VOID
					
		)
	)
	private static void downgradeExcessiveLogSpam(ALogger instance, String message, Object[] parameters, Operation<Void> original) {
		instance.log1(message, parameters);
	}
	
}
