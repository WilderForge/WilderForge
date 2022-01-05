package com.wildermods.wilderforge.api.overlandV1.event.plot;

import com.wildermods.wilderforge.api.mechanicsV1.PlotEvent;
import com.wildermods.wilderforge.mixins.incursion.PlotIncursionAccessor;
import com.worldwalkergames.legacy.game.mechanics.PlotC_incursion;

public abstract class IncursionEvent extends PlotEvent {

	protected final PlotIncursionAccessor incursion;
	
	public IncursionEvent(PlotIncursionAccessor incursion, boolean cancellable) {
		super(incursion.getState(), cancellable);
		this.incursion = incursion;
	}
	
	public final PlotIncursionAccessor getIncursion() {
		return incursion;
	}
	
	public final PlotC_incursion getIncursionAsPlot() {
		return (PlotC_incursion)incursion;
	}
	
	public static abstract class Create extends IncursionEvent {
		
		public Create(PlotIncursionAccessor incursion, boolean cancellable) {
			super(incursion, cancellable);
		}
		
		public static final class Pre extends Create {
			
			public Pre(PlotIncursionAccessor incursion) {
				super(incursion, true);
			}
			
		}
		
		public static final class Post extends Create {
			
			public Post(PlotIncursionAccessor incursion) {
				super(incursion, false);
			}
			
		}
		
	}
	


}
