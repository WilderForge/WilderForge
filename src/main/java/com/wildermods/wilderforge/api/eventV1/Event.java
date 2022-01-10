package com.wildermods.wilderforge.api.eventV1;

import java.util.concurrent.CancellationException;

public abstract class Event {

	private final boolean cancellable;
	private volatile boolean cancelled;
	
	public Event(boolean cancellable) {
		this.cancellable = cancellable;
	}
	
	public boolean isCancelled() {
		return cancelled && cancellable;
	}
	
	public void setCancelled(boolean cancelled) throws CancellationException {
		if(cancellable) {
			this.cancelled = cancelled;
		}
		else {
			CancellationException c = new CancellationException();
			UnsupportedOperationException u = new UnsupportedOperationException("Event " + this.getClass().getCanonicalName() + " is not cancellable");
			c.initCause(u);
			throw c;
		}
	}
	
	public boolean canBeCancelled() {
		return cancellable;
	}
	
}
