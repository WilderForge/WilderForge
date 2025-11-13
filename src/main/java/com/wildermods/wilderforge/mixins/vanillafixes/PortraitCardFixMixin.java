package com.wildermods.wilderforge.mixins.vanillafixes;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.wildermods.wilderforge.api.mixins.v1.Require;
import com.wildermods.wilderforge.api.modLoadingV1.Mod;
import com.worldwalkergames.engine.EID;
import com.worldwalkergames.engine.EntitiesCollection;
import com.worldwalkergames.legacy.game.mission.model.MissionMap;
import com.worldwalkergames.legacy.game.mission.ui.PortraitCard;
import com.worldwalkergames.legacy.render.ui.HealthBarRenderer;

@Require(@Mod(modid = "wildermyth", version = "<1.16.560")) //patched in 1.16+560
@Mixin(PortraitCard.class)
public class PortraitCardFixMixin {

	private @Shadow @Final HealthBarRenderer healthBarRenderer;
	private @Shadow @Final EID entityId;
	
	@WrapOperation(
		method = "update",
		at = @At(
			value = "INVOKE",
			target = 
				"Lcom/worldwalkergames/legacy/game/mission/model/MissionMap;" 
				+ "any("
					+ "Lcom/worldwalkergames/engine/EntitiesCollection;" 
				+ ")" 
				+ "Lcom/worldwalkergames/legacy/game/mission/model/MissionMap;"
		),
		require = 1
	)
	private @Unique MissionMap fixInjuryValueWhenMapIsNull(EntitiesCollection entities, Operation<MissionMap> original) {
		MissionMap map = original.call(entities);
		if(map == null) {
			healthBarRenderer.injurySnapshots.put(this.entityId, 0);
		}
		return map;
	}
	
}
