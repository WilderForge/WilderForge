package com.wildermods.wilderforge.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.worldwalkergames.ui.FancyImageDrawable;

@Mixin(value = FancyImageDrawable.class, remap = false)
public interface FancyImageDrawableAccessor {

	@Accessor
	public SpriteDrawable getSpriteDrawable();
	
	@Accessor
	public void setSpriteDrawable(SpriteDrawable sprite);
	
	@Accessor
	public String getImagePath();
	
	@Accessor(value = "checkSpecificMod")
	public String getSpecificMod();
	
}
