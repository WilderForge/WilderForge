package com.wildermods.wilderforge.mixins.plot;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.wildermods.wilderforge.api.mechanicsV1.PlotEvent;
import com.wildermods.wilderforge.launch.WilderForge;
import com.worldwalkergames.legacy.game.mechanics.PlotWorker;

@Mixin(PlotWorker.class)
public abstract class PlotWorkerMixin implements PlotWorkerAccessor {

	@Inject(
		at = @At("TAIL"),
		method = "kill",
		require = 1
	)
	public void onKill(String reason, CallbackInfo c) {
		WilderForge.EVENT_BUS.fire(new PlotEvent.Finish(this, this.getState(), reason));
	}
	
}
