package com.wildermods.wilderforge.launch.ui;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.RuntimeSkin;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane2;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.wildermods.wilderforge.api.modJsonV1.ModJsonConstants.*;
import com.wildermods.wilderforge.api.modLoadingV1.CoremodInfo;
import com.wildermods.wilderforge.api.uiV1.UIButton;
import com.wildermods.wilderforge.api.uiV1.elements.buttons.LinkButton;
import com.wildermods.wilderforge.launch.InternalOnly;
import com.wildermods.wilderforge.launch.LoadStatus;
import com.wildermods.wilderforge.launch.Main;
import com.wildermods.wilderforge.launch.coremods.Coremod;
import com.wildermods.wilderforge.launch.coremods.Coremods;
import com.worldwalkergames.legacy.context.GameStrings;
import com.worldwalkergames.legacy.context.LegacyViewDependencies;
import com.worldwalkergames.legacy.controller.NavTable;
import com.worldwalkergames.legacy.ui.DialogFrame;
import com.worldwalkergames.legacy.ui.PopUp;
import com.worldwalkergames.legacy.ui.component.FancyLabel;
import com.worldwalkergames.legacy.ui.menu.OptionsDialog.Style;
import com.worldwalkergames.ui.FancyImageDrawable;
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
			masterTable.setDebug(true);
		}
		
		
		masterTable.setWidth(dependencies.screenInfo.width);
		
		RuntimeSkin skin = dependencies.skin;
		style = skin.get(Style.class);
		
		frame = new DialogFrame(dependencies);
		frame.preferredWidth = Float.valueOf(style.f("dialogWidth"));
		frame.preferredWidth = Float.valueOf(style.f("dialogHeight"));
		frame.setDebug(true);
		
		float padLeft = style.f("padLeft");
		float padRight = style.f("padRight");
		
		String title = I18N.ui("wilderforge.ui.coremods.title");
			Label titleLabel = new Label(title, skin, "dialogTitle");
			frame.addInner(titleLabel).expandX().left().padLeft(padLeft).padBottom(style.f("titlePadBottom"));
			
		Table modList = new Table();
		
		modList.defaults().left();
		modList.align(Align.left);
		
		for(Coremod coremod : Coremods.getCoremodsByStatus(LoadStatus.values())) {
			try {
				UIButton<Coremod> modButton = new ModButton(this, coremod);
				modList.add(modButton).left();
				modList.row();
			}
			catch(IOException e) {
				throw new AssertionError(e);
			}
		}
		
		leftScrollPane.setActor(modList);
		masterTable.add(leftScrollPane).width(Value.percentWidth(0.5f, frame));
		masterTable.add(rightScrollPane).width(Value.percentWidth(0.5f, frame));
		frame.addInner(masterTable);
		
		clickModButton(null);
			
		frame.pack();
		
		group.add(frame).setVerticalCenter(0).setHorizontalCenter(0);
	}
	
	private void clickModButton(ModButton modButton) {
		if(selectedMod != null) {
			selectedMod.setChecked(false);
		}
		selectedMod = modButton;

		rightScrollPane.debugAll();
		
		Table table = new Table();
		table.setDebug(true);
		table.defaults().align(Align.topLeft).pad(3f).expandX();
		if(modButton == null) {
			
			rightScrollPane.setWidth(Value.percentWidth(0.5f, masterTable).get());
			rightScrollPane.setHeight(Value.percentHeight(1f, masterTable).get());
			
			rightScrollPane.setActor(table);
			rightScrollPane.setDebug(true);
			
			FancyLabel summary = new FancyLabel("", this.dependencies.skin, "characterSheet", "details");
			
			table.add(summary).grow();
			
			table.add().grow();
			
			rightScrollPane.layout();

			frame.pack();
		}
		else {
			
			Table topLabels = new Table();
			topLabels.defaults().align(Align.topLeft).pad(3f).expandX();
			topLabels.debugAll();
			
			Table imageAndData = new Table();
			imageAndData.defaults().align(Align.topLeft).expandX().padRight(3f);
			imageAndData.debugAll();
			
			Table modNameTable = new Table();
			modNameTable.defaults().align(Align.topLeft).padRight(9f).expandX();
			modNameTable.debugAll();
			
			Table modDataTable = new Table();
			modDataTable.defaults().align(Align.topLeft);
			
			Table linkButtons = new Table();
			linkButtons.defaults().align(Align.center).width(Value.percentWidth(0.5f, table)).expandX();
			linkButtons.debugAll();
			
			Cell<Image> imageCell = null;
			try {
				imageCell = imageAndData.add(modButton.constructImage()).align(Align.topLeft);
			}
			catch(IOException e) {
				LOGGER.catching(e);
			}
			
			NiceLabel modName = new NiceLabel(modButton.coremod.getName(), dependencies.skin, "characterSheetRightPanelBold");
			NiceLabel version = new NiceLabel(I18N.ui("wilderforge.ui.coremods.version", modButton.coremod.getVersion().toString()), dependencies.skin, "characterSheetRightPanel");
			NiceLabel authors = new NiceLabel(I18N.ui("wilderforge.ui.coremods.author", modButton.coremod.getCoremodInfo().author), dependencies.skin, "characterSheetRightPanel");
			NiceLabel modid = new NiceLabel(I18N.ui("wilderforge.ui.coremods.modid", modButton.coremod.value()), dependencies.skin, "characterSheetRightPanel");
			
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
			
			linkButtons.setDebug(true);
			
			JsonObject json;
			try {
				json = modButton.coremod.getModJson();
			} catch (IOException e) {
				Main.LOGGER.catching(e);
				json = null;
			}
			
			if(json != null) {
				
				JsonElement sourceJson = json.get(SOURCE_URL);
				JsonElement issuesJson = json.get(ISSUES_URL);
				JsonElement websiteJson = json.get(WEBSITE);
				JsonElement licenseJson = json.get(LICENSE_URL);
				
				String source = null;
				String issues = null;
				String website = null;
				String license = null;
				
				if(sourceJson != null) {
					source = sourceJson.getAsString();
				}
				if(issuesJson != null) {
					issues = issuesJson.getAsString();
				}
				if(websiteJson != null) {
					website = websiteJson.getAsString();
				}
				if(licenseJson != null) {
					license = licenseJson.getAsString();
				}
				
				linkButtons.add(new LinkButton(I18N.ui("wilderforge.ui.coremods.button.source"), this, "gearLine", source));
				linkButtons.add(new LinkButton(I18N.ui("wilderforge.ui.coremods.button.issues"), this, "gearLine", issues));
				linkButtons.row();
				linkButtons.add(new LinkButton(I18N.ui("wilderforge.ui.coremods.button.website"), this, "gearLine", website));
				linkButtons.add(new LinkButton(I18N.ui("wilderforge.ui.coremods.button.license"), this, "gearLine", license));
			}
			
			table.add(linkButtons.align(Align.center)).growX();
			
			String summary = "";
			
			if(json != null) {
				JsonElement summaryJson = json.get(DESCRIPTION);
				if(summaryJson != null) {
					summary = summaryJson.getAsString();
				}
			}
			
			FancyLabel summaryLabel = new FancyLabel(summary, this.dependencies.skin, "characterSheet", "details");
			
			table.row();
			
			table.add(summaryLabel).grow();

		}
		
		rightScrollPane.setActor(table);
		rightScrollPane.setWidth(Value.percentWidth(0.5f, frame).get());
		rightScrollPane.setHeight(Value.percentHeight(0.5f, frame).get());
		masterTable.getCell(rightScrollPane).align(Align.topLeft).grow();
		
		frame.pack();
	}
	
	private float scale(float f) {
		return dependencies.screenInfo.scale(f);
	}
	
	private static class ModButton extends UIButton<Coremod> {
		
		private CoremodListPopup screen;
		private Coremod coremod;
		private Image modImage;
		
		private ModButton(CoremodListPopup screen, Coremod coremod) throws IOException {
			super("", screen.dependencies.skin, "gearLine");
			this.setUserData(coremod);
			this.screen = screen;
			this.coremod = coremod;
			this.modImage = constructImage();
			
			Table buttonTable = new Table(screen.dependencies.skin);
			buttonTable.add(modImage).height(screen.scale(50f)).padLeft(-screen.scale(4f)).padRight(screen.scale(6f)).width(screen.scale(50f));
			
			NiceLabel nameLabel = new NiceLabel(coremod.getName(), screen.dependencies.skin, "darkInteractive");
			nameLabel.setEllipsis(true);
			buttonTable.add(nameLabel).left().minWidth(screen.scale(325f));
			buttonTable.row();
			
			
			this.add(buttonTable).left();	
		}
		
		private Image constructImage() throws IOException {
			CoremodInfo coremodInfo = coremod.getCoremodInfo();
			Image modImage = new Image(new FancyImageDrawable("wilderforge/assets/ui/coremodlist/exampleModImage.png", null));
			FileHandle imageFile = null;
			if(coremodInfo.getFolder(false) != null) {
				imageFile = coremodInfo.getFolder(false).child(coremod.value() + "/assets/modIcon.png");
			}

			JsonElement imgLoc = coremod.getModJson().get(IMAGE);
			
			if((imgLoc = coremod.getModJson().get(IMAGE)) != null) {
				imageFile = coremodInfo.getFolder(false).child(imgLoc.getAsString());
			}
			if(imageFile != null && imageFile.exists()) {
				modImage = new Image(new FancyImageDrawable(imageFile.path(), null));
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
		
		public default boolean showCoremod(Coremod coremod) {
			return Coremods.getStatus(coremod) == LoadStatus.LOADED;
		}
		
	}
	
}
