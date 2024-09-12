package com.wildermods.wilderforge.api.netV1.server;

import com.worldwalkergames.legacy.control.HostInfo;

public class ServerDeathEvent extends ServerEvent {
	
	public ServerDeathEvent(HostInfo host) {
		super(false, host);
	}
	
}
