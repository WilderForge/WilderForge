package com.wildermods.wilderforge.mixins;
import java.util.LinkedList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.wildermods.wilderforge.api.mixins.v1.Initializer.*;

import com.wildermods.wilderforge.launch.logging.LoggerOverrider;

import com.worldwalkergames.logging.ALogger.Aggregator;
import com.worldwalkergames.logging.ALogger.ILogConsumer;

@Mixin(value = Aggregator.class, remap = false)
public class AggregatorMixin {
	public @Shadow LinkedList<ILogConsumer> consumers;
	
	/*
	 * Redirect Wildermyth's logging to Wilderforge's logger
	 */
	@Inject(
		at = @At("RETURN"), 
		method = DEFAULT_CONSTRUCTOR, 
		require = 1
	)
	public void initTraceConsumerWilderForge(CallbackInfo c) {
		consumers.clear();
		consumers.add(new LoggerOverrider());
	}
	
}
