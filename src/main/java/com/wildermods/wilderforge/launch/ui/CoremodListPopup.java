package com.wildermods.wilderforge.launch.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.RuntimeSkin;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane2;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;

import java.io.IOException;
import java.util.function.Function;

import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModMetadata;

import static com.wildermods.wilderforge.api.modLoadingV1.ModConstants.*;
import com.wildermods.wilderforge.api.modLoadingV1.CoremodInfo;
import com.wildermods.wilderforge.api.uiV1.TextureFilterDrawable;
import com.wildermods.wilderforge.api.uiV1.UIButton;
import com.wildermods.wilderforge.api.uiV1.elements.buttons.LinkButton;
import com.wildermods.wilderforge.launch.InternalOnly;
import com.wildermods.wilderforge.launch.coremods.Configuration;
import com.wildermods.wilderforge.launch.coremods.Coremods;
import com.wildermods.wilderforge.launch.logging.Logger;
import com.worldwalkergames.legacy.context.GameStrings;
import com.worldwalkergames.legacy.context.LegacyViewDependencies;
import com.worldwalkergames.legacy.controller.NavTable;
import com.worldwalkergames.legacy.ui.DialogFrame;
import com.worldwalkergames.legacy.ui.PopUp;
import com.worldwalkergames.legacy.ui.component.FancyLabel;
import com.worldwalkergames.legacy.ui.menu.OptionsDialog.Style;
import com.worldwalkergames.ui.FancyPanelDrawable;
import com.worldwalkergames.ui.NiceLabel;

@InternalOnly
public class CoremodListPopup extends PopUp {
	private static final Logger LOGGER = new Logger(CoremodListPopup.class);
	private Filter filter = new Filter() {};
	private DialogFrame frame;
	private Style style;
	private final LegacyViewDependencies.ScreenInfo screen;
	
	protected final Table masterTable;
	
	protected final NavTable left;
	
	private final ScrollPane2 leftScrollPane;
	private final Table rightPane;
	
	private ModButton selectedMod;
	
	private final GameStrings I18N = dependencies.gameStrings;
	
	@InternalOnly
	public CoremodListPopup(LegacyViewDependencies dependencies) {
		super(true, dependencies);
		this.screen = dependencies.screenInfo;
		
		this.masterTable = new Table();
		
		this.left = new NavTable(dependencies.skin);
		this.left.defaults().expandX().left();
		this.left.align(Align.left);
		
		this.leftScrollPane = new ScrollPane2(left, dependencies.skin, "lightDialogPanel");
		this.leftScrollPane.setScrollingDisabled(true, false);
		this.leftScrollPane.setFadeScrollBars(false);
		
		this.rightPane = new Table();
		rightPane.setBackground(new FancyPanelDrawable(FancyPanelDrawable.Style.inset));
	}

	@Override
	public void build() {
		if(frame != null) {
			group.removeActor(frame);
			masterTable.clear();
		}
		
		RuntimeSkin skin = dependencies.skin;
		style = skin.get(Style.class);

		frame = new DialogFrame(dependencies);
		frame.preferredWidth = style.f("dialogWidth");

		float padLeft = style.f("padLeft");

		String title = I18N.ui("wilderforge.ui.coremods.title");
		Label titleLabel = new Label(title, skin, "dialogTitle");
		frame.addInner(titleLabel).expandX().left().padLeft(padLeft).padBottom(style.f("titlePadBottom"));

		Table modList = new Table();
		modList.align(Align.left);

		for(CoremodInfo coremod : Coremods.getAllCoremods()) {
			try {
				UIButton<CoremodInfo> modButton = new ModButton(this, coremod);
				modList.add(modButton).expandX().fillX().align(Align.left).row();
			} catch(IOException e) {
				throw new AssertionError(e);
			}
		}

		leftScrollPane.setActor(modList);
		clickModButton(null);

		group.add(frame).setVerticalCenter(0).setHorizontalCenter(0);
	}
	
