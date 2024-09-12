package com.wildermods.wilderforge.api.netV1.client;

import com.wildermods.wilderforge.api.eventV1.Event;
import com.worldwalkergames.legacy.server.IConnectionToServer;

public abstract class ClientConnectionToServerEvent extends Event {

	private IConnectionToServer connection;
	
	public IConnectionToServer getConnection() {
		return connection;
	}
	
	public ClientConnectionToServerEvent(IConnectionToServer connection, boolean cancellable) {
		super(cancellable);
		this.connection = connection;
	}

	public static final class Pre extends ClientConnectionToServerEvent {
		public Pre(IConnectionToServer connection) {
			super(connection, true);
		}
	}
	
	public static final class Post extends ClientConnectionToServerEvent {
		public Post(IConnectionToServer connection) {
			super(connection, false);
		}
	}

}
