package com.wildermods.wilderforge.mixins.hero;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.wildermods.wilderforge.api.heroV1.HeroEvent;
import com.wildermods.wilderforge.launch.WilderForge;
import com.worldwalkergames.engine.EID;
import com.worldwalkergames.engine.EntitiesCollection;
import com.worldwalkergames.engine.Entity;
import com.worldwalkergames.legacy.game.mechanics.EffectResolver.EffectContext;
import com.worldwalkergames.legacy.game.mechanics.OutcomeProcessor;
import com.worldwalkergames.legacy.game.mechanics.OutcomeProcessor.ProcessResult;
import com.worldwalkergames.legacy.game.model.effect.Outcome.ChangeControl;
import com.worldwalkergames.legacy.game.model.effect.Role;

@Mixin(OutcomeProcessor.class)
public abstract class OutcomeProcessorHeroMixin {

	public @Shadow EntitiesCollection entities;
	
	@Inject (
		at = @At("HEAD"),
		method = "recruitHero",
		cancellable = true,
		require = 1
	)
	public void onRecruitPre(EffectContext context, EID target, CallbackInfoReturnable<ProcessResult> c) {
		Entity entity = entities.entity(context.action.getFirstEntity(Role.self));
		if(WilderForge.MAIN_BUS.fire(new HeroEvent.Recruit.Pre(entity))) {
			c.setReturnValue(ProcessResult.fail);
		}
	}
	
	@Inject(
		at = @At("TAIL"),
		method = "recruitHero",
		require = 1,
		locals = LocalCapture.CAPTURE_FAILHARD
	)
	public void onRecruitPost(EffectContext context, EID target, CallbackInfoReturnable<ProcessResult> cir, Entity e) {
		WilderForge.MAIN_BUS.fire(new HeroEvent.Recruit.Post(e));
	}
	
	@Inject (
		at = @At("HEAD"),
		method = "processChangeControl",
		cancellable = true,
		require = 1
	)
	public void processChangeControlPre(EffectContext context, Entity outcomeTarget, ChangeControl changeControl, CallbackInfoReturnable<ProcessResult> c) {
		if(WilderForge.MAIN_BUS.fire(new HeroEvent.ControlChange.Pre(context, outcomeTarget, changeControl))) {
			c.setReturnValue(ProcessResult.fail);
		}
	}
	
	@Inject (
		at = @At("TAIL"),
		method = "processChangeControl",
		require = 1
	)
	public void processChangeControlPost(EffectContext context, Entity outcomeTarget, ChangeControl changeControl, CallbackInfoReturnable<ProcessResult> c) {
		WilderForge.MAIN_BUS.fire(new HeroEvent.ControlChange.Post(context, outcomeTarget, changeControl));
	}
	
}
