package com.wildermods.wilderforge.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.wildermods.wilderforge.api.overlandV1.OverlandMapGenerationEvent;
import com.worldwalkergames.legacy.game.world.tiles.OverlandGenContext;
import com.worldwalkergames.legacy.game.world.tiles.OverlandTileMapGenerator;

import com.worldwalkergames.scratchpad.world.OverlandTileMapLab;

@Mixin(OverlandTileMapLab.class)
public class OverlandTileMapLabMixin {

	@ModifyVariable(
			at = @At(
				value = "STORE", 
				target = 
					"com/worldwalkergames/legacy/game/world/tiles/OverlandTileMapGenerator"
			),
			method = "generate",
			name = "generator",
			require = 1
		)
		public OverlandTileMapGenerator create(OverlandTileMapGenerator generator) {
			OverlandGenContext context = ((TileMapGenerator)generator).getContext();
			OverlandMapGenerationEvent generationEvent = new OverlandMapGenerationEvent(generator, context, true);
			//WilderForge.EVENT_BUS.fire(generationEvent);
			return generationEvent.getGenerator();
		}
	
}
