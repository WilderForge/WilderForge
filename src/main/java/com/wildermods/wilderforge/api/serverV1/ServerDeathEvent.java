package com.wildermods.wilderforge.api.serverV1;

import com.worldwalkergames.legacy.server.LegacyServer;

public class ServerDeathEvent extends ServerEvent {
	
	public ServerDeathEvent(LegacyServer server) {
		super(false, server);
	}
	
}
