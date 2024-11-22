package com.wildermods.wilderforge.launch.ui.element;

import java.util.function.BiPredicate;
import java.util.function.Function;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.NiceTextField;
import com.badlogic.gdx.scenes.scene2d.ui.RuntimeSkin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.wildermods.wilderforge.api.mixins.v1.Cast;
import com.wildermods.wilderforge.api.modLoadingV1.config.ModConfigurationEntryBuilder.ConfigurationUIEntryContext;
import com.wildermods.wilderforge.launch.WilderForge;
import com.worldwalkergames.legacy.context.ClientDataContext.Skins;
import com.worldwalkergames.ui.AutoSwapDrawable;
import com.worldwalkergames.ui.Dropdown;

public class WFConfigEntryTextBox extends NiceTextField implements BiPredicate<ConfigurationUIEntryContext, WFConfigEntryTextBox> {

	private final ConfigurationUIEntryContext context;
	volatile BiPredicate<ConfigurationUIEntryContext, WFConfigEntryTextBox> validator = null;
	volatile Function<String, Object> valueBuilder = null;
	
	Color invalidColor = new Color(0xFF6496FF);
	Color validColor = new Color(0x64E664FF);
	
	TintedDrawable<AutoSwapDrawable> backgroundNeutral;
	TintedDrawable<AutoSwapDrawable> backgroundOver;
	TintedDrawable<AutoSwapDrawable> backgroundDown;
	TintedDrawable<AutoSwapDrawable> backgroundDisabled;
	
	ClickListener mouseListener = new ClickListener() {
		
	};
	
	InputListener keyListener = new InputListener() {
		@SuppressWarnings("incomplete-switch")
		@Override
		public boolean handle(Event e) {
			if(e instanceof InputEvent) {
				InputEvent event = Cast.from(e);
				switch(event.getType()) {
					case keyDown:
					case keyTyped:
					case keyUp:
						WilderForge.LOGGER.log("Key event");
						if(test(context, WFConfigEntryTextBox.this)) {
							Object val = buildFromString(context, WFConfigEntryTextBox.this);
							WilderForge.LOGGER.log("set value to " + val);
							context.setNewVal(val);
						}
						break;
				}
			}
			return false;
		}
	};
	
	public WFConfigEntryTextBox(ConfigurationUIEntryContext context, String text, RuntimeSkin skin) {
		this(context, text, skin, "default");
	}

	public WFConfigEntryTextBox(ConfigurationUIEntryContext context, String text, RuntimeSkin skin, String styleName) {
		super(text, skin, styleName);
		
		this.context = context;
		
		this.addListener(mouseListener);
		this.addListener(keyListener);
		
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
	}
	
	public WFConfigEntryTextBox setValidator(BiPredicate<ConfigurationUIEntryContext, WFConfigEntryTextBox> testFunc) {
		this.validator = testFunc;
		return this;
	}
	
	public WFConfigEntryTextBox setBuilder(Function<String, Object> valBuilder) {
		this.valueBuilder = valBuilder;
		return this;
	}
	
	@Override
	public boolean test(ConfigurationUIEntryContext obj, WFConfigEntryTextBox thiz) {
		if(validator == null || valueBuilder == null) {
			return false;
		}
		return validator.test(obj, thiz);
	}
	
	public Object buildFromString(ConfigurationUIEntryContext obj, WFConfigEntryTextBox thiz) {
		if(test(obj, thiz) && valueBuilder != null) {
			return valueBuilder.apply(thiz.getText());
		}
		throw new IllegalStateException();
	}
	
	public void setNewValue(Object o) {
		context.setNewVal(o);
	}
	
	private void addDrawables(String textureName) {
		this.backgroundNeutral.getParent().addOption(textureName + "_up");
		this.backgroundOver.getParent().addOption(textureName + "_over");
		this.backgroundDown.getParent().addOption(textureName + "_down");
		this.backgroundDisabled.getParent().addOption(textureName + "_disabled");
	}
	
	@Override
	protected TintedDrawable<AutoSwapDrawable> getBackgroundDrawable() {
		TintedDrawable<AutoSwapDrawable> drawable;
		if(this.isDisabled()) {
			return this.backgroundDisabled;
		}
		else if(this.mouseListener.isPressed()) {
			return this.backgroundDown;
		}
		else if (this.mouseListener.isOver()) {
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
		
		BiPredicate<ConfigurationUIEntryContext, WFConfigEntryTextBox> validator = this.validator;
		if(validator == null) {
			isValid = null;
		}
		else {
			isValid = validator.test(context, this);
		}
		
		if(Boolean.TRUE.equals(isValid)) {
			drawable.setTint(validColor);
			//setColor(Color.GREEN);
		}
		else if(Boolean.FALSE.equals(isValid)) {
			drawable.setTint(invalidColor);
			//setColor(Color.RED);
		}
		else {
			//NO-OP
		}
		super.draw(batch, parentAlpha);
		
		drawable.removeTint();
		setColor(prevColor);
	}
	
}
