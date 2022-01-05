package com.wildermods.wilderforge.launch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.wildermods.wilderforge.api.eventV1.bus.EventBus;
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
	
	public static LegacyViewDependencies getViewDependencies() {
		return dependencies;
	}
	
}
