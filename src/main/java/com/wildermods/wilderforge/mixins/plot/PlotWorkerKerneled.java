package com.wildermods.wilderforge.mixins.plot;

import org.spongepowered.asm.mixin.Mixin;

import com.wildermods.wilderforge.api.mechanicsV1.PlotWorkerKernelRetriever;
import com.worldwalkergames.legacy.game.mechanics.PlotWorker;

@Mixin(PlotWorker.class)
public interface PlotWorkerKerneled extends PlotWorkerKernelRetriever {

}
