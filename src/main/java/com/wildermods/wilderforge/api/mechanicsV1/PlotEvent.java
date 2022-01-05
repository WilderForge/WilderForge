package com.wildermods.wilderforge.api.mechanicsV1;

import com.wildermods.wilderforge.api.eventV1.Event;
import com.worldwalkergames.legacy.game.campaign.model.PlotState;

public abstract class PlotEvent extends Event {
	
	private final PlotState plot;
	
	public PlotEvent(PlotState plot, boolean cancellable) {
		super(cancellable);
		this.plot = plot;
	}
	
	public PlotState getPlot() {
		return plot;
	}

	public static class Finish extends PlotEvent {
		
		public final String reason;
		
		public Finish(PlotState plot, String reason) {
			super(plot, false);
			this.reason = reason;
		}
		
		public final String getReason() {
			return reason;
		}
		
	}
	
}
