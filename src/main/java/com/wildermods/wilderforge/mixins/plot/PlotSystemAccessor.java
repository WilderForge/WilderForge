package com.wildermods.wilderforge.mixins.plot;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import com.worldwalkergames.legacy.game.action.IReadOnlyRoleMap;
import com.worldwalkergames.legacy.game.campaign.model.Objective;
import com.worldwalkergames.legacy.game.campaign.system.MissionCallbackData;
import com.worldwalkergames.legacy.game.mechanics.PlotProcessor;
import com.worldwalkergames.legacy.game.mechanics.PlotSystem;
import com.worldwalkergames.legacy.game.mission.model.MissionTemplate;

@Mixin(PlotSystem.class)
public interface PlotSystemAccessor extends PlotWorkerKerneled {
	
	public @Invoker boolean invokeShouldTransferToMission(MissionTemplate missionTemplate);
	public @Invoker boolean invokeShouldTransferBackFromMission(MissionCallbackData callbackData);
	public @Invoker void callUpdate(long advanceTime);
	public @Invoker void invokeAddStep(String name, PlotProcessor.PlotStepFunction function);
	public @Invoker void invokeStartUpdating(String name, PlotProcessor.PlotUpdateFunction function);
	public @Invoker void invokeStopUpdating(String name);
	public @Invoker void invokeGoToStep(String name);
	public @Invoker boolean invokeAttemptCheat(Objective objective);
	public @Invoker IReadOnlyRoleMap invokeLoadPremade(String premadeFileName);
	public @Invoker void callDisposePlot();

}
