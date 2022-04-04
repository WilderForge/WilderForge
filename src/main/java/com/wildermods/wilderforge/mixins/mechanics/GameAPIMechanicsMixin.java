package com.wildermods.wilderforge.mixins.mechanics;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;

import com.wildermods.wilderforge.api.mechanicsV1.Kerneled;
import com.wildermods.wilderforge.api.mechanicsV1.PauseEvent;
import com.wildermods.wilderforge.api.mechanicsV1.UnpauseEvent;
import com.wildermods.wilderforge.launch.WilderForge;
import com.worldwalkergames.legacy.game.mechanics.ChangeWriter;
import com.worldwalkergames.legacy.game.mechanics.GameAPI;
import com.worldwalkergames.legacy.game.mission.model.Participant;

@Mixin(value = GameAPI.class, remap = false)
public abstract class GameAPIMechanicsMixin implements Kerneled {

	private @Shadow ChangeWriter changeWriter;
	private static @Shadow boolean campaignTimeIsRunning;
	
	@Inject(
		at = @At("HEAD"),
		method = "startCampaignTime",
		cancellable = true
	)
	public void startCampaignTime(CallbackInfo c) {
		startCampaignTime((Participant)null);
		c.cancel();
	}
	
	@Inject(
		at = @At("HEAD"),
		method = "stopCampaignTime",
		cancellable = true
	)
	public void stopCampaignTime(CallbackInfo c) {
		stopCampaignTime((Participant)null);
		c.cancel();
	}
	
	
	@Unique
	public void startCampaignTime(Participant requester) {
		if(!WilderForge.EVENT_BUS.fire(new UnpauseEvent(requester))) {
			startCampaignTimeWF();
		}
	}
	
	@Unique
	public void stopCampaignTime(Participant requester) {
		if(!WilderForge.EVENT_BUS.fire(new PauseEvent(requester))) {
			stopCampaignTimeWF();
		}
	}
	
	@Unique
	protected void startCampaignTimeWF() {
		changeWriter.resetEvents();
		getKernelWF().invokeStartCampaignTime();
		campaignTimeIsRunning = true;
		changeWriter.sendEvents();
	}
	
	@Unique
	protected void stopCampaignTimeWF() {
		changeWriter.resetEvents();
		getKernelWF().invokeStopCampaignTime();
		campaignTimeIsRunning = false;
		changeWriter.sendEvents();
	}
	
}
