package com.wildermods.wilderforge.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.wildermods.wilderforge.api.overlandV1.event.plot.IncursionEvent;
import com.wildermods.wilderforge.launch.WilderForge;
import com.worldwalkergames.legacy.game.campaign.model.Threat;

@Mixin(targets = "com.worldwalkergames.legacy.game.mechanics.GameKernel", remap = false)
public class IncursionMixin {

	@Inject(
		method = "doIncursion(Lcom/worldwalkergames/legacy/game/campaign/model/Threat;)V",
		at = @At("HEAD"),
		cancellable = true,
		require = 1
	)
	public void buildThreatFromSource(Threat incursionThreat, CallbackInfo c) {
		if(WilderForge.EVENT_BUS.fire(new IncursionEvent.CreateEvent((GameKernelAccessor)(Object)this))) {
			c.cancel();
		}
	}
	
}
