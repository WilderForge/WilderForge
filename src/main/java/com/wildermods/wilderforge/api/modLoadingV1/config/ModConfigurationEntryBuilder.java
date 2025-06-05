package com.wildermods.wilderforge.api.modLoadingV1.config;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.RuntimeSkin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import com.wildermods.wilderforge.api.modLoadingV1.config.ConfigEntry.Range;
import com.wildermods.wilderforge.api.modLoadingV1.config.ConfigEntry.Step;
import com.wildermods.wilderforge.api.modLoadingV1.config.ConfigEntry.Range.DecimalRange;
import com.wildermods.wilderforge.api.modLoadingV1.config.ConfigEntry.Range.DecimalRange.DoubleRange;
import com.wildermods.wilderforge.api.modLoadingV1.config.ConfigEntry.Range.DecimalRange.FloatRange;
import com.wildermods.wilderforge.api.modLoadingV1.config.ConfigEntry.Range.IntegralRange;
import com.wildermods.wilderforge.api.modLoadingV1.config.ConfigEntry.Range.RangeInstance;
import com.wildermods.wilderforge.api.modLoadingV1.config.ConfigEntry.Range.Ranges;
import com.wildermods.wilderforge.api.modLoadingV1.config.ConfigEntry.Restart;
import com.wildermods.wilderforge.api.modLoadingV1.config.ConfigEntry.Step.Steps;
import com.wildermods.provider.util.logging.Logger;
import com.wildermods.wilderforge.api.mixins.v1.Cast;
import com.wildermods.wilderforge.api.modLoadingV1.CoremodInfo;
import com.wildermods.wilderforge.api.modLoadingV1.config.ConfigEntry.GUI.CustomBuilder;
import com.wildermods.wilderforge.api.modLoadingV1.config.ConfigEntry.GUI.Localized;
import com.wildermods.wilderforge.api.modLoadingV1.config.ConfigEntry.GUI.Slider;
import com.wildermods.wilderforge.api.modLoadingV1.config.ConfigEntry.Nullable;
import com.wildermods.wilderforge.api.utils.TypeUtil;
import com.wildermods.wilderforge.launch.WilderForge;
import com.wildermods.wilderforge.launch.coremods.Coremods;
import com.wildermods.wilderforge.launch.exception.ConfigElementException;
import com.wildermods.wilderforge.launch.exception.ConfigurationError;
import com.wildermods.wilderforge.launch.ui.ModConfigurationPopup;
import com.wildermods.wilderforge.launch.ui.element.WFConfigEntryBoolean;
import com.wildermods.wilderforge.launch.ui.element.WFConfigEntryTextBox;

import com.worldwalkergames.legacy.context.ClientDataContext;
import com.worldwalkergames.legacy.context.GameStrings;
import com.worldwalkergames.legacy.context.LegacyViewDependencies;
import com.worldwalkergames.legacy.context.LegacyViewDependencies.ScreenInfo;
import com.worldwalkergames.legacy.controller.NiceSlider;
import com.worldwalkergames.ui.FancyImageButton;
import com.worldwalkergames.ui.FancySliderStyle;
import com.worldwalkergames.ui.NiceButtonBase.ButtonStyle;
import com.worldwalkergames.ui.NiceButtonBase.FancyButtonStyle;
import com.worldwalkergames.ui.NiceLabel;
import com.worldwalkergames.ui.tooltips.TooltipManager;

@SuppressWarnings("rawtypes")
public class ModConfigurationEntryBuilder {
	protected final Logger LOGGER;
	protected final ConfigurationUIContext context;
	protected final ModConfigurationPopup popup;
	protected final LegacyViewDependencies dependencies;
	protected final GameStrings gameStrings;
	protected final TooltipManager tooltipManager;
	
	public ModConfigurationEntryBuilder(ConfigurationUIContext context) {
		this.LOGGER = new Logger(toString());
		this.context = context;
		this.popup = context.popup;
		this.dependencies = context.popup.getDependencies();
		this.gameStrings = dependencies.gameStrings;
		this.tooltipManager = dependencies.tooltipManager;
	}
	
