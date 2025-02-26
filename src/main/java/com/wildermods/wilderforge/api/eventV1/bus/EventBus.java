package com.wildermods.wilderforge.api.eventV1.bus;

import com.wildermods.wilderforge.api.eventV1.Event;

import net.minecraftforge.eventbus.api.BusBuilder;
import net.minecraftforge.eventbus.api.IEventBus;

@Deprecated(forRemoval = true)
public class EventBus {

	private IEventBus bus;
	
	public EventBus(String name) {
		this.bus = BusBuilder.builder().build();
	}
	
	public void register(Class c) {
		bus.register(c);
	}
	
	public void register(Object o) {
		bus.register(o);
	}
	
	
	/**
	 * @deprecated, use {@link IEventBus#post(Event)}
	 * 
	 * NOTE: IEventBus.fire() has a different behavior!
	 * Use IEventBus.post() is the equivalent to this method!
	 */
	@Deprecated(forRemoval = false)
	public boolean fire(Event e) {
		return bus.post(e);
	}
	
}
