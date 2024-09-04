package com.wildermods.wilderforge.mixins;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.codedisaster.steamworks.SteamUtils;

import com.wildermods.wilderforge.launch.WilderForge;
import com.wildermods.wilderforge.launch.logging.GraphicalInfo;
import com.wildermods.wilderforge.launch.logging.LogLevel;
import com.wildermods.wilderforge.launch.logging.LoggerOverrider;
import com.wildermods.wilderforge.launch.steam.SteamUtilityCallback;
import static com.wildermods.wilderforge.api.mixins.v1.Descriptor.*;

import com.worldwalkergames.legacy.LegacyDesktop;
import com.worldwalkergames.legacy.ui.MainScreen;
import com.worldwalkergames.logging.ALogger;
import com.worldwalkergames.logging.ALogger.ILogConsumer;
import com.worldwalkergames.logging.FilteringConsumer;

@Mixin(value = LegacyDesktop.class, remap = false)
public class LegacyDesktopMixin {
	
	private static @Shadow @Final ALogger LOGGER;
	private @Shadow MainScreen ui;

	/**
	 * Set steam's logger to be WilderForge's logger
	 */
	@Inject(at = @At(value = "INVOKE_ASSIGN", target = "Ljava/util/LinkedList;add("+ OBJECT +")" + BOOLEAN), method = "initializeLogging()" + VOID, require = 1)
	private static void setLogger(CallbackInfo c) {
		ALogger.Aggregator aggregator = ALogger.getDefaultAggregator();
		aggregator.consumers.clear();
		LoggerOverrider loggerOverrider = new LoggerOverrider(new FilteringConsumer.Filter());
		aggregator.consumers.add(new LoggerOverrider(new FilteringConsumer.Filter()));
		try {
			new SteamUtils(new SteamUtilityCallback()).setWarningMessageHook(loggerOverrider);
		}
		catch(LinkageError e) {
			WilderForge.LOGGER.warn("Could not redirect steam error output. Is there a steam context?");
			WilderForge.LOGGER.catching(LogLevel.WARN, e);
		}
		for(ILogConsumer consumer : aggregator.consumers) {
			WilderForge.LOGGER.debug(consumer.getClass());
		}
	}
	
	@Inject(at = @At(value = "HEAD"), method = "create()" + VOID)
	public void assignGraphicInfo(CallbackInfo c) {
		GraphicalInfo.INSTANCE = new GraphicalInfo((LegacyDesktop)(Object)this);
	}
	
	@Inject(at = @At(value = "HEAD"), method = "fatalError")
	public void fatalError(Throwable t, CallbackInfo c) throws Throwable {
		dispose();
		throw t;
	}
	
	/**
	 * sets the {@link WilderForge.mainScreen} Field
	 */
	@Inject(
		at = @At(
			value = "FIELD",
			shift = Shift.AFTER,
			opcode = Opcodes.PUTFIELD,
			target = "ui"
		),
		method = "create()" + VOID
	)
	public void assignWFMainScreen(CallbackInfo c) {
		WilderForge.setMainScreen(ui); //set to the main screen instance
	}
	
	@Shadow
	public void dispose() {}
	
}
