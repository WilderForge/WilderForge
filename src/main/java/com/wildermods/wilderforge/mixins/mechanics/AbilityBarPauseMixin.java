package com.wildermods.wilderforge.mixins.mechanics;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.wildermods.wilderforge.api.mechanicsV1.AbilityBarPauseFix;
import com.wildermods.wilderforge.api.mechanicsV1.PauseEvent;
import com.wildermods.wilderforge.launch.WilderForge;
import com.worldwalkergames.engine.EID;
import com.worldwalkergames.legacy.game.api.ViewClientAPI;
import com.worldwalkergames.legacy.game.campaign.model.CampaignTime;
import com.worldwalkergames.legacy.game.common.UISelectionState;
import com.worldwalkergames.legacy.game.common.ui.AbilityBar;

@Mixin(AbilityBar.class)
public abstract class AbilityBarPauseMixin implements AbilityBarPauseFix {

	public @Unique volatile PauseEvent pauseEvent;
	public @Shadow UISelectionState selectionState;
	public @Shadow ViewClientAPI api;
	
	@Inject(
		at = @At(
			value = "HEAD"
		),
		method = "mustReset("
				+ "Lcom/worldwalkergames/engine/EID;"
				+ ")Z",
		cancellable = true,
		require = 1
	)
	private void pauseFixReset(EID selected, CallbackInfoReturnable<Boolean> c) {
		if(pauseEvent != null && pauseEvent.doesShowAbilityBarIfCancelled()) {
			c.setReturnValue(true);
		}
		//otherwise vanilla implementation
	}
	
	@Redirect(
		at = @At(
			value = "FIELD",
			target = "Lcom/worldwalkergames/legacy/game/campaign/model/CampaignTime;"
					+ "isRunning:Z"
		),
		method = "updateSelectedIndividual()V"
	)
	public boolean ignoreCampaignTimeForPausing(CampaignTime campaignTime) {
		return false;
	}
	
	@Inject(
		at = @At("HEAD"),
		method = "updateSelectedIndividual()V",
		require = 1
	)
	private void pauseDebug(CallbackInfo c) {
		WilderForge.LOGGER.log(
			mustReset(this.selectionState.selectedEntity()) + 
			" & " + !api.isWaiting() + ". (" + 
			(this.mustReset(this.selectionState.selectedEntity()) && !api.isWaiting()) + ")");
	}

	@Override
	public void setPauseEvent(PauseEvent e) {
		this.pauseEvent = e;
	}
	
}
