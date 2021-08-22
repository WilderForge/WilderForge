package com.wildermods.wilderforge.api.eventV1;

public abstract class Event {

	private final boolean cancellable;
	private volatile boolean cancelled;
	
	public Event(boolean cancellable) {
		this.cancellable = cancellable;
	}
	
	public boolean isCancelled() {
		return cancelled && cancellable;
	}
	
	public void setCancelled(boolean cancelled) {
		if(cancellable) {
			this.cancelled = cancelled;
		}
		else {
			throw new UnsupportedOperationException("Event " + this.getClass().getName() + " is not cancellable");
		}
	}
	
}
