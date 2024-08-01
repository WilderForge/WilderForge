package com.wildermods.wilderforge.launch;

import com.wildermods.wilderforge.api.eventV1.bus.EventBus;
import com.wildermods.wilderforge.api.eventV1.bus.EventPriority;
import com.wildermods.wilderforge.api.eventV1.bus.SubscribeEvent;
import com.wildermods.wilderforge.api.netV1.clientV1.ClientMessageEvent;
import com.wildermods.wilderforge.api.serverV1.ServerDeathEvent;
import com.wildermods.wilderforge.api.serverV1.ServerEvent;
import com.wildermods.wilderforge.api.serverV1.ServerInstantiationEvent;
import com.wildermods.wilderforge.launch.logging.Logger;

import com.worldwalkergames.legacy.context.LegacyViewDependencies;
import com.worldwalkergames.legacy.control.ClientControl;
import com.worldwalkergames.legacy.server.LegacyServer;
import com.worldwalkergames.legacy.ui.MainScreen;

public final class WilderForge {
	
	@InternalOnly
	public static final Logger LOGGER = new Logger("WilderForge");
	
	@InternalOnly
	private static final ReflectionsHelper reflectionsHelper = new ReflectionsHelper(WilderForge.class.getClassLoader());
	
	@InternalOnly
	private static LegacyViewDependencies dependencies;
	
	@InternalOnly
	@Deprecated(forRemoval = true)
	private static LegacyServer server;
	
	@InternalOnly
	private static MainScreen mainScreen;
	
	@InternalOnly
	private static ClientControl clientControl;
	
	public static final EventBus MAIN_BUS = new EventBus("MAIN");
	public static final EventBus NETWORK_BUS = new EventBus("NETWORK");
	public static final EventBus RENDER_BUS = new EventBus("RENDER");
	
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
		MAIN_BUS.register(WilderForge.class);
		NETWORK_BUS.register(WilderForge.class);
		RENDER_BUS.register(WilderForge.class);
	}
	
	@InternalOnly
	public static void setMainScreen(MainScreen ui) {
		if(mainScreen != null) {
			throw new IllegalStateException("Main Screen is already set!");
		}
		if(ui == null) {
			throw new IllegalArgumentException("Main Screen instance cannot be null.", new NullPointerException());
		}
		mainScreen = ui;
		clientControl = (ClientControl) ((ClientContexted)(Object)ui).getControl();
	}
	
	public static ClientControl getClientControl() {
		return clientControl;
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
		if(WilderForge.server != server) {
			throw new IllegalStateException("Server mismatch?!");
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
	
}
