package com.wildermods.wilderforge.api.overlandV1.event.plot;

import com.wildermods.wilderforge.api.eventV1.Event;
import com.wildermods.wilderforge.mixins.incursion.PlotIncursionAccessor;
import com.worldwalkergames.legacy.game.mechanics.PlotC_incursion;

public abstract class IncursionEvent extends Event {

	public IncursionEvent(boolean cancellable) {
		super(cancellable);
	}
	
	public static abstract class Create extends IncursionEvent {
		
		protected final PlotIncursionAccessor incursion;
		
		public Create(PlotIncursionAccessor incursion, boolean cancellable) {
			super(cancellable);
			this.incursion = incursion;
		}
		
		public final PlotIncursionAccessor getIncursion() {
			return incursion;
		}
		
		public final PlotC_incursion getIncursionAsPlot() {
			return (PlotC_incursion)incursion;
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
