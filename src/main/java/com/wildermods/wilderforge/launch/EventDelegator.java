package com.wildermods.wilderforge.launch;

import com.wildermods.wilderforge.api.eventV1.bus.EventPriority;
import com.wildermods.wilderforge.api.eventV1.bus.SubscribeEvent;
import com.wildermods.wilderforge.api.mechanicsV1.PlotSystemCreateEvent;
import com.wildermods.wilderforge.api.overlandV1.event.plot.IncursionEvent;
import com.wildermods.wilderforge.mixins.incursion.PlotIncursionAccessor;
import com.worldwalkergames.legacy.game.campaign.model.PlotState.Plots;

@InternalOnly
public class EventDelegator {

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void onPlotSystemCreateEvent(PlotSystemCreateEvent e) {
		if(e.getPlotState().plot == Plots.incursion) {
			WilderForge.EVENT_BUS.fire(new IncursionEvent.Create.Post((PlotIncursionAccessor) e.getPlotSystem()));
		}
	}
	
}
