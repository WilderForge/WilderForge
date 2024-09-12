package com.wildermods.wilderforge.api.netV1;

import com.wildermods.wilderforge.api.eventV1.Event;
import com.worldwalkergames.legacy.control.HostInfo;

public abstract class HostEvent extends Event {

	private final HostInfo host;
	
	public HostEvent(boolean cancellable, HostInfo host) {
		super(cancellable);
		this.host = host;
	}
	
	public HostInfo getHost() {
		return host;
	}

}
