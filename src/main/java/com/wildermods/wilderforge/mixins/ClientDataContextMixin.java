package com.wildermods.wilderforge.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.worldwalkergames.legacy.context.ClientDataContext;
import com.worldwalkergames.legacy.game.model.player.PlayerAccount;

@Mixin(remap = false, value = ClientDataContext.class)
public abstract class ClientDataContextMixin {
	
	@Inject(
		at = @At("HEAD"),
		method = "updateAccountAlwaysOnModsLists(Lcom/worldwalkergames/legacy/game/model/player/PlayerAccount;)V",
		cancellable = true,
		require = 1
	)
	/*
	 * Fixes vanilla bug where accounts are unable to be deleted if the user has a mod that
	 * is always on.
	 * 
	 * See https://github.com/Gamebuster19901/WilderForge/issues/32
	 */
	public void updateAccountAlwaysOnModsLists(PlayerAccount account, CallbackInfo c) {
		if(account == null) {
			c.cancel();
		}
	}

}
