package com.wildermods.wilderforge.api.modLoadingV1.event;

import com.wildermods.wilderforge.api.eventV2.ModEvent;
import com.wildermods.wilderforge.api.modLoadingV1.StandardModInfo;

public class StandardModInitializationEvent extends ModEvent<StandardModInfo> {

	public StandardModInitializationEvent(StandardModInfo mod, boolean cancelable) {
		super(mod, cancelable);
	}
	
}
