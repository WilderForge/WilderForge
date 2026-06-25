package com.wildermods.wilderforge.mixins.vanillafixes;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.wildermods.provider.util.logging.Logger;
import com.worldwalkergames.engine.EID;
import com.worldwalkergames.engine.EntitiesCollection;
import com.worldwalkergames.engine.Entity;
import com.worldwalkergames.legacy.game.common.ui.AbilityBar;
import com.worldwalkergames.legacy.game.mission.InputActionBus;
import com.worldwalkergames.legacy.game.model.Faction;
import com.worldwalkergames.legacy.game.model.Faction.FactionRole;
import com.worldwalkergames.legacy.game.model.Faction.FactionStatus;

@Mixin(AbilityBar.class)
public abstract class FactionStatusAbiltiyBarVanillaFixMixin {

	private final Logger LOGGER = new Logger(this.getClass());
	private @Shadow EntitiesCollection entities;
	private @Shadow EID showing;
	private @Final @Shadow InputActionBus actionBus;
	
	@Inject(
		method = "updateSelectedIndividual",
		at = @At(
			value = "RETURN",
			ordinal = 1,
			shift = Shift.BY,
			by = 2
		),
		require = 1,
		cancellable = true
	)
	private void onUpdateIndividual(CallbackInfo c, @Local LocalRef<Entity> entity) {
		if(Faction.getFactionByRole(entities, FactionRole.HERO).status != FactionStatus.ACTIVE) {
			resetBar(showing, actionBus);
			//Debug.trace(LOGGER, "Resetting action bar and disallowing update due to faction not active");
			c.cancel();
		}
	}
	
	protected abstract @Shadow void resetBar(EID selectedIndividual, InputActionBus bus);
}
