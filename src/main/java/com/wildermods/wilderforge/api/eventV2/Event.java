package com.wildermods.wilderforge.api.eventV2;

public abstract class Event extends net.minecraftforge.eventbus.api.Event {
	
	private final boolean cancelable;
	
	public Event(boolean cancelable) {
		this.cancelable = cancelable;
	}
	
	@Override
	public boolean isCancelable() {
		return cancelable;
	}
	
}
