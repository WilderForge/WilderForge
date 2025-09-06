package com.wildermods.wilderforge.api.modLoadingV1;

import java.lang.annotation.Annotation;

import com.worldwalkergames.legacy.game.mods.IModAware;
import com.worldwalkergames.legacy.game.mods.ModInfo;

public class StandardModInfo implements IModAware, Mod {

	private final ModInfo mod;
	
	public StandardModInfo(ModInfo mod) {
		this.mod = mod;
	}
	
	@Override
	public Class<? extends Annotation> annotationType() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String modid() {
		return mod.modId;
	}

	@Override
	public String version() {
		return 0 + "." + mod.modVersion + "." + mod.modVersionMinor;
	}

	@Override
	@Deprecated(forRemoval = false)
	public String getModId() {
		return mod.modId;
	}

}
