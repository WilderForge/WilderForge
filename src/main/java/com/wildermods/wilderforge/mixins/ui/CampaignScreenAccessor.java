package com.wildermods.wilderforge.mixins.ui;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.worldwalkergames.legacy.game.campaign.ui.CampaignHud;
import com.worldwalkergames.legacy.game.campaign.ui.WorldMapCameraControls;
import com.worldwalkergames.legacy.game.mission.controls.WorldInputManager;
import com.worldwalkergames.legacy.ui.CampaignScreen;

@Mixin(CampaignScreen.class)
public interface CampaignScreenAccessor {

	public @Accessor("hud") CampaignHud getCampaignHud();
	public @Accessor WorldInputManager getInputManager();
	public @Accessor WorldMapCameraControls getWorldMapCameraControls();
	
}
