package com.wildermods.wilderforge.api.mechanicsV1;

import com.wildermods.wilderforge.api.eventV1.Event;
import com.worldwalkergames.legacy.game.campaign.model.PlotState;
import com.worldwalkergames.legacy.game.mechanics.PlotProcessor;
import com.worldwalkergames.legacy.game.mechanics.PlotSystem;

public class PlotSystemCreateEvent extends Event {

	private final PlotSystem plotSystem;
	private final PlotState plotState;
	private final PlotProcessor plotProcessor;
	
	public PlotSystemCreateEvent(PlotSystem plot, PlotState plotState, PlotProcessor plotProcessor) {
		super(false);
		this.plotSystem = plot;
		this.plotState = plotState;
		this.plotProcessor = plotProcessor;
	}
	
	public PlotSystem getPlotSystem() {
		return plotSystem;
	}

	public PlotState getPlotState() {
		return plotState;
	}
	
	public PlotProcessor getPlotProcessor() {
		return plotProcessor;
	}
	
}
