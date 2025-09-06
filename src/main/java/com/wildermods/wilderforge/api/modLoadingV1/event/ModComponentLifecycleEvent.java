package com.wildermods.wilderforge.api.modLoadingV1.event;

import com.wildermods.wilderforge.api.eventV3.ModEvent;
import com.wildermods.wilderforge.api.modLoadingV1.StandardModInfo;
import com.worldwalkergames.legacy.game.mods.ModInfo;

/**
 * Not yet API. This is never fired and may be removed or altered significantly. 
 */
@Deprecated
public abstract class ModComponentLifecycleEvent extends ModEvent<StandardModInfo> {

	public ModComponentLifecycleEvent(StandardModInfo mod, boolean cancellable) {
		super(mod, cancellable);
	}
	
	public ModComponentLifecycleEvent(ModInfo mod, boolean cancellable) {
		this(new StandardModInfo(mod), cancellable);
	}
	
}
