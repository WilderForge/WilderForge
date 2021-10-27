package com.wildermods.wilderforge.api.eventV1;

import com.wildermods.wilderforge.launch.Coremod;

public abstract class ModEvent extends Event {

	protected final Coremod coremod;
	
	public ModEvent(Coremod coremod, boolean cancellable) {
		super(cancellable);
		this.coremod = coremod;
	}

	public Coremod getCoremod() {
		return coremod;
	}
	
	public final String getModId() {
		return coremod.value();
	}
	
}
