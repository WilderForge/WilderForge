package com.wildermods.wilderforge.mixins.plot;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.wildermods.wilderforge.api.mechanicsV1.PlotEvent;
import com.wildermods.wilderforge.launch.WilderForge;
import com.worldwalkergames.engine.Entity;
import com.worldwalkergames.legacy.game.campaign.model.Job;
import com.worldwalkergames.legacy.game.campaign.model.Site;
import com.worldwalkergames.legacy.game.campaign.model.Threat;
import com.worldwalkergames.legacy.game.campaign.model.event.Event;
import com.worldwalkergames.legacy.game.mechanics.PlotWorker;
import com.worldwalkergames.legacy.game.model.effect.Role;
import com.worldwalkergames.legacy.game.model.status.Status;
import com.worldwalkergames.legacy.game.world.model.OverlandTile;

@Mixin(PlotWorker.class)
public abstract class PlotWorkerMixin implements PlotWorkerAccessor {

	@Inject(
		at = @At("TAIL"),
		method = "kill",
		require = 1
	)
	public void onKill(String reason, CallbackInfo c) {
		WilderForge.MAIN_BUS.fire(new PlotEvent.Finish(this, this.getState(), reason));
	}
	
	public abstract @Shadow Entity getEntity(Role role);
	public abstract @Shadow Site getSite(Role name);
	public abstract @Shadow Status getStatus(Role name);
	public abstract @Shadow Event getEvent(Role name);
	public abstract @Shadow Job getJob(Role job);
	public abstract @Shadow OverlandTile getTile(Role name);
	public abstract @Shadow Threat getThreat(Role name);
	
}
