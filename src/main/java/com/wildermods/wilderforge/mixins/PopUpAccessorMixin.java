package com.wildermods.wilderforge.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.worldwalkergames.legacy.context.LegacyViewDependencies;
import com.worldwalkergames.legacy.ui.PopUp;

@Mixin(PopUp.class)
public interface PopUpAccessorMixin {
	
	public @Accessor LegacyViewDependencies getDependencies();
	
}
