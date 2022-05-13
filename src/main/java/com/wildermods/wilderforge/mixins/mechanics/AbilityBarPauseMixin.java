package com.wildermods.wilderforge.mixins.mechanics;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.wildermods.wilderforge.api.mechanicsV1.AbilityBarPauseFix;
import com.wildermods.wilderforge.api.mechanicsV1.PauseEvent;
import com.wildermods.wilderforge.launch.WilderForge;
import com.worldwalkergames.legacy.game.campaign.model.CampaignTime;
import com.worldwalkergames.legacy.game.common.ui.AbilityBar;

@Mixin(AbilityBar.class)
public class AbilityBarPauseMixin implements AbilityBarPauseFix {

	public @Unique volatile PauseEvent pauseEvent;
	
	@Redirect(
		at = @At(
			value = "FIELD",
			target = "Lcom/worldwalkergames/legacy/game/campaign/model/CampaignTime;"
					+ "isRunning"
					+ ":"
					+ "Z"
		),
		method = "updateSelectedIndividual()V",
		require = 1
	)
	private boolean pauseFix(CampaignTime t) {
		if(pauseEvent != null) {
			WilderForge.LOGGER.log(pauseEvent.doesShowAbilityBarIfCancelled(), "AbilityBarPauseMixin");
			return !pauseEvent.doesShowAbilityBarIfCancelled();
		}
		return t.isRunning;
	}

	@Override
	public void setPauseEvent(PauseEvent e) {
		this.pauseEvent = e;
	}
	
}
