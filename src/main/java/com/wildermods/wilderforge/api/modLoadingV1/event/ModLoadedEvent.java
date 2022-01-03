package com.wildermods.wilderforge.api.modLoadingV1.event;

import com.wildermods.wilderforge.api.eventV1.ModEvent;
import com.wildermods.wilderforge.api.modLoadingV1.CoremodInfo;

public final class ModLoadedEvent extends ModEvent {
	
	public ModLoadedEvent(CoremodInfo coremod) {
		super(coremod, false);
	}
	
	public CoremodInfo getCoremod() {
		return super.getCoremod();
	}

}
