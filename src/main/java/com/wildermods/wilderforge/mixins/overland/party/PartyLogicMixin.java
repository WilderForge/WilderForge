package com.wildermods.wilderforge.mixins.overland.party;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import com.wildermods.wilderforge.api.mechanicsV1.Kerneled;
import com.worldwalkergames.engine.EntitiesCollection;

@Mixin(remap = false)
public abstract class PartyLogicMixin implements Kerneled {
	
	public @Shadow EntitiesCollection entities;
	
	//these don't seem to work
	/*
	@Inject (
			at = @At ("HEAD"),
			method = "removeMemberFromParty",
			cancellable = true,
			require = 1
	)
	public void preRemoveMemberFromParty(Party party, EID member, CallbackInfo c) {
		if(WilderForge.EVENT_BUS.fire(new PartyMemberRemoveEvent.Pre(this, party, member))) {
			c.cancel();
		}
	}
	
	@Inject (
			at = @At ("TAIL"),
			method = "removeMemberFromParty",
			require = 1
	)
	public void postRemoveMemberFromParty(Party party, EID member, CallbackInfo c) {
		WilderForge.EVENT_BUS.fire(new PartyMemberRemoveEvent.Post(this, party, member));
	}
	*/
	
}
