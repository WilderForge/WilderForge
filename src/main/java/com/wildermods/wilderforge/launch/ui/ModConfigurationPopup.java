package com.wildermods.wilderforge.launch.ui;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Function;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.RuntimeSkin;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane2;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.wildermods.wilderforge.api.mixins.v1.Cast;
import com.wildermods.wilderforge.api.modLoadingV1.CoremodInfo;
import com.wildermods.wilderforge.api.modLoadingV1.MissingCoremod;
import com.wildermods.wilderforge.api.modLoadingV1.config.Config;
import com.wildermods.wilderforge.api.modLoadingV1.config.ConfigEntry.GUI.CustomBuilder;
import com.wildermods.wilderforge.api.modLoadingV1.config.ModConfigurationEntryBuilder;
import com.wildermods.wilderforge.api.modLoadingV1.config.ModConfigurationEntryBuilder.ConfigurationUIContext;
import com.wildermods.wilderforge.api.modLoadingV1.config.ModConfigurationEntryBuilder.ConfigurationUIEntryContext;
import com.wildermods.wilderforge.launch.coremods.Configuration;
import com.wildermods.wilderforge.launch.coremods.Coremods;
import com.wildermods.wilderforge.launch.exception.ConfigurationError;
import com.worldwalkergames.legacy.context.GameStrings;
import com.worldwalkergames.legacy.context.LegacyViewDependencies;
import com.worldwalkergames.legacy.game.common.ui.OptionButton;
import com.worldwalkergames.legacy.options.Keymap;
import com.worldwalkergames.legacy.ui.DialogFrame;
import com.worldwalkergames.legacy.ui.PopUp;
import com.worldwalkergames.legacy.ui.menu.OptionsDialog.Style;

public class ModConfigurationPopup extends PopUp {
	public final CoremodInfo coremod;
	private Object configuration;
	private DialogFrame frame;
	private ScrollPane2 scrollPane;
	private Style style;
	public final GameStrings I18N = dependencies.gameStrings;
	private final Function<ConfigurationUIContext, ? extends ModConfigurationEntryBuilder> builder;
	private HashSet<Field> configFields = new LinkedHashSet<>();
	
	/*
	* This looks unusual, but using a HashMap with the same key and value is most optimal because we need:
	* 
	* 1. Uniqueness of objects in the collection
	* 2. Check if a ConfigurationUIEntryContext already exists before using a new one. (retrieved via containsKey())
	* 3. Retrieve the existing context if it does exist instead of using the new context (via get())
	* 
	* Using the same object as both the key and value allows efficient lookups and retrievals, which a HashSet alone cannot provide.
	*/
	private HashMap<ConfigurationUIEntryContext, ConfigurationUIEntryContext> values = new LinkedHashMap<>();
	
	private final ConfigurationUIContext context;
	
	public ModConfigurationPopup(LegacyViewDependencies dependencies, Config config) {
		super(true, dependencies);
		this.dependencies = dependencies;
		if(config instanceof CoremodInfo) {
			coremod = Cast.from(config);
		}
		else {
			coremod = Coremods.getCoremod(config.modid());
			if(coremod instanceof MissingCoremod) {
				throw new AssertionError();
			}
		}
		
		configuration = Configuration.getConfig(config);
		if(configuration == null) {
			throw new AssertionError();
		}
		
		context = new ConfigurationUIContext(this, configuration);
		builder = getBuilder(configuration.getClass().getAnnotation(CustomBuilder.class), context);
		
		configFields.addAll(List.of(configuration.getClass().getDeclaredFields()));
		
		Iterator<Field> iterator = configFields.iterator();
		while(iterator.hasNext()) {
			Field f = iterator.next();
			int modifier = f.getModifiers();
			if(f.isSynthetic() || Modifier.isStatic(modifier) || Modifier.isTransient(modifier)) {
				iterator.remove();
			}
			f.setAccessible(true);
		}
	}
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	private Function<ConfigurationUIContext, ? extends ModConfigurationEntryBuilder> getBuilder(CustomBuilder builder, ConfigurationUIContext context) {
		if(builder == null && this.builder == null) {
			return new CustomBuilder.DefaultConfigurationBuilder();
		}
		else if(builder == null && this.builder != null) {
			return this.builder;
		}
		try {
			Class clazz = builder.value();
			Constructor<Function> constructor = clazz.getDeclaredConstructor();
			constructor.setAccessible(true);
			Function func = constructor.newInstance();
			return func;
		}
		catch(Throwable t) {
			throw new ConfigurationError("Unable to create builder function for mod " + context.popup.coremod, t);
		}
	}

	@Override
	public void build() {
		if(frame != null) {
			group.removeActor(frame);
		}
		frame = new DialogFrame(dependencies);
		RuntimeSkin skin = dependencies.skin;
		style = skin.get(Style.class);
		
		frame.preferredWidth = style.f("dialogWidth");
		
		
		
		float padLeft = style.f("padLeft");
		float padRight = style.f("padRight");
		
		String title = I18N.ui("wilderforge.ui.coremods.configure.title", coremod.name);
		Label titleLabel = new Label(title, skin, "dialogTitle");
		titleLabel.setAlignment(Align.topLeft);
		frame.addInner(titleLabel).expandX().left().padLeft(padLeft).padBottom(style.f("titlePadBottom")).padRight(-padRight);
		
		Table fieldTable = new Table().padLeft(padLeft).padRight(padRight);
		
		for(Field f : configFields) {
			Function<ConfigurationUIContext, ? extends ModConfigurationEntryBuilder> builderObtainer = getBuilder(f.getAnnotation(CustomBuilder.class), context);
			ModConfigurationEntryBuilder builder = builderObtainer.apply(context);
			ConfigurationUIEntryContext entryContext = new ConfigurationUIEntryContext(this, fieldTable, f, configuration);
			values.putIfAbsent(entryContext, entryContext);
			entryContext = values.get(entryContext);
			entryContext.fieldTable = fieldTable;
			builder.buildValueSpan(entryContext);

		}
		
		
		//frame.addInner(fieldTable).left().expandX().fillX();
		this.scrollPane = new ScrollPane2(fieldTable, dependencies.skin, "darkDialogPanel");
		scrollPane.setScrollingDisabled(true, false);
		frame.addInner(scrollPane).expandX().fillX();
		
		Table bottomButtons = new Table();
		bottomButtons.defaults().expandX().uniformX();
		OptionButton.Factory buttonFactory = new OptionButton.Factory(dependencies, actionBus, "dialogActionButton");
		OptionButton<Object> cancel = buttonFactory.ui(Keymap.Actions.menu_customCloseDialog, "optionsDialog.cancel", this::cancel);
		OptionButton<Object> confirm = buttonFactory.ui(Keymap.Actions.menu_customConfirmDialog, "optionsDialog.confirm", () -> {
			try {
				saveAndClose();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		
		bottomButtons.add(cancel);
		bottomButtons.add(confirm);
		
		frame.addInner(bottomButtons).expandX().fillX();
		//frame.debugAll();
		group.add(frame).setVerticalCenter(0f).setHorizontalCenter(0f);
	}
	
	public DialogFrame getFrame() {
		return frame;
	}
	
	@Override
	public void close() {
		//NO-OP
	}
	
	public void cancel() {
		super.close();
	}
	
	private void save() throws IOException {
		Configuration.saveConfig(coremod, configuration, Cast.from(values));
	}
	
	public void saveAndClose() throws IOException {
		save();
		super.close();
	}
	
	public LegacyViewDependencies getDependencies() {
		return dependencies;
	}
	
}
