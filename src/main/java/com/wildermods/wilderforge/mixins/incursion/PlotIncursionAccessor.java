package com.wildermods.wilderforge.mixins.incursion;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import com.wildermods.wilderforge.mixins.plot.PlotSystemAccessor;
import com.worldwalkergames.legacy.game.campaign.model.Threat;
import com.worldwalkergames.legacy.game.mechanics.PlotC_calamities;
import com.worldwalkergames.legacy.game.mechanics.PlotC_incursion;
import com.worldwalkergames.legacy.game.world.model.OverlandTile;

@Mixin(PlotC_incursion.class)
public interface PlotIncursionAccessor extends PlotSystemAccessor {

	public @Accessor("SPEED") float getSpeed();
	public @Accessor("balance") PlotC_calamities.CalamityBalance getCalamityBalance();
	public @Accessor OverlandTile getTile();
	public @Accessor Threat getThreat();
	
	public @Invoker boolean invokeRazeSite();
	
	public @Invoker void callReduceIncursionStrength(boolean fronDefence);
	
	public @Invoker boolean invokePlanMove();
	public @Invoker boolean invokeMoveToNextTile();
	
}
