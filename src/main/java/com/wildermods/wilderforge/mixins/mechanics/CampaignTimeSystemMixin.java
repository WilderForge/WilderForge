package com.wildermods.wilderforge.mixins.mechanics;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.At;

import com.worldwalkergames.communication.messages.Message;
import com.worldwalkergames.legacy.game.campaign.CampaignDomain;
import com.worldwalkergames.legacy.game.campaign.system.CampaignTimeSystem;
import com.worldwalkergames.legacy.game.mission.model.Participant;
import com.worldwalkergames.legacy.server.context.ServerDataContext;

import javassist.bytecode.Opcode;


@Mixin(CampaignTimeSystem.class)
public abstract class CampaignTimeSystemMixin extends CampaignTimeSystem {

	private @Shadow Participant player;
	
	@Inject(
		at = @At( //directly AFTER playerRequestedStartTime is set to false
			value = "FIELD",
			target = "playerRequestedStartTime",
			opcode = Opcode.PUTFIELD
		),
		method = "update"
	)
	protected void clearPlayer(int dtMs, CallbackInfo c) {
		player = null;
	}
	
	@Inject(
		at = @At(
			value = "FIELD",
			target = "playerRequestedStopTime",
			opcode = Opcodes.PUTFIELD
		),
		method = "processPlayerMessage"
	)
	protected void setStopTimePlayer(Participant player, Message message, CallbackInfoReturnable<Boolean> c) {
		this.player = player;
	}
	
	@Redirect(
		at = @At(
			value = "INVOKE",
			target = "playerRequestsStopTime"
		),
		method = "update"
	)
	private void redirectStopTime() {
		playerRequestsStopTime(player);
	}
	
	@Unique
	private void playerRequestsStopTime(Participant requester) {
		((GameAPIMechanicsMixin)(Object)this.domain.gameAPI).stopCampaignTime(player);
	}
	
	public CampaignTimeSystemMixin(CampaignDomain domain, ServerDataContext serverDataContext) {
		super(domain, serverDataContext);
		throw new AssertionError();
	}
	
}
