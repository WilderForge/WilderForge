package com.wildermods.wilderforge.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.worldwalkergames.legacy.LegacyDesktop;
import com.worldwalkergames.legacy.ui.MainScreen;

@Mixin(LegacyDesktop.class)
public interface LegacyDesktopAccessor {

	@Accessor
	public long getLastFrame();
	
	@Accessor("ui")
	public MainScreen getMainScreen();
	
}
