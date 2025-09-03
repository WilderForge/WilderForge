package com.wildermods.wilderforge.api.eventV2;

import com.wildermods.wilderforge.api.mixins.v1.Cast;
import com.wildermods.wilderforge.api.modLoadingV1.CoremodInfo;
import com.wildermods.wilderforge.api.modLoadingV1.Mod;
import com.worldwalkergames.legacy.game.mods.IModAware;

import net.fabricmc.loader.api.ModContainer;

public abstract class ModEvent<T extends ModContainer & Mod & IModAware> extends Event {
	
	protected final T mod;
	
	public ModEvent(T mod, boolean cancelable) {
		super(cancelable);
		this.mod = mod;
	}
	
	public T getMod() {
		return mod;
	}
	
	@Deprecated(forRemoval = true)
	public CoremodInfo getCoremod() {
		return Cast.as(mod, CoremodInfo.class);
	}
	
	public final String getModId() {
		return mod.modid();
	}
	
}
