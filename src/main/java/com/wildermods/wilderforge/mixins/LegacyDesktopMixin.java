package com.wildermods.wilderforge.mixins;

import org.apache.logging.log4j.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.codedisaster.steamworks.SteamUtils;
import com.wildermods.wilderforge.api.modLoadingV1.event.PostInitializationEvent;
import com.wildermods.wilderforge.launch.Main;
import com.wildermods.wilderforge.launch.WilderForge;
import com.wildermods.wilderforge.launch.logging.LoggerOverrider;
import com.wildermods.wilderforge.launch.steam.SteamUtilityCallback;
import com.worldwalkergames.legacy.LegacyDesktop;
import com.worldwalkergames.logging.ALogger;
import com.worldwalkergames.logging.ALogger.ILogConsumer;
import com.worldwalkergames.logging.FilteringConsumer;

@Mixin(value = LegacyDesktop.class, remap = false)
public class LegacyDesktopMixin {

	@Inject(at = @At(value = "INVOKE_ASSIGN", target = "Ljava/util/LinkedList;add(Ljava/lang/Object;)Z"), method = "initializeLogging()V")
	private static void initializeLogging(CallbackInfo c) {
		ALogger.Aggregator aggregator = ALogger.getDefaultAggregator();
		aggregator.consumers.clear();
		LoggerOverrider loggerOverrider = new LoggerOverrider(new FilteringConsumer.Filter());
		aggregator.consumers.add(new LoggerOverrider(new FilteringConsumer.Filter()));
		try {
			new SteamUtils(new SteamUtilityCallback()).setWarningMessageHook(loggerOverrider);
		}
		catch(LinkageError e) {
			Main.LOGGER.warn("Could not redirect steam error output. Is there a steam context?");
			Main.LOGGER.catching(Level.WARN, e);
		}
		for(ILogConsumer consumer : aggregator.consumers) {
			System.out.println(consumer.getClass());
		}
	}
	
	@Inject(at = @At(value = "RETURN"), method = "create()V")
	private void create(CallbackInfo c) {
		WilderForge.EVENT_BUS.fire(new PostInitializationEvent());
	}
	
}
