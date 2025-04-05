package com.wildermods.wilderforge.api.eventV2;

import com.wildermods.wilderforge.api.modLoadingV1.CoremodInfo;

public abstract class ModEvent extends Event {
	
	protected final CoremodInfo coremod;
	
	public ModEvent(CoremodInfo coremod, boolean cancelable) {
		super(cancelable);
		this.coremod = coremod;
	}
	
	public CoremodInfo getCoremod() {
		return coremod;
	}
	
	public final String getModId() {
		return coremod.modId;
	}
	
}
