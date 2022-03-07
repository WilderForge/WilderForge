package com.wildermods.wilderforge.mixins.overland.party;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.badlogic.gdx.utils.Array;
import com.wildermods.wilderforge.api.overlandV1.party.PartyCreateEvent;
import com.wildermods.wilderforge.api.overlandV1.party.PartyDisbandEvent;
import com.wildermods.wilderforge.launch.WilderForge;
import com.worldwalkergames.legacy.game.campaign.event.PartyProposal;
import com.worldwalkergames.legacy.game.campaign.model.Party;

@Mixin(targets = "com.worldwalkergames.legacy.game.mechanics.GameKernel")
public class GameKernelPartyMixin {

	@Inject (
		at = @At("HEAD"),
		method = 
			"createParty("
				+ "Lcom/worldwalkergames/legacy/game/campaign/event/PartyProposal;"
				+ "Z"
				+ "Lcom/badlogic/gdx/utils/Array;"		
			+ ")V",
		cancellable = true,
		require = 1
	)
	/**
	 * This needs to be done outside of PartyLogic.class so that jobs are not affected if the event is cancelled.
	 */
	public void createPartyPre(PartyProposal proposal, boolean removeJobs, Array<Party.TravelGroup> oldTravelGroups, CallbackInfo c) {
		PartyCreateEvent e = new PartyCreateEvent.Pre(proposal, removeJobs, oldTravelGroups);
		if (WilderForge.EVENT_BUS.fire(e)) {
			c.cancel();
		}
		removeJobs = e.isRemovingJobs();
	}
	
	@Inject (
		at = @At("TAIL"),
		method = 
		"createParty("
			+ "Lcom/worldwalkergames/legacy/game/campaign/event/PartyProposal;"
			+ "Z"
			+ "Lcom/badlogic/gdx/utils/Array;"		
		+ ")V",
		require = 1
	)
	public void createPartyPost(PartyProposal proposal, boolean removeJobs, Array<Party.TravelGroup> oldTravelGroups, CallbackInfo c) {
		WilderForge.EVENT_BUS.fire(new PartyCreateEvent.Post(proposal, removeJobs, oldTravelGroups));
	}
	
	@Inject (
		at = @At("HEAD"),
		method = "disbandParty",
		cancellable = true,
		require = 1
	)
	public void disbandParty(Party party, CallbackInfo c) {
		if(WilderForge.EVENT_BUS.fire(new PartyDisbandEvent(party))) {
			c.cancel();
		}
	}
	
}
