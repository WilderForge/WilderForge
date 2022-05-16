package com.wildermods.wilderforge.mixins.net.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import com.worldwalkergames.legacy.game.api.ViewClientAPI;

@Mixin(ViewClientAPI.class)
public class ViewClientAPIPauseMixin {

	@Overwrite
	public boolean isWaiting() {
		return false;
	}
	
}
