package com.wildermods.wilderforge.api.eventV2;

import com.wildermods.wilderforge.api.modLoadingV1.CoremodInfo;

@SuppressWarnings("removal")
public abstract class ModEvent extends com.wildermods.wilderforge.api.eventV1.ModEvent {
	
	public ModEvent(CoremodInfo coremod, boolean cancelable) {
		super(coremod, cancelable);
	}
	
}