	public final void delegateBuildValueSpan(ConfigurationUIEntryContext context) {
		CustomBuilder builder = context.field.getAnnotation(CustomBuilder.class);
		if(builder != null) {
			try {
				Class<Function<ConfigurationUIEntryContext, ? extends ModConfigurationEntryBuilder>> clazz = Cast.from(builder.value());
				Constructor<Function<ConfigurationUIEntryContext, ? extends ModConfigurationEntryBuilder>> constructor = clazz.getDeclaredConstructor();
				constructor.setAccessible(true);
				Function<? extends ConfigurationUIContext, ? extends ModConfigurationEntryBuilder> func = constructor.newInstance();
				ModConfigurationEntryBuilder customBuilder = func.apply(Cast.from(context));
				customBuilder.buildValueSpan(context);
			}
			catch(Throwable t) {
				LOGGER.catching(t);
			}
			return;
		}
		buildValueSpan(context);
	}
	
	@SuppressWarnings({ "unchecked" })
	public void buildValueSpan(ConfigurationUIEntryContext context) {
		try {
			
			buildNameLabel(context);
		

			Cell inputField = buildInputField(context);
			FancyImageButton<Runnable> undoButton = buildUndo(context, inputField.getActor());
			FancyImageButton<Runnable> resetButton = buildReset(context, inputField.getActor());
			String undoLocalization = "wilderforge.ui.coremods.configure.entry.button.undo.tooltip";
			String resetLocalization = "wilderforge.ui.coremods.configure.entry.button.reset.tooltip";
			
			if(!(inputField.getActor() instanceof EntryValue)) {
				undoLocalization = undoLocalization + ".fallback";
				resetLocalization = resetLocalization + ".fallback";
				tooltipManager.autoTooltip(undoButton, gameStrings.ui(undoLocalization));
				tooltipManager.autoTooltip(resetButton, gameStrings.ui(resetLocalization));
			}
			else {
				EntryValue entryValue = (EntryValue) inputField.getActor();
				try {
					tooltipManager.autoTooltip(undoButton, gameStrings.ui(undoLocalization, entryValue.convertToString(context, context.oldVal)));
					tooltipManager.autoTooltip(resetButton, gameStrings.ui(resetLocalization, entryValue.convertToString(context, context.defaultVal)));
				}
				catch(Throwable t) {
					//The errors should have already been thrown way before now.
					throw new AssertionError(t);
				}
			}
			applyInputField(context, inputField, undoButton, resetButton);
		}
		catch(ConfigElementException e) {
			LOGGER.catching(e);
		}
	}

	public Cell buildNameLabel(ConfigurationUIEntryContext context) {
		LocalizationContext localization = new LocalizationContext(context);
		String name = localization.name();
		final Table fieldTable = context.fieldTable;
		final NiceLabel nameLabel = new NiceLabel(name, dependencies.skin, "darkInteractive");
		buildTooltip(nameLabel, context);
		return fieldTable.add(nameLabel).align(Align.right);
	}
	
