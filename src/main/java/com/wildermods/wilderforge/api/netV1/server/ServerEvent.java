package com.wildermods.wilderforge.api.netV1.server;

import com.wildermods.wilderforge.api.netV1.HostEvent;
import com.worldwalkergames.legacy.control.HostInfo;

public class ServerEvent extends HostEvent {

	public ServerEvent(boolean cancellable, HostInfo host) {
		super(cancellable, host);
	}

}
