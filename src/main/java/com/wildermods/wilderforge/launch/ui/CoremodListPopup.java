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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModMetadata;

import static com.wildermods.wilderforge.api.modJsonV1.ModJsonConstants.*;
import com.wildermods.wilderforge.api.modLoadingV1.CoremodInfo;
import com.wildermods.wilderforge.api.uiV1.TextureFilterDrawable;
import com.wildermods.wilderforge.api.uiV1.UIButton;
import com.wildermods.wilderforge.api.uiV1.elements.buttons.LinkButton;
import com.wildermods.wilderforge.launch.InternalOnly;
import com.wildermods.wilderforge.launch.coremods.Coremods;
import com.worldwalkergames.legacy.context.GameStrings;
import com.worldwalkergames.legacy.context.LegacyViewDependencies;
import com.worldwalkergames.legacy.controller.NavTable;
import com.worldwalkergames.legacy.ui.DialogFrame;
import com.worldwalkergames.legacy.ui.PopUp;
import com.worldwalkergames.legacy.ui.component.FancyLabel;
import com.worldwalkergames.legacy.ui.menu.OptionsDialog.Style;
import com.worldwalkergames.ui.NiceLabel;

@InternalOnly
public class CoremodListPopup extends PopUp {
	private static final Logger LOGGER = LogManager.getLogger(CoremodListPopup.class);
	private Filter filter = new Filter() {};
	private DialogFrame frame;
	private Style style;
	private final LegacyViewDependencies.ScreenInfo screen;
	
	protected final Table masterTable;
	
	protected final NavTable left;
	protected final NavTable right;
	
	private final ScrollPane2 leftScrollPane;
	private final ScrollPane2 rightScrollPane;
	
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
		
		this.right = new NavTable(dependencies.skin);
		this.right.defaults().expandX().left();
		this.right.right().pad(scale(10f));
		
		this.rightScrollPane = new ScrollPane2(right, dependencies.skin, "insetOpaquePanel");
		this.rightScrollPane.setScrollingDisabled(true, false);
		this.rightScrollPane.setFadeScrollBars(false);
		
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
		frame.preferredWidth = Float.valueOf(style.f("dialogWidth"));
		frame.preferredWidth = Float.valueOf(style.f("dialogHeight"));
		
		float padLeft = style.f("padLeft");
		float padRight = style.f("padRight");
		
		String title = I18N.ui("wilderforge.ui.coremods.title");
			Label titleLabel = new Label(title, skin, "dialogTitle");
			frame.addInner(titleLabel).expandX().left().padLeft(padLeft).padBottom(style.f("titlePadBottom"));
			
		Table modList = new Table();
		
		modList.defaults().left();
		modList.align(Align.left);
		
		for(CoremodInfo coremod : Coremods.getAllCoremods()) {
			try {
				UIButton<CoremodInfo> modButton = new ModButton(this, coremod);
				modList.add(modButton).left();
				modList.row();
			}
			catch(IOException e) {
				throw new AssertionError(e);
			}
		}
		
		leftScrollPane.setActor(modList);
		
		clickModButton(null); //I don't know why, but this has to be called twice or the right panel will not be the correct size.
		clickModButton(null); //I'm done trying to debug this
		
