package com.wildermods.wilderforge.mixins.hero;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.wildermods.wilderforge.api.heroV1.HeroEvent;
import com.wildermods.wilderforge.launch.WilderForge;
import com.wildermods.wilderforge.mixins.plot.PlotWorkerMixin;

import com.worldwalkergames.engine.Entity;
import com.worldwalkergames.legacy.game.campaign.model.Site;
import com.worldwalkergames.legacy.game.mechanics.PlotC_recruitNewHero;
import com.worldwalkergames.legacy.game.model.effect.Role;

@Mixin(PlotC_recruitNewHero.class)
public abstract class PlotC_recruitNewHeroMixin extends PlotWorkerMixin {

	@Inject(
		at = @At("HEAD"),
		method = "resolve",
		cancellable = true,
		require = 1
	)
	public void resolveRecruitPre(CallbackInfoReturnable<Boolean> c) {
		Entity hero = this.getEntity(Role.target);
		Site site = this.getSite(Role.site);
		if(WilderForge.MAIN_BUS.fire(new HeroEvent.Recruit.Pre(hero, site))) {
			c.setReturnValue(false);
		}
	}
	
}
