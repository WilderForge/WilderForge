package com.wildermods.wilderforge.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.wildermods.wilderforge.api.modLoadingV1.event.PostInitializationEvent;
import com.wildermods.wilderforge.launch.LoadStage;
import com.wildermods.wilderforge.launch.coremods.Wildermyth;

import static com.wildermods.wilderforge.launch.LoadStage.POST_INIT;

import com.wildermods.wilderforge.launch.ui.NoFeedbackPopup;

import com.worldwalkergames.legacy.context.LegacyViewDependencies;
import com.worldwalkergames.legacy.ui.LegacyUIRoot;
import com.worldwalkergames.legacy.ui.feedback.FeedbackPopup;

@Mixin(value = LegacyUIRoot.class, remap = false)
public class LegacyUIRootMixin {
	
	protected @Shadow LegacyViewDependencies dependencies;
	
	@Inject(
		method = "before()V",
		at = @At(value = "RETURN"),
		require = 1
	)
	/*
	 * Fires the PostInitializationEvent and sets LegacyViewDependencies
	 */
	public void before(CallbackInfo c) {
		LoadStage.setLoadStage(POST_INIT);
		Wildermyth.init(new PostInitializationEvent(), dependencies);
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
	
}