		group.add(frame).setVerticalCenter(0).setHorizontalCenter(0);
	}
	
	private void clickModButton(ModButton modButton) {
		if(selectedMod != null) {
			selectedMod.setChecked(false);
		}
		selectedMod = modButton;
		
		Table table = new Table();
		table.defaults().align(Align.topLeft).pad(3f).expandX();

		if(modButton == null) {

			masterTable.clear();
			masterTable.add(leftScrollPane).width(Value.percentWidth(0.5f, frame));
			masterTable.add(rightScrollPane).width(Value.percentWidth(0.5f, frame)).height(leftScrollPane.getHeight());
			
			masterTable.getCell(rightScrollPane).align(Align.topLeft).grow();
			
			rightScrollPane.setActor(table);
			
		}
		else {
			
			Table topLabels = new Table();
			topLabels.defaults().align(Align.topLeft).pad(3f).expandX();
			
			Table imageAndData = new Table();
			imageAndData.defaults().align(Align.topLeft).expandX().padRight(3f);
			
			Table modNameTable = new Table();
			modNameTable.defaults().align(Align.topLeft).padRight(9f).expandX();
			
			Table modDataTable = new Table();
			modDataTable.defaults().align(Align.topLeft);
			
			Table linkButtons = new Table();
			linkButtons.defaults().align(Align.center).width(Value.percentWidth(0.5f, table)).expandX();
			
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
			CustomValue licenseCV = metadata.getCustomValue(LICENSE_URL);
			
			String license = null;
			
			if(licenseCV != null) {
				license = licenseCV.getAsString();
			}
			
			linkButtons.add(new LinkButton(I18N.ui("wilderforge.ui.coremods.button.source"), this, "gearLine", sources));
			linkButtons.add(new LinkButton(I18N.ui("wilderforge.ui.coremods.button.issues"), this, "gearLine", issues));
			linkButtons.row();
			linkButtons.add(new LinkButton(I18N.ui("wilderforge.ui.coremods.button.website"), this, "gearLine", website));
			linkButtons.add(new LinkButton(I18N.ui("wilderforge.ui.coremods.button.license"), this, "gearLine", license));
			
			table.add(linkButtons.align(Align.center)).growX();
			
			String summary = metadata.getDescription();
			
			FancyLabel summaryLabel = new FancyLabel(summary, this.dependencies.skin, "characterSheet", "details");
			
			table.row();
			
			table.add(summaryLabel).expand();

		}
		
		rightScrollPane.setActor(table);

		masterTable.clear();
		masterTable.add(leftScrollPane).width(Value.percentWidth(0.5f, frame));
		masterTable.add(rightScrollPane).width(Value.percentWidth(0.5f, frame)).height(leftScrollPane.getHeight());
		
		masterTable.getCell(rightScrollPane).align(Align.topLeft).grow();
		
		frame.addInner(masterTable);
		
		frame.pack();
	}
	
	private float scale(float f) {
		return dependencies.screenInfo.scale(f);
	}
	
	private static class ModButton extends UIButton<CoremodInfo> {
		
		private CoremodListPopup screen;
		private CoremodInfo coremod;
		private Image modImage;
		
		private ModButton(CoremodListPopup screen, CoremodInfo coremod) throws IOException {
			super("", screen.dependencies.skin, "gearLine");
			this.setUserData(coremod);
			this.screen = screen;
			this.coremod = coremod;
			this.modImage = constructImage();
			
			Table buttonTable = new Table(screen.dependencies.skin);
			buttonTable.add(modImage).height(screen.scale(50f)).padLeft(-screen.scale(4f)).padRight(screen.scale(6f)).width(screen.scale(50f));
			
			NiceLabel nameLabel = new NiceLabel(coremod.name, screen.dependencies.skin, "darkInteractive");
			nameLabel.setEllipsis(true);
			buttonTable.add(nameLabel).left().minWidth(screen.scale(325f));
			buttonTable.row();
			
			
			this.add(buttonTable).left();	
		}
		
		private Image constructImage() throws IOException {
			CoremodInfo coremodInfo = coremod;
			Image modImage = new Image(new TextureFilterDrawable("assets/wilderforge/ui/coremodlist/exampleModImage.png", null, TextureFilter.Nearest));
			FileHandle imageFile = null;
			if(coremodInfo.getFolder() != null) {
				if(coremodInfo.modId.equals("wildermyth")) {
					imageFile = Gdx.files.internal("assets/ui/icon/wildermythIcon_256.png");
				}
				else {
					imageFile = coremodInfo.getFolder().child("assets/" + coremod.modId + "/icon.png");
				}
			}

			CustomValue imgLoc = coremod.getMetadata().getCustomValue(IMAGE);
			
			if(imgLoc  != null) {
				imageFile = coremodInfo.getFolder().child(imgLoc.getAsString());
			}
			if(imageFile != null && imageFile.exists()) {
				modImage = new Image(new TextureFilterDrawable(imageFile.path(), null, TextureFilter.Nearest));
			}
			else {
				LOGGER.info("Could not find " + imageFile.path() + " " + imageFile.type());
				LOGGER.info(coremod.getClass().getSimpleName());
			}

			modImage.setScaling(Scaling.fill);
			return modImage;
		}
		
		@Override
		public void clickImpl() {
			screen.clickModButton(this);
		}
		
	}
	
	private static interface Filter {
		
		public default boolean showCoremod(CoremodInfo coremod) {
			return true;
		}
		
	}
	
}
