package com.wildermods.wilderforge.api.overlandV1.event.plot;

import com.wildermods.wilderforge.api.eventV1.Event;
import com.wildermods.wilderforge.mixins.GameKernelAccessor;

public abstract class IncursionEvent extends Event {

	public IncursionEvent(boolean cancellable) {
		super(cancellable);
	}
	
	public static class CreateEvent extends IncursionEvent {

		public CreateEvent(GameKernelAccessor gameKernel) {
			super(true);
		}
		
	}

}
