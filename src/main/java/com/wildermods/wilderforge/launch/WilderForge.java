package com.wildermods.wilderforge.launch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.wildermods.wilderforge.api.eventV1.bus.EventBus;
import com.wildermods.wilderforge.api.eventV1.bus.SubscribeEvent;
import com.wildermods.wilderforge.api.overlandV1.party.PartyCreateEvent;
import com.wildermods.wilderforge.api.overlandV1.party.PartyDisbandEvent;
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
	public static void onPartyCreate(PartyCreateEvent.Post e) {
		System.out.println("A party of size " + e.getPartyProposal().members.size + " was created!");
	}
	
	@SubscribeEvent
	public static void onPartyDisband(PartyDisbandEvent e) {
		int size = e.getParty().members.size();
		System.out.println("Game wants to disband party of size " + size);
		if(size == 2 && e.canBeCancelled()) {
			e.setCancelled(true);
			new Throwable().printStackTrace();
			System.out.println("Cancelling disbandonment of party with size " + size);
		}
		else {
			System.out.println("Allowing disbandonment of party with size " + size);
		}
	}
	
	public static LegacyViewDependencies getViewDependencies() {
		return dependencies;
	}
	
}
