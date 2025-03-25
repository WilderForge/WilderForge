package com.wildermods.wilderforge.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.wildermods.wilderforge.api.uiV1.PopUpEvent.PopUpAddEvent;
import com.wildermods.wilderforge.api.uiV1.PopUpEvent.PopUpRemoveEvent;
import com.wildermods.wilderforge.launch.WilderForge;
import com.worldwalkergames.ui.popup.IPopUp;
import com.worldwalkergames.ui.popup.PopUpManager;

@Mixin(PopUpManager.class)
public class PopUpManagerMixin {

	@Inject(
		method = "pushFront",
		at = @At(
			value = "HEAD"
		),
		cancellable = true,
		require = 1
	)
	public void onPushFront(IPopUp popup, boolean skipFadeIn, CallbackInfo c) {
		WilderForge.LOGGER.fatal("POPUP FPUSHED: " + popup);
		if(WilderForge.MAIN_BUS.post(new PopUpAddEvent.PushFrontEvent.Pre(popup, skipFadeIn))) {
			c.cancel();
		}
	}
	
	@Inject(
		method = "pushBack",
		at = @At(
			value = "HEAD"
		),
		cancellable = true,
		require = 1
	)
	public void onPushBack(IPopUp popup, CallbackInfo c) {
		WilderForge.LOGGER.fatal("POPUP BPUSHED: " + popup);
		if(WilderForge.MAIN_BUS.post(new PopUpAddEvent.PushBackEvent.Pre(popup))) {
			c.cancel();
		}
	}
	
	@Inject(
		method = "removePopUp",
		at = @At(
			value = "TAIL"
		),
		cancellable = true,
		require = 1
	)
	public void onRemovePopup(IPopUp popup, CallbackInfo c) {
		WilderForge.LOGGER.error("POPUP REMOVED: " + popup.toString());
		if(WilderForge.MAIN_BUS.post(new PopUpRemoveEvent.Post(popup))) {
			c.cancel();
		}
		
	}
	
}
