package com.wildermods.wilderforge.api.eventV1;

/**
 * Deprecated, use {@link com.wildermods.wilderforge.api.eventV2.Event}
 */
@Deprecated(forRemoval = true)
public abstract class Event extends net.minecraftforge.eventbus.api.Event {
	
	private final boolean cancelable;
	
	public Event(boolean cancellable) {
		this.cancelable = cancellable;
	}
	
	@Deprecated(forRemoval = true)
	public boolean isCancelled() {
		return this.isCanceled();
	}
	
	@Deprecated(forRemoval = true)
	public void setCancelled(boolean cancelled) throws UnsupportedOperationException {
		this.setCanceled(cancelled);
	}
	
	@Deprecated
	public boolean canBeCancelled() {
		return this.isCancelable();
	}
	
	@Override
	public boolean isCancelable() {
		return cancelable;
	}
	
}
