package com.wildermods.wilderforge.launch.ui.element;

import java.util.Objects;
import java.util.function.BiPredicate;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.RuntimeSkin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import com.wildermods.wilderforge.api.modLoadingV1.config.ModConfigurationEntryBuilder.ConfigurationUIEntryContext;
import com.worldwalkergames.legacy.context.ClientDataContext.Skins;
import com.worldwalkergames.ui.AutoSwapDrawable;
import com.worldwalkergames.ui.Dropdown;
import com.worldwalkergames.ui.FancyImageButton;
import com.worldwalkergames.ui.NiceCheckBox;

public class WFConfigEntryBoolean<T> extends NiceCheckBox<T> implements BiPredicate<ConfigurationUIEntryContext, WFConfigEntryBoolean<T>> {

	private final ConfigurationUIEntryContext context;
	
	private boolean state = false;
	
	volatile BiPredicate<ConfigurationUIEntryContext, WFConfigEntryBoolean<T>> validator = null;

	volatile FancyImageButton undoButton = null;
	volatile FancyImageButton resetButton = null;
	
	TintedDrawable<AutoSwapDrawable> backgroundNeutral;
	TintedDrawable<AutoSwapDrawable> backgroundOver;
	TintedDrawable<AutoSwapDrawable> backgroundDown;
	TintedDrawable<AutoSwapDrawable> backgroundDisabled;
	
	public WFConfigEntryBoolean(ConfigurationUIEntryContext context, String text, RuntimeSkin skin) {
		this(context, text, skin, "default");
	}
	
	public WFConfigEntryBoolean(ConfigurationUIEntryContext context, String text, RuntimeSkin skin, String styleName) {
		super(text, skin, styleName);
		this.context = context;
		
		RuntimeSkin scaledSkin = skin.getSisterSkin(Skins.SCALE_UI);
		this.backgroundNeutral = new TintedDrawable<>(new AutoSwapDrawable(scaledSkin));
		this.backgroundOver = new TintedDrawable<>(new AutoSwapDrawable(scaledSkin));
		this.backgroundDown = new TintedDrawable<>(new AutoSwapDrawable(scaledSkin));
		this.backgroundDisabled = new TintedDrawable<>(new AutoSwapDrawable(scaledSkin));
		
		this.addDrawables("buttonRip_192x6");
		this.addDrawables("buttonRip_192x4");
		this.addDrawables("buttonRip_192x3");
		this.addDrawables("buttonRip_192x2");
		this.addDrawables("buttonRip_192x1");
		
		this.addDrawables("buttonRip_128x8");
		this.addDrawables("buttonRip_128x6");
		this.addDrawables("buttonRip_128x4");
		this.addDrawables("buttonRip_128x3");
		this.addDrawables("buttonRip_128x2");
		this.addDrawables("buttonRip_128x1");
		
		int scalar = skin.getScaleModeNumber();
		Dropdown dropdown = new Dropdown(skin);
		dropdown.adjustLayoutVars(backgroundNeutral.getParent(), scalar);
		dropdown.adjustLayoutVars(backgroundOver.getParent(), scalar);
		dropdown.adjustLayoutVars(backgroundDown.getParent(), scalar);
		dropdown.adjustLayoutVars(backgroundDisabled.getParent(), scalar);
		
		this.getImageCell().getActor().remove();
		this.getImageCell().minWidth(0f).spaceRight(0f);
		
		this.addListener(new ClickListener() {
			public void clicked (InputEvent event, float x, float y) {
				toggle();
			}
		});
	}
	
	public WFConfigEntryBoolean<T> setValidator(BiPredicate<ConfigurationUIEntryContext, WFConfigEntryBoolean<T>> testFunc) {
		this.validator = testFunc;
		return this;
	}
	
	public void setState(boolean state) {
		this.state = state;
		update();
	}
	
	public boolean getState() {
		return state;
	}
	
	public void toggle() {
		this.state = !state;

		
		update();
	}
	
	private void update() {
		context.setNewVal(state);
		this.setText(state + "");
		if(undoButton != null) {
			if(test(context, this) && context.changed()) {
				undoButton.setDisabled(false);
			}
			else {
				undoButton.setDisabled(true);
			}
		}
		
		if(resetButton != null) {
			if(test(context, this) && !context.isDefault()) {
				resetButton.setColor(UIColors.invalidColor);
				resetButton.setDisabled(false);
			}
			else {
				resetButton.setColor(Color.WHITE);
				resetButton.setDisabled(true);
			}
		}
	}
	
	@Override
	public void buildLabel() {
		Label label = this.getLabel();
		this.add(label);
		label.setAlignment(Align.center);
	}
	
	private void addDrawables(String textureName) {
		this.backgroundNeutral.getParent().addOption(textureName + "_up");
		this.backgroundOver.getParent().addOption(textureName + "_over");
		this.backgroundDown.getParent().addOption(textureName + "_down");
		this.backgroundDisabled.getParent().addOption(textureName + "_disabled");
	}
	
	@Override
	protected TintedDrawable<AutoSwapDrawable> getBackgroundDrawable() {
		ClickListener listener = this.getClickListener();
		if(this.isDisabled()) {
			return this.backgroundDisabled;
		}
		else if(listener.isPressed()) {
			return this.backgroundDown;
		}
		else if (listener.isOver()) {
			return this.backgroundOver;
		}
		else {
			return this.backgroundNeutral;
		}
	}
	
	@Override
	public void draw(Batch batch, float parentAlpha) {
		TintedDrawable<AutoSwapDrawable> drawable = getBackgroundDrawable();
		
		Color prevColor = getColor();
		
		Boolean isValid;
		
		BiPredicate<ConfigurationUIEntryContext, WFConfigEntryBoolean<T>> validator = this.validator;
		if(validator == null) {
			isValid = null;
		}
		else {
			isValid = validator.test(context, this);
		}
		
		if(Boolean.TRUE.equals(isValid)) {
			drawable.setTint(UIColors.validColor);
		}
		else if(Boolean.FALSE.equals(isValid)) {
			drawable.setTint(UIColors.invalidColor);
		}
		else {
			//NO-OP
		}
		super.draw(batch, parentAlpha);
		drawable.removeTint();
		setColor(prevColor);
	}

	@Override
	public boolean test(ConfigurationUIEntryContext obj, WFConfigEntryBoolean<T> thiz) {
		if(validator == null) {
			return false;
		}
		return validator.test(obj, thiz);
	}

	public WFConfigEntryBoolean<T> setUndoButton(FancyImageButton<Runnable> undoButton) {
		Objects.requireNonNull(this.undoButton = undoButton);
		return this;
	}
	
	public WFConfigEntryBoolean<T> setResetButton(FancyImageButton<Runnable> resetButton) {
		Objects.requireNonNull(this.resetButton = resetButton);
		return this;
	}
	
}
