package com.wildermods.wilderforge.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.wildermods.wilderforge.launch.logging.LoggerOverrider;
import com.worldwalkergames.legacy.LegacyDesktop;
import com.worldwalkergames.logging.ALogger;
import com.worldwalkergames.logging.FilteringConsumer;

@Mixin(value = LegacyDesktop.class, remap = false)
public class LegacyDesktopMixin {

	@Inject(at = @At(value = "INVOKE_ASSIGN", target = "Ljava/util/LinkedList;add(Ljava/lang/Object;)Z"), method = "initializeLogging()V")
	private static void initializeLogging(CallbackInfo c) {
		ALogger.Aggregator aggregator = ALogger.getDefaultAggregator();
		aggregator.consumers.clear();
		aggregator.consumers.add(new LoggerOverrider(new FilteringConsumer.Filter()));
	}
	
}
