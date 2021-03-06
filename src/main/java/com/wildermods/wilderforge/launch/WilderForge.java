package com.wildermods.wilderforge.launch;

import com.wildermods.wilderforge.api.eventV1.bus.EventBus;
import com.wildermods.wilderforge.api.eventV1.bus.EventPriority;
import com.wildermods.wilderforge.api.eventV1.bus.SubscribeEvent;
import com.wildermods.wilderforge.api.mechanicsV1.PauseEvent;
import com.wildermods.wilderforge.api.netV1.clientV1.ClientMessageEvent;
import com.wildermods.wilderforge.api.serverV1.ServerDeathEvent;
import com.wildermods.wilderforge.api.serverV1.ServerEvent;
import com.wildermods.wilderforge.api.serverV1.ServerInstantiationEvent;
import com.wildermods.wilderforge.launch.logging.Logger;
import com.worldwalkergames.legacy.context.LegacyViewDependencies;
import com.worldwalkergames.legacy.server.LegacyServer;

public final class WilderForge {
	
	@InternalOnly
	public static final Logger LOGGER = new Logger("WilderForge");
	
	@InternalOnly
	private static final ReflectionsHelper reflectionsHelper = new ReflectionsHelper(WilderForge.class.getClassLoader());
	
	@InternalOnly
	private static LegacyViewDependencies dependencies;
	
	@InternalOnly
	private static LegacyServer server;
	
	public static final EventBus EVENT_BUS = new EventBus();
	
	@InternalOnly
	public static ReflectionsHelper getReflectionsHelper() {
		return reflectionsHelper;
	}

	@InternalOnly
	public static void init(LegacyViewDependencies dependencies) {
		getReflectionsHelper().getTypeAndSubTypesOf(WilderForge.class);
		if(WilderForge.dependencies == null) {
			WilderForge.dependencies = dependencies;
		}
		else {
			throw new IllegalStateException();
		}
		EVENT_BUS.register(WilderForge.class);
	}
	
	@InternalOnly
	private static void initServer(LegacyServer server) {
		if(WilderForge.server == null) {
			WilderForge.server = server;
		}
		else {
			throw new IllegalStateException("Server initialized twice?! This shouldn't happen!");
		}
	}
	
	@InternalOnly
	private static void killServer(LegacyServer server) {
		if(WilderForge.server == null) {
			throw new IllegalStateException("Server already killed?!");
		}
		WilderForge.server = null;
	}
	
	@SubscribeEvent(priority = EventPriority.LOW)
	public static void onServerEvent(ServerEvent e) {
		if(e instanceof ServerInstantiationEvent) {
			initServer(e.getServer());
		}
		else if (e instanceof ServerDeathEvent) {
			killServer(e.getServer());
		}
	}
	
	@SubscribeEvent
	public static void onPause(PauseEvent e) {
		e.setCancelled(true);
		LOGGER.info("Cancelled pausing!");
		LOGGER.info("The server is: " +    server);
	}
	
	@SubscribeEvent(priority = EventPriority.LOW)
	public static void onUnhandledClientMessage(ClientMessageEvent.PostVanillaChecks e) {
		if(e.getMessage().to.match("wilderforge.event.cancelled")) {
			e.getClient().setWaiting(false);
			e.setHandled();
		}
	}
	
	public static LegacyViewDependencies getViewDependencies() {
		return dependencies;
	}
	
	public static LegacyServer getServer() {
		return server;
	}
	
}
