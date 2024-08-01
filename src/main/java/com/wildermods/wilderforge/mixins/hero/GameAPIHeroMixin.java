package com.wildermods.wilderforge.mixins.hero;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.badlogic.gdx.utils.Array;
import com.wildermods.wilderforge.api.heroV1.HeroProposeEvent;
import com.wildermods.wilderforge.api.mechanicsV1.Kerneled;
import com.wildermods.wilderforge.launch.WilderForge;
import com.wildermods.wilderforge.mixins.ObservantChangeWriterAccessor;
import com.worldwalkergames.engine.EntitiesCollection;
import com.worldwalkergames.legacy.game.campaign.ParticipantHostDomain;
import com.worldwalkergames.legacy.game.campaign.model.RecruitChoice.Proposal;
import com.worldwalkergames.legacy.game.mechanics.ChangeWriter;
import com.worldwalkergames.legacy.game.mechanics.GameAPI;
import com.worldwalkergames.legacy.game.mission.model.Participant;

@Mixin(GameAPI.class)
public class GameAPIHeroMixin implements Kerneled {

	public @Final @Shadow ChangeWriter changeWriter;
	
	@Inject(
		at = @At("HEAD"),
		method = "proposeRecruit",
		cancellable = true,
		require = 1
	)
	public void onProposeRecruitPre(Proposal proposal, CallbackInfo c) {
		if(WilderForge.MAIN_BUS.fire(new HeroProposeEvent.Pre(proposal))) {
			changeWriter.resetEvents();
			ObservantChangeWriterAccessor writer = (ObservantChangeWriterAccessor) changeWriter;
			EntitiesCollection entities = writer.getEntities();
			ParticipantHostDomain domain = writer.getDomain();
			Array<Participant> participants = Participant.all(entities);
			for(Participant participant : participants) {
				if(!participant.isActive) {
					continue;
				}
				domain.sendToPlayer(participant, "wilderforge.event.cancelled", "recruit");
			}
			changeWriter.sendEvents();
			c.cancel();
		}
	}
	
	@Inject(
		at = @At("TAIL"),
		method = "proposeRecruit",
		require = 1
	)
	public void onProposeRecruitPost(Proposal proposal, CallbackInfo c) {
		WilderForge.MAIN_BUS.fire(new HeroProposeEvent.Post(proposal));
	}
	
}
