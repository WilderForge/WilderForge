package com.wildermods.wilderforge.launch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.wildermods.wilderforge.api.eventV1.bus.EventBus;
import com.wildermods.wilderforge.api.eventV1.bus.SubscribeEvent;
import com.wildermods.wilderforge.api.heroV1.HeroEvent;
import com.wildermods.wilderforge.api.heroV1.HeroProposeEvent;
import com.wildermods.wilderforge.api.netV1.clientV1.ClientMessageEvent;
import com.worldwalkergames.legacy.context.LegacyViewDependencies;

public final class WilderForge {
	
	@InternalOnly
	public static final Logger LOGGER = LogManager.getLogger(WilderForge.class);
	
	@InternalOnly
	private static final ReflectionsHelper reflectionsHelper = new ReflectionsHelper(WilderForge.class.getClassLoader());
	
	@InternalOnly
	private static LegacyViewDependencies dependencies;
	
	public static final EventBus EVENT_BUS = new EventBus();
	
	@InternalOnly
	public static ReflectionsHelper getReflectionsHelper() {
		return reflectionsHelper;
	}

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
	
	@SubscribeEvent
	public static void onProposeRecruitHero(HeroProposeEvent.Pre e) {
		LOGGER.error("Cancelling proposal of hero.");
		e.setCancelled(true);
	}
	
	@SubscribeEvent
	public static void onRecruitHero(HeroEvent.Recruit.Pre e) {
		LOGGER.error("Cancelling recruitment of hero");
		e.setCancelled(true);
	}
	
	@SubscribeEvent
	public static void onHeroChange(HeroEvent.ControlChange.Pre e) {
		LOGGER.error("Cancelling control change of hero");
		e.setCancelled(true);
	}
	
	@SubscribeEvent
	public static void onUnhandledClientMessage(ClientMessageEvent.PostVanillaChecks e) {
		if(e.getMessage().to.match("wilderforge.event.cancelled")) {
			e.getClient().setWaiting(false);
			e.setHandled();
			LOGGER.error("Yay, we successfully handled the cancelled action!");
		}
	}
	
	public static LegacyViewDependencies getViewDependencies() {
		return dependencies;
	}
	
}
