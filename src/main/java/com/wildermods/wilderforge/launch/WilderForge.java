package com.wildermods.wilderforge.launch;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.wildermods.provider.services.CrashLogService;
import com.wildermods.wilderforge.api.eventV1.bus.EventBus;
import com.wildermods.wilderforge.api.eventV1.bus.EventPriority;
import com.wildermods.wilderforge.api.eventV1.bus.SubscribeEvent;
import com.wildermods.wilderforge.api.mixins.v1.Cast;
import com.wildermods.wilderforge.api.netV1.client.ClientMessageEvent;
import com.wildermods.wilderforge.api.netV1.server.ServerBirthEvent;
import com.wildermods.wilderforge.api.netV1.server.ServerDeathEvent;
import com.wildermods.wilderforge.api.netV1.server.ServerEvent;
import com.wildermods.wilderforge.launch.logging.CrashInfo;
import com.wildermods.wilderforge.launch.logging.Logger;

import com.worldwalkergames.legacy.context.LegacyViewDependencies;
import com.worldwalkergames.legacy.control.ClientControl;
import com.worldwalkergames.legacy.control.HostInfo;
import com.worldwalkergames.legacy.ui.MainScreen;

public final class WilderForge {
	
	@InternalOnly
	public static final Logger LOGGER = new Logger("WilderForge");
	
	@InternalOnly
	private static final ReflectionsHelper reflectionsHelper = new ReflectionsHelper(WilderForge.class.getClassLoader());
	
	@InternalOnly
	private static LegacyViewDependencies dependencies;
	
	@InternalOnly
	private static HostInfo host;
	
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
		
		dependencies.globalInputProcessor.anyKeyDown.add(WilderForge.class, () -> {
			if(
				Gdx.input.isKeyJustPressed(Input.Keys.C)
				&& (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT))
				&& (Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.ALT_RIGHT))
			) {
				CrashLogService crashService = CrashLogService.obtain();
				CrashInfo c = null;
				if(crashService instanceof CrashInfo) {
					c =  Cast.from(crashService);
				}
				if(Gdx.input.isKeyPressed(Input.Keys.F1) && c != null) {
					c.doThreadDump(true);
				}
				String message = "Manually Triggered Debug Crash";
				if(c != null && c.isDumpingThreads()) {
					message = message + " With Thread Dump Enabled";
				}
				message = message + " CTRL + ALT + SHIFT +";
				message = message + ((c == null || !c.isDumpingThreads()) ? " C)" : " F1 + C)");
				throw new Error(message);
			}
		});
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
	private static void initServer(HostInfo host) {
		if(WilderForge.host == null) {
			WilderForge.host = host;
		}
		else {
			throw new IllegalStateException("Host initialized twice?! This shouldn't happen!");
		}
	}
	
	@InternalOnly
	private static void killServer(HostInfo host) {
		if(WilderForge.host != host) {
			throw new IllegalStateException("Host mismatch?!");
		}
		WilderForge.host = null;
	}
	
	@SubscribeEvent(priority = EventPriority.LOW)
	public static void onServerEvent(ServerEvent e) {
		if(e instanceof ServerBirthEvent) {
			initServer(e.getHost());
		}
		else if (e instanceof ServerDeathEvent) {
			killServer(e.getHost());
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
