package com.wildermods.wilderforge.api.netV1.server;

import com.worldwalkergames.legacy.control.HostInfo;

public class ServerBirthEvent extends ServerEvent {
	
	public ServerBirthEvent(HostInfo host) {
		super(false, host);
	}
	
}
