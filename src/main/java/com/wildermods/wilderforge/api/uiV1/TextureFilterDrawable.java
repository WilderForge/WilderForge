package com.wildermods.wilderforge.api.uiV1;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.wildermods.wilderforge.mixins.FancyImageDrawableAccessor;
import com.worldwalkergames.legacy.context.GarbageCollectingTextureCache;
import com.worldwalkergames.ui.FancyImageDrawable;

public class TextureFilterDrawable extends FancyImageDrawable {

	public final FancyImageDrawableAccessor access = (FancyImageDrawableAccessor)(Object)this;
	
	private final TextureFilter minFilter;
	private final TextureFilter magFilter;
	
	public TextureFilterDrawable(String imagePath, String checkSpecificMod, TextureFilter minimizeFilter, TextureFilter magnifyFilter) {
		super(imagePath, checkSpecificMod);
		this.minFilter = minimizeFilter;
		this.magFilter = magnifyFilter;
	}
	
	public TextureFilterDrawable(String imagePath, String checkSpecificMod, TextureFilter filter) {
		this(imagePath, checkSpecificMod, filter, filter);
	}
	
	@Override
	public void draw(Batch batch, float x, float y, float width, float height) {
		GarbageCollectingTextureCache cache = GarbageCollectingTextureCache.getInstance();
		Texture texture = cache.use(access.getImagePath(), access.getSpecificMod(), true, 2f);
		texture.setFilter(minFilter, magFilter);
		if (access.getSpriteDrawable().getSprite() != null && texture != access.getSpriteDrawable().getSprite().getTexture()) {
			Sprite sprite = new Sprite(texture);
			this.setMinWidth(sprite.getWidth());
			this.setMinHeight(sprite.getHeight());
			access.setSpriteDrawable(new SpriteDrawable(sprite));
		}
		access.getSpriteDrawable().draw(batch, x, y, width, height);
	}

}
