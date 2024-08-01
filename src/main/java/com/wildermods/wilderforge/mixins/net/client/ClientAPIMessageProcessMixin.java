package com.wildermods.wilderforge.mixins.net.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.wildermods.wilderforge.api.netV1.clientV1.ClientMessageEvent;
import com.wildermods.wilderforge.launch.WilderForge;
import com.worldwalkergames.communication.messages.Message;
import com.worldwalkergames.legacy.game.api.ClientAPI;

@Mixin(ClientAPI.class)
public abstract class ClientAPIMessageProcessMixin implements ClientAPIAccessor {

	public @Shadow boolean isWaiting;
	
	@Inject(
		at = @At("HEAD"),
		method = "process",
		require = 1,
		cancellable = true
	)
	public void preVanillaChecks(Message message, CallbackInfoReturnable<Boolean> c) {
		ClientMessageEvent e = new ClientMessageEvent.PreVanillaChecks(this, message);
		WilderForge.NETWORK_BUS.fire(e);
		Boolean handled = e.hasBeenHandled();
		if(null != handled) {
			c.setReturnValue(handled);
		}
	}
	
	@Inject(
		at = @At(
			value = "INVOKE",
			target = "Lcom/worldwalkergames/communication/Path;"
					+ "match"
					+ "+" //MATCH ALL INVOCATIONS OF Path#match(String value) (fewer than 1 match results in error condition)
					+ "("
						+ "Ljava/lang/String;"
					+ ")Z"
		),
		method = "process",
		expect = 18,
		cancellable = true
	)
	public void onVanillaMessageCheck(Message message, CallbackInfoReturnable<Boolean> c) {
		ClientMessageEvent e = new ClientMessageEvent.OnVanillaCheck(this, message);
		WilderForge.NETWORK_BUS.fire(e);
		Boolean handled = e.hasBeenHandled();
		if(null != handled) {
			c.setReturnValue(handled);
		}
	}
	
	@Inject(
		at = @At("TAIL"),
		method = "process",
		require = 1,
		cancellable = true
	)
	public void postVanillaChecks(Message message, CallbackInfoReturnable<Boolean> c) {
		ClientMessageEvent e = new ClientMessageEvent.PostVanillaChecks(this, message);
		WilderForge.NETWORK_BUS.fire(e);
		Boolean handled = e.hasBeenHandled();
		if(null != handled) {
			c.setReturnValue(handled);
		}
	}
}
