package com.wildermods.wilderforge.launch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.wildermods.wilderforge.api.eventV1.bus.EventBus;
import com.wildermods.wilderforge.api.eventV1.bus.SubscribeEvent;
import com.wildermods.wilderforge.api.overlandV1.event.plot.IncursionEvent;
import com.wildermods.wilderforge.mixins.incursion.PlotIncursionAccessor;
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
	public static void onIncursionPre(IncursionEvent.Create.Pre e) {
		e.setCancelled(true);
		PlotIncursionAccessor incursion = e.getIncursion();
		System.out.println("An incursion was cancelled. It was of type " + incursion.getThreat().flavor + ". It had a strength of " + incursion.getThreat().getStrength(true) + " and a size of " + incursion.getThreat().getIncursionSize());
	}
	
	@SubscribeEvent
	public static void onIncursionPost(IncursionEvent.Create.Post e) {
		PlotIncursionAccessor incursion = e.getIncursion();
		System.out.println("Wow, an incursion of type " + incursion.getThreat().flavor + " was created! It's strength is " + incursion.getThreat().getStrength(true) + " and it's size is " + incursion.getThreat().getIncursionSize());
	}
	
	public static LegacyViewDependencies getViewDependencies() {
		return dependencies;
	}
	
}
