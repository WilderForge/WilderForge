package com.wildermods.wilderforge.mixins;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.wildermods.wilderforge.launch.Coremods;
import static com.wildermods.wilderforge.launch.LoadStatus.*;

import java.lang.reflect.Field;

import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "com.worldwalkergames.legacy.ui.menu.RootMenu", remap = false)
public class RootMenuMixin {
	
	private static final Field STYLE_FIELD; 
	private static final Field VERSION_STYLE_FIELD;
	static {
		try {
			STYLE_FIELD = Class.forName("com.worldwalkergames.legacy.ui.menu.RootMenu").getDeclaredField("style");
			VERSION_STYLE_FIELD = Class.forName("com.worldwalkergames.legacy.ui.menu.RootMenu$Style").getDeclaredField("version");
		} 
		catch(Exception e) {
			throw new AssertionError(e);
		}
		STYLE_FIELD.setAccessible(true);
		VERSION_STYLE_FIELD.setAccessible(true);
	}
	
	@Redirect(
		method = "build()V",
		at = @At(
			value = "NEW",
			target = "Lcom/badlogic/gdx/scenes/scene2d/ui/Label;"
		),
		require = 3
	)
	/*
	 * Setup the custom patchline
	 */
	public Label overwritePatchLine(CharSequence text, LabelStyle style) {
		if(style == getVersionStyle()) {
			text = StringUtils.capitalize(Coremods.getCoremod("wilderforge").getVersionString()) + " (" + Coremods.getCoremodCountByStatus(LOADED) + "/" + Coremods.getCoremodCountByStatus(LOADED, NOT_LOADED, ERRORED, DISCOVERED, LOADING) + " coremods loaded)";
		}
		return new Label(text, style);
	}
	
	/*
	 * the class RootMenu.Style is package private, so we cannot reference it directly. Ugly reflection to the rescue!
	 */
	@Unique
	public Object getVersionStyle() { 
		try {
			return VERSION_STYLE_FIELD.get(STYLE_FIELD.get(this));
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new AssertionError(e);
		}
	}
	
}
