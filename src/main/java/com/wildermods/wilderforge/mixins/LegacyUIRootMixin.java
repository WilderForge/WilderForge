package com.wildermods.wilderforge.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.wildermods.wilderforge.api.mixins.v1.Descriptor.*;
import com.wildermods.wilderforge.launch.ClientContexted;
import com.wildermods.wilderforge.launch.WilderForge;
import com.wildermods.wilderforge.launch.ui.NoFeedbackPopup;

import com.worldwalkergames.legacy.context.LegacyViewDependencies;
import com.worldwalkergames.legacy.control.ClientContext;
import com.worldwalkergames.legacy.ui.LegacyUIRoot;
import com.worldwalkergames.legacy.ui.feedback.FeedbackPopup;

@Mixin(value = LegacyUIRoot.class, remap = false)
public abstract class LegacyUIRootMixin implements ClientContexted {
	
	protected @Shadow LegacyViewDependencies dependencies;
	
	@Inject(
		method = "before()" + VOID,
		at = @At(value = "RETURN"),
		require = 1
	)
	/*
	 * Fires the PostInitializationEvent and sets LegacyViewDependencies
	 */
	public void before(CallbackInfo c) {
		WilderForge.init(dependencies);
	}
	
	/*
	 * Disables Wildermyth's feedback gui.
	 * 
	 * This method is intentionally using @Overwrite, as no other mixins should alter this method.
	 * Wildermyth developers should not get spammed with issues caused by a coremodded environment.
	 */
	@Overwrite(remap = false)
	private void onGiveFeedback() {
		if(!FeedbackPopup.isActive) {
			dependencies.popUpManager.pushFront(new NoFeedbackPopup(dependencies), false);
		}
		return;
	}
	
	public abstract @Accessor ClientContext getControl();
	
}
