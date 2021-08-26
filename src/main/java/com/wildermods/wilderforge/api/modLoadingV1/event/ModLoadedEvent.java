package com.wildermods.wilderforge.api.modLoadingV1.event;

import com.wildermods.wilderforge.api.eventV1.ModEvent;
import com.wildermods.wilderforge.launch.Coremod;

@SuppressWarnings("deprecation")
public final class ModLoadedEvent extends ModEvent {
	
	public ModLoadedEvent(Coremod coremod) {
		super(coremod, false);
	}
	
	public Coremod getCoremod() {
		return super.getCoremod();
	}

}