	private void clickModButton(ModButton modButton) {
		if(selectedMod != null) {
			selectedMod.setChecked(false);
		}
		selectedMod = modButton;
		rightPane.clear();
		
		Table table = new Table();
		table.defaults().align(Align.topLeft).pad(3f).expandX();

		if(modButton != null) {
			LOGGER.log("Author of " + modButton.coremod.name + ": " + modButton.coremod.author);
			
			Table topLabels = new Table();
			topLabels.defaults().align(Align.topLeft).pad(3f).expandX();
			
			Table imageAndData = new Table();
			imageAndData.defaults().align(Align.topLeft).expandX().padRight(3f);
			
			Table modNameTable = new Table();
			modNameTable.defaults().align(Align.topLeft).padRight(9f).expandX();
			
			Table modDataTable = new Table();
			modDataTable.defaults().align(Align.topLeft);
			
			Table linkButtons = new Table();
			linkButtons.defaults().align(Align.topLeft).width(Value.percentWidth(0.495f, rightPane)).expandX();
			
			Cell<Image> imageCell = null;
			try {
				imageCell = imageAndData.add(modButton.constructImage()).align(Align.topLeft);
			}
			catch(IOException e) {
				LOGGER.catching(e);
			}
			
			ModMetadata metadata = modButton.coremod.getMetadata();
			
			NiceLabel modName = new NiceLabel(modButton.coremod.name, dependencies.skin, "characterSheetRightPanelBold");
			NiceLabel version = new NiceLabel(I18N.ui("wilderforge.ui.coremods.version", metadata.getVersion().getFriendlyString()), dependencies.skin, "characterSheetRightPanel");
			NiceLabel authors = new NiceLabel(I18N.ui("wilderforge.ui.coremods.author", modButton.coremod.author), dependencies.skin, "characterSheetRightPanel");
			NiceLabel modid = new NiceLabel(I18N.ui("wilderforge.ui.coremods.modid", modButton.coremod.modId), dependencies.skin, "characterSheetRightPanel");
			
			modName.setEllipsis(true);
			authors.setEllipsis(true);
			
			
			table.add(imageAndData).align(Align.left);
			
			modNameTable.add(modName);
			modNameTable.add(version);
			
			modDataTable.add(modNameTable);
			modDataTable.row();
			modDataTable.add(authors);
			modDataTable.row();
			modDataTable.add(modid);
			
			imageAndData.add(modDataTable);
			imageAndData.row();
			
			if(imageCell != null) {
				imageCell.width(Value.percentHeight(1f, imageAndData));
				imageCell.height(Value.percentHeight(1f, imageAndData));
				imageAndData.pack();
			}
			
			table.row();
				
			String sources = metadata.getContact().get(SOURCE_URL).orElse(null);
			String issues = metadata.getContact().get(ISSUES_URL).orElse(null);
			String website = metadata.getContact().get(HOMEPAGE).orElse(null);
			String license = metadata.getContact().get(LICENSE).orElse(null);
			
			linkButtons.add(new LinkButton(I18N.ui("wilderforge.ui.coremods.button.source"), this, "gearLine", sources));
			linkButtons.add(new LinkButton(I18N.ui("wilderforge.ui.coremods.button.issues"), this, "gearLine", issues));
			linkButtons.row();
			linkButtons.add(new LinkButton(I18N.ui("wilderforge.ui.coremods.button.website"), this, "gearLine", website));
			linkButtons.add(new LinkButton(I18N.ui("wilderforge.ui.coremods.button.license"), this, "gearLine", license));
			linkButtons.row();
			
			table.add(linkButtons.align(Align.center));
			
			table.row();
			
			table.add(new ModConfigButton(this, modButton.coremod)).align(Align.center).expandX().width(Value.percentWidth(.99f, rightPane)).pad(-3f);
			
			String summary = metadata.getDescription();
			
			table.row();
			
			FancyLabel summaryLabel = new FancyLabel(summary, this.dependencies.skin, "characterSheet", "details");
			summaryLabel.wrapEnabled = true;
			ScrollPane2 textScroller = new ScrollPane2(summaryLabel, dependencies.skin, "clear");
			textScroller.setScrollingDisabled(true, false);
			
			table.add(textScroller).grow();

			rightPane.add(table).expand().grow();
		}

		masterTable.clear();
		masterTable.add(leftScrollPane).width(Value.percentWidth(0.5f, frame)).grow();
		masterTable.add(rightPane).width(Value.percentWidth(0.5f, frame)).grow();
		
		frame.addInner(masterTable);
		
		frame.pack();
	}
	
