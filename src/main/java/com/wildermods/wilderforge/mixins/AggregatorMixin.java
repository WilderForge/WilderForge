package com.wildermods.wilderforge.mixins;

import java.util.Arrays;
import java.util.LinkedList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.wildermods.wilderforge.launch.logging.LoggerOverrider;

import com.worldwalkergames.logging.ALogger.Aggregator;
import com.worldwalkergames.logging.ALogger.ILogConsumer;

@Mixin(value = Aggregator.class, remap = false)
public class AggregatorMixin {

	public @Shadow LinkedList<ILogConsumer> consumers;
	
	@Inject(at = @At("RETURN"), method = "<init>()V")
	public void initTraceConsumerWilderForge(CallbackInfo c) {
		System.out.println(Arrays.toString(consumers.toArray()));
		consumers.clear();
		consumers.add(new LoggerOverrider());
		System.out.println(Arrays.toString(consumers.toArray()));
	}
	
}
