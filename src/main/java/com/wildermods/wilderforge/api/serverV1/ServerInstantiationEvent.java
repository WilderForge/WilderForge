package com.wildermods.wilderforge.api.serverV1;
import com.worldwalkergames.legacy.server.LegacyServer;

public class ServerInstantiationEvent extends ServerEvent {
	
	public ServerInstantiationEvent(LegacyServer server) {
		super(false, server);
	}
	
}
