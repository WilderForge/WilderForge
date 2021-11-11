package com.wildermods.wilderforge.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.wildermods.wilderforge.api.overlandV1.OverlandMapGenerationEvent;
import com.wildermods.wilderforge.launch.coremods.WilderForge;
import com.worldwalkergames.legacy.game.campaign.CampaignGenerator;
import com.worldwalkergames.legacy.game.campaign.model.CampaignTemplate;
import com.worldwalkergames.legacy.game.campaign.model.GameSettings;
import com.worldwalkergames.legacy.game.world.tiles.OverlandGenContext;
import com.worldwalkergames.legacy.game.world.tiles.OverlandTileMapGenerator;
import com.worldwalkergames.legacy.platform.model.NewGameRequest;

@Mixin(value = CampaignGenerator.class, remap = false)
public class CampaignGeneratorMixin {
	
	@ModifyVariable(
		at = @At(
			value = "STORE", 
			target = 
				"com/worldwalkergames/legacy/game/world/tiles/OverlandTileMapGenerator"
		),
		method = "create",
		name = "tileMapGenerator",
		require = 1
	)
	public OverlandTileMapGenerator create(OverlandTileMapGenerator generator, CampaignTemplate campaignTemplate, GameSettings settings, NewGameRequest request) {
		OverlandGenContext context = ((TileMapGenerator)generator).getContext();
		OverlandMapGenerationEvent generationEvent = new OverlandMapGenerationEvent(generator, context, campaignTemplate, settings, request);
		WilderForge.EVENT_BUS.fire(generationEvent);
		return generationEvent.getGenerator();
	}
	
}