	private float scale(float f) {
		return dependencies.screenInfo.scale(f);
	}
	
	private static class ModButton extends UIButton<CoremodInfo> {
		private CoremodInfo coremod;
		private CoremodListPopup screen;
		private Image modImage;
		
		private ModButton(CoremodListPopup screen, CoremodInfo coremod) throws IOException {
			super(coremod.name, screen.dependencies.skin, "gearLine");
			this.setUserData(coremod);
			this.coremod = coremod;
			this.screen = screen;
			this.modImage = constructImage();
			
			NiceLabel nameLabel = new NiceLabel(coremod.name, screen.dependencies.skin, "darkInteractive");
			nameLabel.setEllipsis(true);
			nameLabel.setAlignment(Align.left);
			this.label = nameLabel;

			this.add(modImage).height(screen.scale(48f)).padLeft(-screen.scale(4f)).padRight(screen.scale(6f)).width(screen.scale(48f)).align(Align.left);
			this.add(nameLabel).expandX().fillX();
			
			this.align(Align.left);
		}
		
		@Override
		public void build(String text) {
			//NO-OP
		}
		
		private Image constructImage() throws IOException {
			CoremodInfo coremodInfo = getUserData();
			Image modImage = new Image(new TextureFilterDrawable("assets/wilderforge/ui/coremodlist/exampleModImage.png", null, TextureFilter.Nearest)); 
			FileHandle imageFile = null;
			if(coremodInfo.getFolder() != null) {
				if(coremodInfo.modId.equals("wildermyth")) {
					imageFile = Gdx.files.internal("assets/ui/icon/wildermythIcon_256.png"); //OFFICE ACTION #1: Assets from the base game must be pulled from the user's filesystem.
				}
				else {
					imageFile = coremodInfo.getFolder().child("assets/" + coremodInfo.modId + "/icon.png");
				}
			}

			CustomValue imgLoc = coremodInfo.getMetadata().getCustomValue(IMAGE);
			
			if(imgLoc  != null) {
				imageFile = coremodInfo.getFolder().child(imgLoc.getAsString());
			}
			if(imageFile != null && imageFile.exists()) {
				modImage = new Image(new TextureFilterDrawable(imageFile.path(), null, TextureFilter.Nearest));
			}
			else {
				LOGGER.info("Could not find " + imageFile.path() + " " + imageFile.type());
				LOGGER.info(coremodInfo.getClass().getSimpleName());
			}

			modImage.setScaling(Scaling.fill);
			return modImage;
		}
		
		@Override
		public void clickImpl() {
			screen.clickModButton(this);
		}
		
	}
	
	private static class ModConfigButton extends UIButton<CoremodInfo> {
		private static final Runnable NO_ACTION = () -> {};
		private CoremodListPopup screen;
		private CoremodInfo coremod;
		private Runnable onClick = NO_ACTION;
		
		private ModConfigButton(CoremodListPopup screen, CoremodInfo coremod) {
			super(screen.dependencies.gameStrings.ui("wilderforge.ui.coremods.button.configure"), screen.dependencies.skin, "gearLine");
			setUserData(coremod);
			Function<LegacyViewDependencies, ? extends PopUp> customConfig;
			customConfig = Configuration.getCustomConfigPopUp(coremod);
			if(customConfig != null) {
				onClick = () -> {
					PopUp popup = customConfig.apply(screen.dependencies);
					screen.dependencies.popUpManager.pushFront(popup, true);
				};
				this.setDisabled(false);
				return;
			}
			if(Configuration.getConfig(coremod) != null) {
				onClick = () -> {
					PopUp popup = new ModConfigurationPopup(screen.dependencies, coremod);
					screen.dependencies.popUpManager.pushFront(popup, true);
				};
			}
			if(onClick == NO_ACTION) {
				this.setDisabled(true);
			}
		}
		
		@Override
		public void clickImpl() {
			onClick.run();
			this.setChecked(false);
		}
	}
	
	private static interface Filter {
		
		public default boolean showCoremod(CoremodInfo coremod) {
			return true;
		}
		
	}
	
}
