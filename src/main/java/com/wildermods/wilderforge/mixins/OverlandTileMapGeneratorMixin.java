package com.wildermods.wilderforge.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.badlogic.gdx.utils.Array;
import com.worldwalkergames.collection.WeightedList;
import com.worldwalkergames.legacy.game.generation.BitmapLayer;
import com.worldwalkergames.legacy.game.world.model.OverlandTile;
import com.worldwalkergames.legacy.game.world.model.OverlandTile.Biome;
import com.worldwalkergames.legacy.game.world.tiles.OverlandTileMapGenerator;

@Mixin(value = OverlandTileMapGenerator.class, remap = false)
public class OverlandTileMapGeneratorMixin {

	@ModifyVariable(
		at = @At(
			value = "INVOKE",
			target =
				"Lcom/worldwalkergames/collection/WeightedList;"
					+ "add("
						+ "Ljava/lang/Object;"
						+ "F"
					+ ")"
				+ "Lcom/worldwalkergames/collection/WeightedList;",
			ordinal = 2
		),
		method = "assignBiomes",
		require = 1
	)
	@SuppressWarnings("unchecked")
	private WeightedList<Biome> addBiomes(WeightedList<Biome> biomes, Array<OverlandTile> tiles, BitmapLayer elevation) {
		biomes.clear();
		biomes.add(Biome.valueOf("testBiome"), 1);
		return biomes;
		//for(Biome biome : Biome.values()) {
		//	com.wildermods.wilderforge.api.overlandV1.Biome rbiome = (com.wildermods.wilderforge.api.overlandV1.Biome)(Object)biome;
		//	thiz.add((Biome)(Object)rbiome, rbiome.getWeight());
		//}
	}
	
}
