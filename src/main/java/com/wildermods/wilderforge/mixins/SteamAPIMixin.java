package com.wildermods.wilderforge.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import com.codedisaster.steamworks.SteamAPI;

@Mixin(value = SteamAPI.class, remap = false)
public class SteamAPIMixin {

	@Overwrite
	public static boolean init() {
		throw new Error("Manually triggered debug crash");
	}
	
}
