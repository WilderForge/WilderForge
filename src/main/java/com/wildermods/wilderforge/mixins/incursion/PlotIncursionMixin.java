package com.wildermods.wilderforge.mixins.incursion;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.wildermods.wilderforge.api.overlandV1.event.plot.IncursionEvent;
import com.wildermods.wilderforge.launch.WilderForge;
import com.worldwalkergames.legacy.game.mechanics.PlotC_incursion;

@Mixin(PlotC_incursion.class)
public abstract class PlotIncursionMixin implements PlotIncursionAccessor {
	
	@Inject(
		method = "buildThreatFromSource",
		at = @At(
			value = "FIELD", 
			shift = At.Shift.AFTER,
			target = "Lcom/worldwalkergames/legacy/game/campaign/model/Threat;"
						+ "brainState"
		),
		cancellable = true,
		require = 1
	)
	public void buildThreadFromSourcePre(CallbackInfoReturnable<Boolean> c) {
		if(WilderForge.EVENT_BUS.fire(new IncursionEvent.Create.Pre(this))) {
			this.getKernelWF().getChangeWriter().deleteEntity(this.getThreat().getParentEntity());
			this.getState().kill("Incursion cancelled.");
			c.setReturnValue(false);
		}
	}
	
	@Inject(
		method = "buildThreatFromSource",
		at = @At("RETURN"),
		cancellable = true,
		require = 1
	)
	public void buildThreatFromSourcePost(CallbackInfoReturnable<Boolean> c) {
		if(c.getReturnValueZ() == true) {
			WilderForge.EVENT_BUS.fire(new IncursionEvent.Create.Post(this));
		}
	}
	
}
