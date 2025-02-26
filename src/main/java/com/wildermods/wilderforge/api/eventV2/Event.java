package com.wildermods.wilderforge.api.eventV2;

@SuppressWarnings("removal")
public abstract class Event extends com.wildermods.wilderforge.api.eventV1.Event {
	
	public Event(boolean cancelable) {
		super(cancelable);
	}
	
	/**
	 * @deprecated, use {@link #isCanceled()}
	 */
	@Deprecated(forRemoval = true)
	public boolean isCancelled() {
		return this.isCanceled();
	}
	
	/**
	 * @deprecated, use {@link #setCanceled(boolean)}
	 */
	@Deprecated(forRemoval = true)
	public void setCancelled(boolean cancelled) throws UnsupportedOperationException {
		this.setCanceled(cancelled);
	}
	
	/**
	 * @deprecated, use {@link #isCancelable()}
	 */
	@Deprecated(forRemoval = true)
	public boolean canBeCancelled() {
		return this.isCancelable();
	}
	
	@Override
	public boolean isCancelable() {
		return super.isCancelable();
	}
	
}
