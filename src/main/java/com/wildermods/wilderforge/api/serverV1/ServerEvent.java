package com.wildermods.wilderforge.api.serverV1;

import com.wildermods.wilderforge.api.eventV1.Event;
import com.worldwalkergames.legacy.server.LegacyServer;

public abstract class ServerEvent extends Event {

	private final LegacyServer server;
	
	public ServerEvent(boolean cancellable, LegacyServer server) {
		super(cancellable);
		this.server = server;
	}
	
	public LegacyServer getServer() {
		return server;
	}

}
