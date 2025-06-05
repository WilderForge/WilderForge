package com.wildermods.wilderforge.mixins.input;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.wildermods.wilderforge.api.mixins.v1.Descriptor.*;

import com.wildermods.provider.util.logging.LogLevel;
import com.wildermods.wilderforge.api.mixins.v1.Cast;
import com.wildermods.wilderforge.launch.WilderForge;
import com.worldwalkergames.communication.observer.signals.Signal2;
import com.worldwalkergames.legacy.input.GlobalInputProcessor;

@Mixin(GlobalInputProcessor.class)
public class GlobalInputProcessorMixin {
	
	public @Shadow Signal2<GlobalInputProcessor, Object> anyKeyDown;
	
	@Inject(
		method = "<init>("
					+ "Lcom/worldwalkergames/legacy/context/CursorManager;"
					+ "Lcom/worldwalkergames/legacy/options/InterfaceOptions;"
					+ "Lcom/worldwalkergames/legacy/options/Keymap;"
				+ ")" + VOID,
		at = @At (
			value = "TAIL"
		),
		require = 1
	)
	private void unlimitedAnyInputListener(CallbackInfo c) {
		final int capacity = -1;
		WilderForge.LOGGER.log(LogLevel.DEBUG, "set anyKeyDown signal capacity to " + capacity, getClass().getSimpleName());
		anyKeyDown = new Signal2<>(Cast.from(this), capacity, false);
	}
	
}
