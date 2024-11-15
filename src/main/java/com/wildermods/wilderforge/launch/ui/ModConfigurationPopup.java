package com.wildermods.wilderforge.launch.ui;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Function;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.RuntimeSkin;
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
import com.worldwalkergames.legacy.ui.DialogFrame;
import com.worldwalkergames.legacy.ui.PopUp;
import com.worldwalkergames.legacy.ui.menu.OptionsDialog.Style;

public class ModConfigurationPopup extends PopUp {
	public final CoremodInfo coremod;
	private Object configuration;
	private DialogFrame frame;
	private Style style;
	public final GameStrings I18N = dependencies.gameStrings;
	private final Function<ConfigurationUIContext, ? extends ModConfigurationEntryBuilder> builder;
	private HashSet<Field> configFields = new LinkedHashSet<>();
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
		
		//frame.preferredWidth = Float.valueOf(style.f("dialogWidth"));
		
		float padLeft = style.f("padLeft");
		float padRight = style.f("padRight");
		
		String title = I18N.ui("wilderforge.ui.coremods.configure.title", coremod.name);
		Label titleLabel = new Label(title, skin, "dialogTitle");
		titleLabel.setAlignment(Align.topLeft);
		frame.addInner(titleLabel).expandX().left().padLeft(padLeft).padBottom(style.f("titlePadBottom")).padRight(-padRight);
		
		Table fieldTable = new Table().padLeft(padLeft).padRight(padRight);
		
		for(Field f : configFields) {
			Table valueSpanTable = new Table();
			Function<ConfigurationUIContext, ? extends ModConfigurationEntryBuilder> builderObtainer = getBuilder(f.getAnnotation(CustomBuilder.class), context);
			ModConfigurationEntryBuilder builder = builderObtainer.apply(context);
			ConfigurationUIEntryContext entryContext = new ConfigurationUIEntryContext(this, fieldTable, valueSpanTable, f, configuration);
			builder.buildValueSpan(entryContext);
		}
		
		frame.debug();
		frame.addInner(fieldTable).left().expandX().fillX();
		group.add(frame).setVerticalCenter(0f).setHorizontalCenter(0f);
	}
	
	public LegacyViewDependencies getDependencies() {
		return dependencies;
	}
	
}
