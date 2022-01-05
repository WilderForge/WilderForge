package com.wildermods.wilderforge.mixins.plot;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.wildermods.wilderforge.api.mechanicsV1.PlotSystemCreateEvent;
import com.wildermods.wilderforge.launch.WilderForge;
import com.worldwalkergames.legacy.game.campaign.model.PlotState;
import com.worldwalkergames.legacy.game.mechanics.PlotProcessor;
import com.worldwalkergames.legacy.game.mechanics.PlotSystem;

@Mixin(PlotProcessor.class)
public abstract class PlotProcessorMixin {

	@Inject (
		method = 
			"makeNewPlotSystem",
		at = @At(
			value = "RETURN"
		)
	)
	public PlotSystem makeNewPlotSystem(PlotState plot, CallbackInfoReturnable<PlotSystem> c) {
		WilderForge.EVENT_BUS.fire(new PlotSystemCreateEvent(c.getReturnValue(),plot, (PlotProcessor)(Object)this));
		return c.getReturnValue();
	}
	
}
