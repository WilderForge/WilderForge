package com.wildermods.wilderforge.api.uiV1;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.wildermods.wilderforge.launch.coremods.WilderForge;
import com.wildermods.wilderforge.launch.coremods.Wildermyth;
import com.worldwalkergames.ui.NiceButton;

public class UIButton<T> extends NiceButton<T> {

	public UIButton(String text, Skin skin) {
		this(text, skin, skin.get(Style.class));
	}
	
	public UIButton(String text, Skin skin, String styleName) {
		this(text, skin, skin.get(styleName, Style.class));
	}
	
	public UIButton(String text, Skin skin, Style style) {
		super(text, skin, style);
	}
	
	@Override
	public void setStyle(ButtonStyle style) {
		if (style == null) {
			throw new NullPointerException("style cannot be null");
		}
		
		super.setStyle(style);
		this.style = (Style)style;
		Style iStyle = this.style;
		if (label != null) {
			Label.LabelStyle labelStyle = this.label.getStyle();
			labelStyle.font = iStyle.font;
			labelStyle.fontColor = iStyle.fontColor;
			this.label.setStyle(labelStyle);
		}
		if(iStyle.padLeft != null) {
			padLeft(iStyle.padLeft);
		}
		if(iStyle.padRight != null) {
			padRight(iStyle.padRight);
		}
		if(iStyle.padTop != null) {
			padTop(iStyle.padTop);
		}
		if(iStyle.padBottom != null) {
			padBottom(iStyle.padBottom);
		}
	}

	@Override
	protected final boolean tryClick() {
		if(!WilderForge.EVENT_BUS.fire(new ButtonEvent.ButtonTryClickEvent(Wildermyth.getViewDependencies(), this)) && super.tryClick()) {
			onClick();
			return true;
		}
		return false;
	}
	
	private final void onClick() {
		clickImpl();
		WilderForge.EVENT_BUS.fire(new ButtonEvent.ButtonClickEvent(Wildermyth.getViewDependencies(), this));
	}
	
	protected void clickImpl() {
		this.clicked.dispatch(getUserData());
	}

}
