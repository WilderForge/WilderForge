package com.wildermods.wilderforge.api.overlandV1.event.plot;

import com.wildermods.wilderforge.api.eventV1.Event;
import com.wildermods.wilderforge.mixins.GameKernelAccessor;
import com.wildermods.wilderforge.mixins.incursion.PlotIncursionAccessor;
import com.worldwalkergames.legacy.game.mechanics.PlotC_incursion;

public abstract class IncursionEvent extends Event {

	public IncursionEvent(boolean cancellable) {
		super(cancellable);
	}
	
	public static abstract class Create extends IncursionEvent {
		
		protected final GameKernelAccessor gameKernel;
		
		public Create(GameKernelAccessor gameKernel) {
			super(true);
			this.gameKernel = gameKernel;
		}
		
		public static class Pre extends Create {
			
			public Pre(GameKernelAccessor gameKernel) {
				super(gameKernel);
			}
			
			public GameKernelAccessor getGameKernel() {
				return gameKernel;
			}
			
		}
		
		public static class Post extends Create {
			
			private final PlotIncursionAccessor incursion;
			
			public Post(PlotIncursionAccessor incursion) {
				super(incursion.getKernelWF());
				this.incursion = incursion;
			}
			
			public PlotIncursionAccessor getIncursion() {
				return incursion;
			}
			
			public PlotC_incursion getIncursionAsPlot() {
				return (PlotC_incursion)incursion;
			}
			
		}
		
	}
	


}
