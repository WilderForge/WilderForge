package com.wildermods.wilderforge.mixins;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.wildermods.wilderforge.launch.WilderForge;
import com.wildermods.wilderforge.launch.coremods.Coremods;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.lang.reflect.Field;

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
		ModContainer wilderforge = FabricLoader.getInstance().getModContainer("wilderforge").get();
		if(style == getVersionStyle()) {
			text = WilderForge.getViewDependencies().getString("wilderforge.mainMenu.patchline", wilderforge.getMetadata().getName(), wilderforge.getMetadata().getVersion().getFriendlyString(), Coremods.getCoremodCount() + "");
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