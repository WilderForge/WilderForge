package com.wildermods.wilderforge.launch.ui.element;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.wildermods.wilderforge.launch.Unknown;

public class TintedDrawable<T extends Drawable> implements Drawable {

	private final T parent;
	private @Nullable Color tint = null;
	
	public TintedDrawable(T drawable) {
		this.parent = Objects.requireNonNull(drawable, "Parent drawable cannot be null.");
	}
	
	public TintedDrawable setTint(Color tint) {
		this.tint = tint;
		return this;
	}
	
	public void removeTint() {
		this.tint = null;
	}
	
	public T getParent() {
		return parent;
	}
	
	@Override
	public void draw(Batch batch, @Unknown float arg1, @Unknown float arg2, @Unknown float arg3, @Unknown float arg4) {
		Color prevColor = batch.getColor();
		Color tint = this.tint; //set a local variable for the tint to avoid threading issues.
		if(tint != null) {
			batch.setColor(tint);
		}
		parent.draw(batch, arg1, arg2, arg3, arg4);
		batch.setColor(prevColor);
	}

	@Override
	public float getBottomHeight() {
		return parent.getBottomHeight();
	}

	@Override
	public float getLeftWidth() {
		return parent.getLeftWidth();
	}

	@Override
	public float getMinHeight() {
		return parent.getMinHeight();
	}

	@Override
	public float getMinWidth() {
		return parent.getMinWidth();
	}

	@Override
	public float getRightWidth() {
		return parent.getRightWidth();
	}

	@Override
	public float getTopHeight() {
		return parent.getTopHeight();
	}

	@Override
	public void setBottomHeight(float bottomHeight) {
		parent.setBottomHeight(bottomHeight);
	}

	@Override
	public void setLeftWidth(float leftWidth) {
		parent.setLeftWidth(leftWidth);
	}

	@Override
	public void setMinHeight(float minHeight) {
		parent.setMinHeight(minHeight);
	}

	@Override
	public void setMinWidth(float minWidth) {
		parent.setMinWidth(minWidth);
	}

	@Override
	public void setRightWidth(float rightWidth) {
		parent.setRightWidth(rightWidth);
	}

	@Override
	public void setTopHeight(float topHeight) {
		parent.setTopHeight(topHeight);
	}
	
	@Override
	public String toString() {
		return "TintedDrawable{parent=" + parent + ", tint=" + tint + "}";
	}

}
