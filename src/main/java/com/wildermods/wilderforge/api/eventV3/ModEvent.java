package com.wildermods.wilderforge.api.eventV3;

import java.lang.annotation.Annotation;

import com.wildermods.provider.util.logging.Logger;
import com.wildermods.wilderforge.api.mixins.v1.Cast;
import com.wildermods.wilderforge.api.modLoadingV1.CoremodInfo;
import com.wildermods.wilderforge.api.modLoadingV1.Mod;
import com.worldwalkergames.legacy.game.mods.IModAware;
import com.worldwalkergames.legacy.game.mods.ModInfo;

public abstract class ModEvent<T extends Mod & IModAware> extends Event implements Mod, IModAware {
	
	protected static final Logger LOGGER = new Logger(ModEvent.class);
	protected final T mod;
	
	public ModEvent(T mod, boolean cancelable) {
		super(cancelable);
		this.mod = mod;
	}
	
	public T getMod() {
		return mod;
	}
	
	public CoremodInfo getCoremod() {
		return Cast.from(mod);
	}
	
	public ModInfo getStandardMod() {
		if(isCoremod()) {
			LOGGER.warn("getStandardMod() call on coremod event", mod.getClass().getSimpleName());
		}
		return Cast.from(mod);
	}
	
	public boolean isStandardModPure() {
		return !isCoremod() && isStandardMod();
	}
	
	public boolean isStandardMod() {
		return mod instanceof ModInfo; //should always return true...
	}
	
	public boolean isCoremod() {
		return mod instanceof CoremodInfo;
	}
	
	@Override
	@Deprecated(forRemoval = false)
	public final String getModId() {
		return mod.modid();
	}
	
	@Override
	public final String modid() {
		return mod.modid();
	}
	
	@Override
	public final String version() {
		return mod.version();
	}

	@Override
	@Deprecated
	public Class<? extends Annotation> annotationType() {
		throw new UnsupportedOperationException();
	}
	
}
