package com.wildermods.wilderforge.api.modLoadingV1;

import com.wildermods.wilderforge.launch.coremods.Coremods;
import com.worldwalkergames.legacy.game.mods.IModAware;

public interface CoremodAware extends IModAware, Mod {

	public default CoremodInfo getCoremod() {
		return Coremods.getCoremod(this);
	}
	
	@Override
	public default String getModId() {
		return modid();
	}
	
	@Override
	public default String version() {
		return getCoremod().version();
	}
	
	@Override
	public String modid();
	
}