	public Cell buildInputField(ConfigurationUIEntryContext context) throws ConfigElementException {
		final Table fieldTable = context.fieldTable;
		final Field f = context.field;
		
		if(TypeUtil.isBoolean(f)) {
			try {
				return buildBoolean(context);
			} catch (ConfigElementException e) {
				LOGGER.catching(e);
			}
		}
		else if(TypeUtil.isNumeric(f)) {
			Range range = f.getAnnotation(Range.class);
			Step step = f.getAnnotation(Step.class);
			if(range == null) {
				range = Ranges.getRangeOfType(f);
			}
			if(step == null) {
				step = Steps.getStepOfType(f);
			}
			if(TypeUtil.isDecimal(f)) {
				RangeInstance dRange = Ranges.getRange(f);
				if(TypeUtil.isFloat(f)) {
					return buildFloat(context, (FloatRange)dRange, step);
				}
				else if (TypeUtil.isDouble(f)) {
					return buildDouble(context, (DoubleRange)dRange, step);
				}
				else {
					throw new AssertionError();
				}
			}
			else if(TypeUtil.isIntegral(f)) {
				IntegralRange iRange = new IntegralRange(range);
				if(TypeUtil.isLong(f)) {
					return buildLong(context, iRange, step);
				}
				else if(TypeUtil.isInt(f)) {
					return buildInt(context, iRange, step);
				}
				else if(TypeUtil.isChar(f)) {
					return buildChar(context, iRange, step);
				}
				else if(TypeUtil.isShort(f)) {
					return buildShort(context, iRange, step);
				}
				else if(TypeUtil.isByte(f)) {
					return buildByte(context, iRange, step);
				}
				else {
					throw new AssertionError();
				}
			}
			else {
				throw new AssertionError();
			}
		}
		else if(f.getType() == String.class) {
			return buildString(context);
		}
		
		return fieldTable.add();
	}
	
