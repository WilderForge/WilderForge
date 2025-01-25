package com.wildermods.wilderforge.api.modLoadingV1.event;

import com.wildermods.wilderforge.api.eventV1.Event;
import com.wildermods.wilderforge.api.mixins.v1.Cast;
import com.wildermods.wilderforge.mixins.LegacyDesktopAccessor;
import com.worldwalkergames.legacy.LegacyDesktop;
import com.worldwalkergames.legacy.context.LegacyViewDependencies;

public class PostInitializationEvent extends Event {

	private final LegacyDesktop mainApp;
	private final LegacyViewDependencies dependencies;
	
	public PostInitializationEvent(LegacyDesktop mainApp, LegacyViewDependencies dependencies) {
		super(false);
		this.mainApp = mainApp;
		this.dependencies = dependencies;
	}

	
	public LegacyDesktop getMainApplication() {
		return mainApp;
	}
	
	public LegacyDesktopAccessor getMainApplicationAccessor() {
		return Cast.from(mainApp);
	}
	
	public LegacyViewDependencies getDependencies() {
		return dependencies;
	}
}
