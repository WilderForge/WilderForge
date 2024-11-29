package com.wildermods.wilderforge.launch.ui.element;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.NiceTextField;
import com.badlogic.gdx.scenes.scene2d.ui.RuntimeSkin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.wildermods.wilderforge.api.mixins.v1.Cast;
import com.wildermods.wilderforge.api.modLoadingV1.config.EntryValue;
import com.wildermods.wilderforge.api.modLoadingV1.config.ModConfigurationEntryBuilder.ConfigurationUIEntryContext;
import com.wildermods.wilderforge.launch.logging.Logger;
import com.wildermods.wilderforge.launch.exception.ConfigurationError;
import com.worldwalkergames.legacy.context.ClientDataContext.Skins;
import com.worldwalkergames.ui.AutoSwapDrawable;
import com.worldwalkergames.ui.Dropdown;
import com.worldwalkergames.ui.FancyImageButton;

public class WFConfigEntryTextBox<T> extends NiceTextField implements BiPredicate<ConfigurationUIEntryContext, WFConfigEntryTextBox<T>>, EntryValue<T> {

	private static final Logger LOGGER = new Logger(WFConfigEntryTextBox.class);
	private final ConfigurationUIEntryContext context;
	volatile BiPredicate<ConfigurationUIEntryContext, WFConfigEntryTextBox<T>> validator = null;
	volatile BiFunction<ConfigurationUIEntryContext, String, T> valueBuilder = null;
	volatile BiFunction<ConfigurationUIEntryContext, T, String> stringConverter = null;
	volatile FancyImageButton undoButton = null;
	volatile FancyImageButton resetButton = null;
	
	
	Color invalidColor = new Color(0xFF6496FF);
	Color validColor = new Color(0x64E664FF);
	
	TintedDrawable<AutoSwapDrawable> backgroundNeutral;
	TintedDrawable<AutoSwapDrawable> backgroundOver;
	TintedDrawable<AutoSwapDrawable> backgroundDown;
	TintedDrawable<AutoSwapDrawable> backgroundDisabled;
	
	ClickListener mouseListener = new ClickListener() {
		
	};
	
	InputListener keyListener = new InputListener() {

		@Override
		@SuppressWarnings("incomplete-switch")
		public boolean handle(Event e) {
			if(e instanceof Update) {
				update();
			}
			else if(e instanceof InputEvent) {
				InputEvent event = Cast.from(e);
				switch(event.getType()) {
					case keyDown:
					case keyTyped:
					case keyUp:
						update();
						break;
				}
			}
			return false;
		}
		
		private void update() {
			if(test(context, WFConfigEntryTextBox.this)) {
				Object val = buildFromString(context, WFConfigEntryTextBox.this);
				context.setNewVal(val);
			}
			
			if(undoButton != null) {
				if(!test(context, WFConfigEntryTextBox.this) || context.changed()) {
					undoButton.setDisabled(false);
				}
				else {
					undoButton.setDisabled(true);
				}
			}
			
			if(resetButton != null) {
				if(!test(context, WFConfigEntryTextBox.this) || !context.isDefault()) {
					resetButton.setColor(invalidColor);
					resetButton.setDisabled(false);
				}
				else {
					resetButton.setColor(Color.WHITE);
					resetButton.setDisabled(true);
				}
			}
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
	
	public WFConfigEntryTextBox<T> setValidator(BiPredicate<ConfigurationUIEntryContext, WFConfigEntryTextBox<T>> testFunc) {
		this.validator = testFunc;
		return this;
	}
	
	public WFConfigEntryTextBox<T> setBuilder(BiFunction<ConfigurationUIEntryContext, String, T> valBuilder) {
		this.valueBuilder = valBuilder;
		return this;
	}
	
	public WFConfigEntryTextBox<T> setStringConverter(BiFunction<ConfigurationUIEntryContext, T, String> stringConverter) {
		this.stringConverter = stringConverter;
		return this;
	}
	
	@Override
	public BiFunction<ConfigurationUIEntryContext, T, String> getStringConverter() {
		return stringConverter;
	}
	
	public WFConfigEntryTextBox<T> setUndoButton(FancyImageButton<Runnable> undoButton) {
		this.undoButton = undoButton;
		return this;
	}
	
	public WFConfigEntryTextBox<T> setResetButton(FancyImageButton<Runnable> resetButton) {
		this.resetButton = resetButton;
		return this;
	}
	
	public void update() {
		keyListener.handle(new Update());
	}
	
	@Override
	public boolean test(ConfigurationUIEntryContext obj, WFConfigEntryTextBox<T> thiz) {
		if(validator == null || valueBuilder == null) {
			return false;
		}
		return validator.test(obj, thiz);
	}
	
	public Object buildFromString(ConfigurationUIEntryContext obj, WFConfigEntryTextBox<T> thiz) {
		if(test(obj, thiz) && valueBuilder != null) {
			return valueBuilder.apply(obj, thiz.getText());
		}
		throw new IllegalStateException();
	}
	
	public String convertToString(ConfigurationUIEntryContext obj, T val) throws Throwable {
		return stringConverter.apply(obj, val);
	}
	
	@SuppressWarnings("unchecked")
	public String convertToString(ConfigurationUIEntryContext obj, WFConfigEntryTextBox<T> thiz) {
		if(stringConverter != null) {
			String ret;
			try {
				ret = convertToString(obj, (T)obj.obtainVal());
				setText(ret);
				return ret;
			}
			catch(Throwable t) {
				LOGGER.warn("Falling back to default value. Couldn't obtain string for field " + obj.getField().getName() + " in " + obj.getField().getDeclaringClass().getCanonicalName());
				LOGGER.catching(t);
				try {
					ret = convertToString(obj, (T)obj.getDefaultVal());
					setText(ret);
					return ret;
				}
				catch(Throwable t2) {
					LOGGER.fatal("Falling back to default value failed!");
					throw new ConfigurationError("Falling back to default value failed! Couldn't build string for field " + obj.getField().getName() + " in " + obj.getField().getDeclaringClass().getCanonicalName() , t2);
				}
			}
		}
		return "NOT_READY";
	}
	
	public void setNewValue(ConfigurationUIEntryContext obj, T val) {
		context.setNewVal(val);
		if(stringConverter != null) {
			setText(stringConverter.apply(obj, val));
		}
	}
	
	@Override
	public void setText(String text) {
		super.setText(text);
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
		
		BiPredicate<ConfigurationUIEntryContext, WFConfigEntryTextBox<T>> validator = this.validator;
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
	
	private static final class Update extends Event {}
	
}
