package com.wildermods.wilderforge.launch;

import com.wildermods.wilderforge.api.eventV1.bus.EventBus;
import com.wildermods.wilderforge.api.eventV1.bus.EventPriority;
import com.wildermods.wilderforge.api.eventV1.bus.SubscribeEvent;
import com.wildermods.wilderforge.api.netV1.clientV1.ClientMessageEvent;
import com.wildermods.wilderforge.api.overlandV1.party.PartyMemberRemoveEvent;
import com.wildermods.wilderforge.launch.logging.Logger;
import com.worldwalkergames.legacy.context.LegacyViewDependencies;

public final class WilderForge {
	
	@InternalOnly
	public static final Logger LOGGER = new Logger("WilderForge");
	
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
	public static void onRemoveMemberFromParty(PartyMemberRemoveEvent.Pre e) {
		System.out.println("Cancelled the removal of " + WilderForge.dependencies.gameStrings.bestName(e.getPartyLogic().getKernelWF().getEntities(), e.getMember()));
		e.setCancelled(true);
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
