package com.wildermods.wilderforge.mixins;
import java.util.LinkedList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.wildermods.wilderforge.launch.logging.Logger;
import com.wildermods.wilderforge.launch.logging.LoggerOverrider;

import com.worldwalkergames.logging.ALogger.Aggregator;
import com.worldwalkergames.logging.ALogger.ILogConsumer;

@Mixin(value = Aggregator.class, remap = false)
public class AggregatorMixin {
	public Logger logger = new Logger(this.getClass());
	public @Shadow LinkedList<ILogConsumer> consumers;
	
	/*
	 * Redirect Wildermyth's logging to Wilderforge's logger
	 */
	@Inject(at = @At("RETURN"), method = "<init>()V", require = 1)
	public void initTraceConsumerWilderForge(CallbackInfo c) {
		consumers.clear();
		consumers.add(new LoggerOverrider());
	}
	
}