	public void applyInputField(ConfigurationUIEntryContext context, Cell cell, FancyImageButton<Runnable> undoButton, FancyImageButton<Runnable> resetButton) {		
		cell.width(Value.percentWidth(0.333f, context.popup.getFrame())).align(Align.left);
		buildTooltip(cell.getActor(), context);
		Actor actor = cell.getActor();
		if(actor instanceof WFConfigEntryTextBox) {
			WFConfigEntryTextBox<?> box = Cast.from(actor);
			box.setUndoButton(undoButton);
			box.setResetButton(resetButton);
			ClickListener uiUpdater = new ClickListener () {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					box.update();
				}
			};
			undoButton.addListener(uiUpdater);
			resetButton.addListener(uiUpdater);
			box.update();
		}
		if(actor instanceof WFConfigEntryBoolean) {
			WFConfigEntryBoolean<?> box = Cast.from(actor);
			box.setUndoButton(undoButton);
			box.setResetButton(resetButton);
			box.setState(context.getNewVal(Boolean.class));
		}
		context.fieldTable.row();
	}
	
	public FancyImageButton<Runnable> buildUndo(ConfigurationUIEntryContext context, Actor actor) {
		ScreenInfo screenInfo = context.popup.getDependencies().screenInfo;
		RuntimeSkin skin = context.popup.getDependencies().skin.getSisterSkin(ClientDataContext.Skins.SCALE_UI);
		FancyImageButton<Runnable> undoButton = new FancyImageButton<>(skin, screenInfo.scale(6f), "icon_hudTop_undo", "icon_hudTop_undo2x");
		undoButton.setStyle(ButtonStyle.fancy(FancyButtonStyle.dark));
		
		@SuppressWarnings("unchecked")
		Runnable exec = () -> {
			context.undo();
			if(actor instanceof WFConfigEntryTextBox) {
				WFConfigEntryTextBox box = Cast.from(actor);
				box.convertToString(context, box);
			}
			else if(actor instanceof WFConfigEntryBoolean) {
				WFConfigEntryBoolean box = Cast.from(actor);
				box.setState(Cast.from(context.getOldVal(Boolean.class)));
			}
		};
		
		undoButton.setUserData(exec);
		undoButton.clicked.add(this, () -> {
			undoButton.getUserData().run();
		});
		context.fieldTable.add(undoButton).expandY().fillY().width(Value.percentWidth(1f / 16f, context.fieldTable));
		undoButton.setDisabled(!context.changed());
		return undoButton;

	}
	
	public FancyImageButton<Runnable> buildReset(ConfigurationUIEntryContext context, Actor actor) {
		ScreenInfo screenInfo = context.popup.getDependencies().screenInfo;
		RuntimeSkin skin = context.popup.getDependencies().skin.getSisterSkin(ClientDataContext.Skins.SCALE_UI);
		FancyImageButton<Runnable> resetButton = new FancyImageButton<>(skin, screenInfo.scale(6f), "icon_x", "icon_x2x");
		
		@SuppressWarnings("unchecked")
		Runnable exec = () -> {
			context.resetToDefault();
			if(actor instanceof WFConfigEntryTextBox) {
				WFConfigEntryTextBox box = Cast.from(actor);
				box.convertToString(context, box);
			}
			else if(actor instanceof WFConfigEntryBoolean) {
				WFConfigEntryBoolean box = Cast.from(actor);
				box.setState(Cast.from(context.getDefaultVal()));
			}
		};
		
		resetButton.setUserData(exec);
		resetButton.clicked.add(this, () -> {
			resetButton.getUserData().run();
		});
		context.fieldTable.add(resetButton).expandY().fillY().width(Value.percentWidth(1f / 16f, context.fieldTable));
		resetButton.setDisabled(context.isDefault());
		return resetButton;

	}
	
	public Cell buildBoolean(ConfigurationUIEntryContext context) throws ConfigElementException {
		
		Cell<WFConfigEntryBoolean<Boolean>> ret = buildToggleInput(context, 
			(c, boolbox) -> {
				return boolbox != null;
			}
		);
		
		boolean val;
		
		try {
			val = context.getNewVal(boolean.class);
		}
		catch(ConfigurationError e) {
			LOGGER.catching(e);
			val = false;
		}
		
		ret.getActor().setText(val + "");
		return ret;
	}
	
	public Cell buildFloat(ConfigurationUIEntryContext context, DecimalRange range, Step step) throws ConfigElementException {
		if(context.field.getAnnotation(Slider.class) != null) {
			if(range.minDecimal() >= -1000 || range.maxDecimal() <= 1000) {
				return buildSlider(context, range, step);
			}
			else {
				throw new ConfigurationError("Slider @Range out of bounds or not present. Slider @Range boundaries must be between -1000 and 1000");
			}
		}
		Cell<WFConfigEntryTextBox<Float>> ret = buildTextInput(context, 
			(c, textBox) -> {
				float val;
				if(textBox == null) {
					return false;
				}
				
				//We set a local instance of text just in case another thread modifies it while we're checking
				String text = textBox.getText(); 
				if(text == null) {
					return false;
				}
				
				
				try {
					val = Float.parseFloat(text);
				}
				catch(NumberFormatException e) {
					return false;
				}
				return range.contains(val);
			},
			(c, input) -> {
				return Float.parseFloat(input);
			},
			(c, floatVal) -> {
				return floatVal.toString();
			}
		);
		
		float val;
		try {
			val = context.getNewVal(float.class);
		}
		catch(ConfigurationError e) {
			LOGGER.catching(e);
			if(range.contains(0f)) {
				val = 0f;
			}
			else {
				val = (float) range.minDecimal();
			}
		}
		ret.getActor().setText(val + "");
		return ret;
	}
	
	public Cell buildDouble(ConfigurationUIEntryContext context, DecimalRange range, Step step) throws ConfigElementException {
		Cell<WFConfigEntryTextBox<Double>> ret = buildTextInput(context, 
			(c, textBox) -> {
				double val;
				
				if(textBox == null) {
					return false;
				}
				
				String text = textBox.getText();
				if(text == null) {
					return false;
				}
				
				try {
					val = Double.parseDouble(textBox.getText());
				} 
				catch(NumberFormatException e) {
					return false;
				}
				return range.contains(val);
				
			},
			(c, input) -> {
				return Double.parseDouble(input);
			},
			(c, doubleVal) -> {
				return doubleVal.toString();
			}
		);
		
		double val;
		try {
			val = context.getNewVal(double.class);
		}
		catch(ConfigurationError e) {
			LOGGER.catching(e);
			if(range.contains(0d)) {
				val = 0d;
			}
			else {
				val = range.minDecimal();
			}
		}
		ret.getActor().setText(val + "");
		return ret;
	}
	
	public Cell buildLong(ConfigurationUIEntryContext context, IntegralRange range, Step step) throws ConfigElementException {
		Cell<WFConfigEntryTextBox<Long>> ret = buildTextInput(context, 
			(c, textBox) -> {
				long val;
				
				if(textBox == null) {
					return false;
				}
				
				String text = textBox.getText();
				if(text == null) {
					return false;
				}
				
				try {
					val = Long.parseLong(textBox.getText());
				}
				catch(NumberFormatException e) {
					return false;
				}
				return range.contains(val);
			},
			(c, input) -> {
				return Long.parseLong(input);
			},
			(c, longVal) -> {
				return longVal.toString();
			}
		);
		
		long val;
		try {
			val = context.getNewVal(long.class);
		}
		catch(ConfigurationError e) {
			LOGGER.catching(e);
			if(range.contains(0l)) {
				val = 0l;
			}
			else {
				val = range.min();
			}
		}
		ret.getActor().setText(val + "");
		return ret;
	}
	
	public Cell buildInt(ConfigurationUIEntryContext context, IntegralRange range, Step step) throws ConfigElementException {
		Cell<WFConfigEntryTextBox<Integer>> ret = buildTextInput(context, 
			(c, textBox) -> {
				int val;
				
				if(textBox == null) {
					return false;
				}
				
				String text = textBox.getText();
				if(text == null) {
					return false;
				}
				
				try {
					val = Integer.parseInt(text);
				}
				catch(NumberFormatException e) {
					return false;
				}
				return range.contains(val);
			},
			(c, input) -> {
				return Integer.parseInt(input);
			},
			(c, intVal) -> {
				return intVal.toString();
			}
		);
		
		int val;
		try {
			val = context.getNewVal(int.class);
		}
		catch(ConfigurationError e) {
			LOGGER.catching(e);
			if(range.contains(0)) {
				val = 0;
			}
			else {
				val = (int)range.min();
			}
		}
		ret.getActor().setText(val + "");
		return ret;
	}
	
	public Cell buildChar(ConfigurationUIEntryContext context, IntegralRange range, Step step) throws ConfigElementException {
		Cell<WFConfigEntryTextBox<Character>> ret = buildTextInput(context, 
			(c, textBox) -> {
				int val;
				
				if(textBox == null) {
					return false;
				}
				
				String text = textBox.getText();
				if(text == null) {
					return false;
				}
				
				try {
					val = Integer.parseInt(text);
				}
				catch(NumberFormatException e) {
					return false;
				}
				return range.contains(val) && Ranges.CHAR.contains(val);
			},
			(c, input) -> {
				int val = Integer.parseInt(input);
				IntegralRange charRange = Ranges.CHAR;
				if(charRange.contains(val)) {
					return (char)val;
				}
				throw new NumberFormatException(val + "");
			},
			(c, charVal) -> {
				int val = (int) charVal;
				return val + "";
			}
		);
		
		int val;
		try {
			val = (int)context.getNewVal(char.class);
		}
		catch(ConfigurationError e) {
			LOGGER.catching(e);
			if(range.contains(0)) {
				val = 0;
			}
			else {
				val = (int)range.min();
			}
		}
		ret.getActor().setText(val + "");
		return ret;
	}
	
	public Cell buildShort(ConfigurationUIEntryContext context, IntegralRange range, Step step) throws ConfigElementException {
		Cell<WFConfigEntryTextBox<Short>> ret = buildTextInput(context, 
			(c, textBox) -> {
				short val;
				
				if(textBox == null) {
					return false;
				}
				
				String text = textBox.getText();
				if(text == null) {
					return false;
				}
				
				try {
					val = Short.parseShort(text);
				}
				catch(NumberFormatException e) {
					return false;
				}
				return range.contains(val);
			},
			(c, input) -> {
				return Short.parseShort(input);
			},
			(c, shortVal) -> {
				return shortVal.toString();
			}
		);
		
		short val;
		try {
			val = context.getNewVal(short.class);
		}
		catch(ConfigurationError e) {
			LOGGER.catching(e);
			if(range.contains(0)) {
				val = 0;
			}
			else {
				val = (short)range.min();
			}
		}
		ret.getActor().setText(val + "");
		return ret;
	}
	
	public Cell buildByte(ConfigurationUIEntryContext context, IntegralRange range, Step step) throws ConfigElementException {
		Cell<WFConfigEntryTextBox<Byte>> ret = buildTextInput(context, 
			(c, textBox) -> {
				byte val;
				
				if(textBox == null) {
					return false;
				}
				
				String text = textBox.getText();
				if(text == null) {
					return false;
				}
				
				try {
					val = Byte.parseByte(text);
				}
				catch(NumberFormatException e) {
					return false;
				}
				return range.contains(val);
			},
			(c, input) -> {
				return Byte.parseByte(input);
			},
			(c, byteVal) -> {
				return byteVal.toString();
			}
		);
		
		byte val;
		try { 
			val = context.getNewVal(byte.class);
		}
		catch(ConfigurationError e) {
			LOGGER.catching(e);
			
			if(range.contains(0)) {
				val = 0;
			}
			else {
				val = (byte)range.min();
			}
		}
		ret.getActor().setText(val + "");
		return ret;
	}
	
	public Cell buildString (ConfigurationUIEntryContext context) throws ConfigElementException {
		Cell<WFConfigEntryTextBox<String>> ret = buildTextInput(context,
			(c, textBox) -> {
				if(textBox == null) {
					return false;
				}

				return true;
			},
			(c, input) -> {
				return input;
			},
			(c, stringVal) -> {
				return stringVal;
			}
				
		);
		
		String val;

		val = context.getNewVal(String.class);

		ret.getActor().setText(val);
		return ret;
	}
	
	public Cell buildSlider(ConfigurationUIEntryContext context, Range range, Step step) throws ConfigElementException {
		Table fieldTable = context.fieldTable;
		final Field f = context.field;
		final CoremodInfo coremod = context.popup.coremod;
		final Object config = context.configurationObj;
		try {
			if(!TypeUtil.isFloat(f.getType())) {
				throw new ConfigElementException(coremod, f, config, "Sliders only support float values at this time.");
			}
			RuntimeSkin scaleSkin = dependencies.skin.getSisterSkin(ClientDataContext.Skins.SCALE_UI);
			FancySliderStyle style = new FancySliderStyle(scaleSkin);
			
			boolean decimal;
			decimal = step == Steps.DECIMAL;
			
			if(decimal) {
				NiceSlider slider = new NiceSlider((float)range.minDecimal(), (float)range.maxDecimal(), (float)step.value(), false, style);
				slider.setValue(f.getFloat(config));
				slider.setDisabled(false);
				slider.addListener(new ChangeListener() {

					@Override
					public void changed(ChangeEvent arg0, Actor arg1) {
						// TODO Auto-generated method stub
						
					}
					
				});
				

				return fieldTable.add(slider).expandX().fillX();
			}
			throw new AssertionError("Unreachable code reached");
		}
		catch(ConfigElementException e) {
			throw e;
		}
		catch(Throwable t) {
			throw new ConfigElementException(coremod, f, config, t);
		}
	}
	
	public <T> Cell<WFConfigEntryTextBox<T>> buildTextInput(ConfigurationUIEntryContext context, BiPredicate<ConfigurationUIEntryContext, WFConfigEntryTextBox<T>> validator, BiFunction<ConfigurationUIEntryContext, String, T> builder, BiFunction<ConfigurationUIEntryContext, T, String> converter) {
		Table fieldTable = context.fieldTable;
		final WFConfigEntryTextBox<T> textField = new WFConfigEntryTextBox<>(context, "", dependencies.skin);
		textField.setValidator(validator);
		textField.setBuilder(builder);
		textField.setStringConverter(converter);
		textField.test(context, textField);
		textField.setAlignment(Align.center);
		textField.setTextFieldListener((tfield, c) -> {
			WFConfigEntryTextBox<T> tbox = Cast.from(tfield);
			if(tbox.test(context, tbox)) {
				context.setNewVal(tbox.buildFromString(context, textField));
			}
		});
		return fieldTable.add(textField);
	}
	
	public <T> Cell<WFConfigEntryBoolean<T>> buildToggleInput(ConfigurationUIEntryContext context, BiPredicate<ConfigurationUIEntryContext, WFConfigEntryBoolean<T>> validator) {
		Table fieldTable = context.fieldTable;
		final WFConfigEntryBoolean<T> booleanField = new WFConfigEntryBoolean<>(context, "", dependencies.skin);
		
		booleanField.setValidator(validator);

		booleanField.test(context, booleanField);
		
		return fieldTable.add(booleanField);
	}
	
	public void buildTooltip(Actor actor, ConfigurationUIEntryContext context) {
		final StringBuilder s = new StringBuilder();
		final Field field = context.field;
		s.append(context.localization.tooltip());
		
		Range range = field.getAnnotation(Range.class);
		if(range == null) {
			range = Ranges.getRangeOfType(field);
		}
		
		if(range != null) {
			if(!s.toString().isBlank()) {
				s.append("\n\n");
			}
			s.append(gameStrings.ui("wilderforge.ui.coremods.configure.range.tooltip", Ranges.getMinimum(range).toString(), Ranges.getMaximum(range).toString()));
		}
		
		Nullable nullable = field.getAnnotation(Nullable.class);
		if(nullable != null) {
			if(!s.toString().isBlank()) {
				s.append("\n\n");
			}
			s.append(gameStrings.ui("wilderforge.ui.coremods.configure.nullable.tooltip"));
		}
		
		if(TypeUtil.isBoolean(field)) {
			if(!s.toString().isBlank()) {
				s.append("\n\n");
			}
			s.append(gameStrings.ui("wilderforge.ui.coremods.configure.boolean.tooltip"));
		}
		
		Restart restart = field.getAnnotation(Restart.class);
		if(restart != null) {
			
			if(!s.toString().isBlank()) {
				s.append("\n\n");
			}
			
			if(restart.immediate()) {
				if(!restart.prompt()) {
					s.append(gameStrings.ui("wilderforge.ui.coremods.configure.restart.tooltip.immediate"));
				}
				else {
					s.append(gameStrings.ui("wilderforge.ui.coremods.configure.restart.tooltip.hard"));
				}
			}
			else {
				s.append(gameStrings.ui("wilderforge.ui.coremods.configure.restart.tooltip.soft"));
			}
		}
		
		String tooltip = s.toString();
		if(!tooltip.isBlank()) {
			tooltipManager.autoTooltip(actor, tooltip);
		}
	}
	
	public static class ConfigurationContext implements Config {
		public final Config config;
		public final Object configurationObj;
		
		public ConfigurationContext(Config config, Object configurationObj) {
			this.config = config;
			this.configurationObj = configurationObj;
		}
		
		public CoremodInfo getCoremod() {
			return Coremods.getCoremod(modid());
		}

		@Override
		public String modid() {
			return config.modid();
		}
		
		@Override
		public Class<? extends Annotation> annotationType() {
			return null;
		}
	}
	
	public static class ConfigurationUIContext extends ConfigurationContext {
		public final ModConfigurationPopup popup;
		
		public ConfigurationUIContext(Config config, ModConfigurationPopup popup, Object configurationObj) {
			super(config, configurationObj);
			this.popup = popup;
		}
		
	}
	
	public static class ConfigurationUIEntryContext extends ConfigurationUIContext implements ConfigurationFieldContext {
		
		public ConfigEntry entry;
		public Table fieldTable;
		public final Field field;
		protected final Object defaultVal;
		protected final Object oldVal;
		protected Object newVal;
		public final LocalizationContext localization;
		
		public ConfigurationUIEntryContext(Config config, ConfigEntry entry, ModConfigurationPopup popup, Table fieldTable, Field f, Object configurationObj, Object defaultObj) {
			super(config, popup, configurationObj);
			this.entry = entry;
			this.fieldTable = fieldTable;
			this.field = f;
			this.localization = new LocalizationContext(this);
			try {
				this.defaultVal = field.get(defaultObj);
				this.oldVal = field.get(configurationObj);
			}
			catch(IllegalArgumentException | IllegalAccessException e) {
				throw new ConfigurationError("Could not get field" + field.getName(), e);
			}
			this.newVal = oldVal;
		}

		public Object obtainVal() {
			return getNewVal(Object.class);
		}
		
		public <T> T getOldVal(Class<T> type) {
			return Cast.from(oldVal);
		}
		
		public void setNewVal(Object newVal) {
			this.newVal = newVal;
		}
		
		public <T> T getNewVal(Class<T> type) {
			return Cast.from(newVal);
		}
		
		public Object getDefaultVal() {
			return defaultVal;
		}
		
		public void undo() {
			this.newVal = oldVal;
		}
		
		public void resetToDefault() {
			this.newVal = defaultVal;
		}
		
		public boolean changed() {
			ConfigEntry entry = field.getAnnotation(ConfigEntry.class);
			if(entry == null || !entry.strict()) {
				boolean equals = !Objects.equals(oldVal, newVal);
				return !Objects.equals(oldVal, newVal);
			}
			return oldVal == newVal;
		}
		
		public boolean isDefault() {
			ConfigEntry entry = field.getAnnotation(ConfigEntry.class);
			if(entry == null || !entry.strict()) {
				boolean equals = Objects.equals(defaultVal, newVal);
				return Objects.equals(defaultVal, newVal);
			}
			return defaultVal == newVal;
		}
		
		public Field getField() {
			return field;
		}
		
		/**
		 * @return true if the this object and the parameter represent the same field
		 */
		@Override
		public boolean equals(Object o) {
			if(o instanceof ConfigurationUIEntryContext) {
				if(field.equals(((ConfigurationUIEntryContext)o).field)) {
					WilderForge.LOGGER.log(field + ".equals" + ((ConfigurationUIEntryContext)o).field);
					return true;
				}
				else {
					WilderForge.LOGGER.log("!" + field + ".equals" + ((ConfigurationUIEntryContext)o).field);
				}
			}
			return false;
		}
		
		@Override
		public int hashCode() {
			return field.hashCode();
		}

	}
	
	public static interface ConfigurationFieldContext {
		public Object obtainVal();
		public <T> T getOldVal(Class<T> type);
		public void setNewVal(Object newVal);
		public <T> T getNewVal(Class<T> type);
		public Field getField();
	}
	
	public static class LocalizationContext implements Localized {

		private static final Localized NO_LOCALIZATION = new Localized() {
			@Override
			public Class<? extends Annotation> annotationType() {
				return null;
			}

			@Override
			public String nameLocalizer() {
				return "";
			}

			@Override
			public String tooltipLocalizer() {
				return "";
			}
		};
		
		protected final ConfigurationUIEntryContext context;
		protected Localized localization;
		
		public LocalizationContext(ConfigurationUIEntryContext context) {
			this.context = context;
			this.localization = context.field.getAnnotation(Localized.class);
			if(localization == null) {
				localization = NO_LOCALIZATION;
			}
		}
		
		@Override
		public final Class<? extends Annotation> annotationType() {
			return null;
		}

		@Override
		public String nameLocalizer() {
			return localization.nameLocalizer();
		}

		@Override
		public String tooltipLocalizer() {
			return localization.tooltipLocalizer();
		}

		public String name() {
			String name = context.field.getName();
			if(!nameLocalizer().isBlank()) {
				name = context.popup.I18N.ui(nameLocalizer());
			}
			else {
				ConfigEntry configEntry = context.field.getAnnotation(ConfigEntry.class);
				if(configEntry != null && !configEntry.name().isBlank()) {
					name = configEntry.name();
				}
			}
			return name;
		}
		
		public String tooltip() {
			String tooltip = tooltipLocalizer();
			if(!tooltip.isBlank()) {
				tooltip = context.popup.I18N.ui(tooltipLocalizer());
			}
			return tooltip;
		}
		
	}
}
