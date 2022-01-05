package com.wildermods.wilderforge.mixins.plot;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.wildermods.wilderforge.api.mechanicsV1.PlotEvent;
import com.wildermods.wilderforge.launch.WilderForge;
import com.worldwalkergames.legacy.game.campaign.model.PlotState;

@Mixin(PlotState.class)
public abstract class PlotStateMixin {

	@Inject(
		at = @At("TAIL"),
		method = "kill"
	)
	public void onKill(String reason, CallbackInfo c) {
		WilderForge.EVENT_BUS.fire(new PlotEvent.Finish((PlotState)(Object)this, reason));
	}
	
}
