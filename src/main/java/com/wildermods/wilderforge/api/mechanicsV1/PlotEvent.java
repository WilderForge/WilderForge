package com.wildermods.wilderforge.api.mechanicsV1;

import com.wildermods.wilderforge.api.eventV1.Event;
import com.wildermods.wilderforge.mixins.plot.PlotWorkerAccessor;
import com.worldwalkergames.legacy.game.campaign.model.PlotState;

public abstract class PlotEvent extends Event {
	
	private final PlotWorkerAccessor plotWorker;
	private final PlotState plotState;
	
	public PlotEvent(PlotWorkerAccessor plotWorker, PlotState plotState, boolean cancellable) {
		super(cancellable);
		this.plotWorker = plotWorker;
		this.plotState = plotState;
	}
	
	public PlotWorkerAccessor getPlotWorker() {
		return plotWorker;
	}
	
	public PlotState getPlotState() {
		return plotState;
	}

	public static class Finish extends PlotEvent {
		
		public final String reason;
		
		public Finish(PlotWorkerAccessor plotWorker, PlotState plotState, String reason) {
			super(plotWorker, plotState, false);
			this.reason = reason;
		}
		
		public final String getReason() {
			return reason;
		}
		
	}
	
}
