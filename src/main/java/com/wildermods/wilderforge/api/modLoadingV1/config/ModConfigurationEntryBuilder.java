package com.wildermods.wilderforge.api.modLoadingV1.config;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.RuntimeSkin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.wildermods.wilderforge.api.modLoadingV1.config.ConfigEntry.Range;
import com.wildermods.wilderforge.api.modLoadingV1.config.ConfigEntry.Step;
import com.wildermods.wilderforge.api.modLoadingV1.config.ConfigEntry.Range.Ranges;
import com.wildermods.wilderforge.api.modLoadingV1.config.ConfigEntry.Step.Steps;
import com.wildermods.wilderforge.api.modLoadingV1.CoremodInfo;
import com.wildermods.wilderforge.api.modLoadingV1.config.ConfigEntry.GUI.Localized;
import com.wildermods.wilderforge.api.modLoadingV1.config.ConfigEntry.GUI.Slider;
import com.wildermods.wilderforge.api.utils.TypeUtil;
import com.wildermods.wilderforge.launch.exception.ConfigElementException;
import com.wildermods.wilderforge.launch.exception.ConfigurationError;
import com.wildermods.wilderforge.launch.logging.Logger;
import com.wildermods.wilderforge.launch.ui.ModConfigurationPopup;
import com.worldwalkergames.legacy.context.ClientDataContext;
import com.worldwalkergames.legacy.context.LegacyViewDependencies;
import com.worldwalkergames.legacy.controller.NiceSlider;
import com.worldwalkergames.ui.AutoSwapDrawable;
import com.worldwalkergames.ui.FancySliderStyle;
import com.worldwalkergames.ui.NiceCheckBox;
import com.worldwalkergames.ui.NiceLabel;

@SuppressWarnings("rawtypes")
public class ModConfigurationEntryBuilder {
	
	protected final Logger LOGGER;
	protected final ConfigurationUIContext context;
	protected final ModConfigurationPopup popup;
	protected final LegacyViewDependencies dependencies;
	
	public ModConfigurationEntryBuilder(ConfigurationUIContext context) {
		this.LOGGER = new Logger(toString());
		this.context = context;
		this.popup = context.popup;
		this.dependencies = context.popup.getDependencies();
	}
	
	public void buildValueSpan(ConfigurationUIEntryContext context) {
		buildNameLabel(context);
		buildConnectingRule(context);
		try {
			Cell inputField = buildInputField(context);
			applyInputField(context, inputField);
		}
		catch(ConfigElementException e) {
			LOGGER.catching(e);
		}
	}

	public Cell buildNameLabel(ConfigurationUIEntryContext context) {
		LocalizationContext localization = new LocalizationContext(context);
		String name = localization.name();
		String tooltip = localization.tooltip();
		final Table valueSpanTable = context.valueSpanTable;
		final NiceLabel nameLabel = new NiceLabel(name, dependencies.skin, "darkInteractive");
		return valueSpanTable.add(nameLabel).align(Align.left);
	}
	
	public Cell buildHorizontalRule(ConfigurationUIEntryContext context) {
		final Table valueSpanTable = context.valueSpanTable;
		final AutoSwapDrawable dividerDrawer = new AutoSwapDrawable(dependencies.skin.getSisterSkin(ClientDataContext.Skins.SCALE_UI));
		dividerDrawer.addOption("dividerBar");
		dividerDrawer.addOption("dividerBar2x");
		final Image image = new Image(dividerDrawer, Scaling.stretch);
		return valueSpanTable.add(image).expandX().fillX();
	}
	
	public Cell buildConnectingRule(ConfigurationUIEntryContext context) {
		Cell cell = buildHorizontalRule(context);
		cell.minWidth(Value.percentWidth(0.3f));
		
		return cell;
	}
	
	public Cell buildInputField(ConfigurationUIEntryContext context) throws ConfigElementException {
		final Table valueSpanTable = context.valueSpanTable;
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
				if(TypeUtil.isFloat(f)) {
					return buildFloat(context, range, step);
				}
			}
		}
		
		return valueSpanTable.add();
	}
	
	public void applyInputField(ConfigurationUIEntryContext context, Cell cell) {
		final Table fieldTable = context.fieldTable;
		final Table valueSpanTable = context.valueSpanTable;
		fieldTable.add(valueSpanTable).align(Align.left).expandX().fillX();
		fieldTable.row();
	}
	
	public Cell buildBoolean(ConfigurationUIEntryContext context) throws ConfigElementException {
		return buildCheckbox(context);
	}
	
	public Cell buildCheckbox(ConfigurationUIEntryContext context) throws ConfigElementException {
		final Field f = context.field;
		final Object config = context.configurationObj;
		final Table valueSpanTable = context.valueSpanTable;
		try {
			NiceCheckBox checkbox = new NiceCheckBox("", dependencies.skin, "default");
			checkbox.setChecked(f.getBoolean(config));
			return valueSpanTable.add(checkbox);
		}
		catch(Exception e) {
			throw new ConfigElementException(context.popup.coremod, f, config, e);
		}
	}
	
	public Cell buildFloat(ConfigurationUIEntryContext context, Range range, Step step) throws ConfigElementException {
		if(context.field.getAnnotation(Slider.class) != null) {
			if(range.minDecimal() >= -1000 || range.maxDecimal() <= 1000) {
				return buildSlider(context, range, step);
			}
			else {
				throw new ConfigurationError("Slider @Range out of bounds or not present. Slider @Range boundaries must be between -1000 and 1000");
			}
		}
		else {
			Cell textInputCell = buildTextInput(context);
		}
	}
	
	public Cell buildSlider(ConfigurationUIEntryContext context, Range range, Step step) throws ConfigElementException {
		Table valueSpanTable = context.valueSpanTable;
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
				
				LOGGER.log("Min range: " + range.minDecimal());
				LOGGER.log("Current value: " + slider.getValue());
				LOGGER.log("Max range: " + range.maxDecimal());
				return valueSpanTable.add(slider).expandX().fillX();
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
	
	public Cell buildTextInput(ConfigurationUIEntryContext context) {
		
	}
	
	public static class ConfigurationUIContext {
		public final ModConfigurationPopup popup;
		public final Object configurationObj;
		
		public ConfigurationUIContext(ModConfigurationPopup popup, Object configurationObj) {
			this.popup = popup;
			this.configurationObj = configurationObj;
		}
	}
	
	public static class ConfigurationUIEntryContext extends ConfigurationUIContext {
		
		public final Table fieldTable;
		public Table valueSpanTable;
		public final Field field;
		public final LocalizationContext localization;
		
		public ConfigurationUIEntryContext(ModConfigurationPopup popup, Table fieldTable, Table valueSpanTable, Field f, Object configurationObj) {
			super(popup, configurationObj);
			this.fieldTable = fieldTable;
			this.valueSpanTable = valueSpanTable;
			this.field = f;
			this.localization = new LocalizationContext(this);
		}
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
