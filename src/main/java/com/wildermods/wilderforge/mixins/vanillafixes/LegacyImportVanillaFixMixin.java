package com.wildermods.wilderforge.mixins.vanillafixes;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.wildermods.provider.util.logging.Logger;
import com.worldwalkergames.legacy.server.context.PlayerLegacy;

@Mixin(PlayerLegacy.class)
public class LegacyImportVanillaFixMixin {

	private static @Unique Logger LOGGER = new Logger("LegacyImportVanillaFixMixin");
	
	@WrapMethod(method = "loadFromData")
	public void onLegacyLoad(PlayerLegacy.Data data, Operation<Void> original) {
		if(data == null) {
			LOGGER.warn("Unable to load legacy. Data is null.");
		}
		else {
			try {
				synchronized(this) { //Original implementation wasn't synchronized, yet all the other methods that read/modify values here were.
					original.call(data);
				}
			}
			catch(Exception e) {
				LOGGER.catching(e);
			}
		}
	}
	
}
