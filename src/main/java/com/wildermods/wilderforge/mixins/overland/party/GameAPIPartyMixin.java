package com.wildermods.wilderforge.mixins.overland.party;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.wildermods.wilderforge.api.overlandV1.party.PartyDisbandEvent;
import com.wildermods.wilderforge.launch.WilderForge;
import com.worldwalkergames.legacy.game.campaign.model.Party;
import com.worldwalkergames.legacy.game.mechanics.GameAPI;

@Mixin(GameAPI.class)
public class GameAPIPartyMixin {

	@Inject (
		at = @At("HEAD"),
		method = "disbandParty",
		cancellable = true,
		require = 1
	)
	public void disbandParty(Party party, CallbackInfo c) {
		if(WilderForge.MAIN_BUS.fire(new PartyDisbandEvent(party))) {
			c.cancel();
		}
	}
	
}
