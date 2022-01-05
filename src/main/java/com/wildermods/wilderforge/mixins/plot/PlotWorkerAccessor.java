package com.wildermods.wilderforge.mixins.plot;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.wildermods.wilderforge.api.mechanicsV1.Kerneled;
import com.worldwalkergames.legacy.game.campaign.model.PlotState;
import com.worldwalkergames.legacy.game.mechanics.PlotWorker;

@Mixin(PlotWorker.class)
public interface PlotWorkerAccessor extends Kerneled {

	public @Accessor PlotState getState();
	
}
