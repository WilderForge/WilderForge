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

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.wildermods.wilderforge.api.modJsonV1.ModJsonConstants.*;
import com.wildermods.wilderforge.api.modLoadingV1.CoremodInfo;
import com.wildermods.wilderforge.api.uiV1.UIButton;
import com.wildermods.wilderforge.launch.InternalOnly;
import com.wildermods.wilderforge.launch.LoadStatus;
import com.wildermods.wilderforge.launch.coremods.Coremod;
import com.wildermods.wilderforge.launch.coremods.Coremods;
import com.worldwalkergames.legacy.context.GameStrings;
import com.worldwalkergames.legacy.context.LegacyViewDependencies;
import com.worldwalkergames.legacy.controller.NavTable;
import com.worldwalkergames.legacy.ui.DialogFrame;
import com.worldwalkergames.legacy.ui.PopUp;
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
		rightScrollPane.clear();
		rightScrollPane.debugAll();
		Table table = new Table();
		table.setDebug(true);
		if(modButton == null) {
			
			rightScrollPane.setWidth(Value.percentWidth(0.5f, masterTable).get());
			rightScrollPane.setHeight(Value.percentHeight(1f, masterTable).get());
			masterTable.getCell(rightScrollPane).fill();
			
			rightScrollPane.setActor(table);

			frame.pack();
			return;
		}
		else {
			Cell<Image> imageCell = null;
			try {
				imageCell = table.add(modButton.constructImage()).align(Align.topLeft);
			}
			catch(IOException e) {
				LOGGER.catching(e);
			}
			
			Table topLabels = new Table();
			topLabels.defaults().align(Align.topLeft).pad(3f);
			
			Table modNameTable = new Table();
			modNameTable.defaults().align(Align.topLeft).padRight(9f);
			
			NiceLabel modName = new NiceLabel(modButton.coremod.getName(), dependencies.skin, "characterSheetRightPanelBold");
			NiceLabel version = new NiceLabel(I18N.ui("wilderforge.ui.coremods.version", modButton.coremod.getVersion().toString()), dependencies.skin, "characterSheetRightPanel");
			NiceLabel authors = new NiceLabel(I18N.ui("wilderforge.ui.coremods.author", modButton.coremod.getCoremodInfo().author), dependencies.skin, "characterSheetRightPanel");
			NiceLabel modid = new NiceLabel(I18N.ui("wilderforge.ui.coremods.modid", modButton.coremod.value()), dependencies.skin, "characterSheetRightPanel");
			
			modName.setEllipsis(true);
			authors.setEllipsis(true);
			
			
			table.add(topLabels).align(Align.topLeft);
			
			modNameTable.add(modName);
			modNameTable.add(version);
			
			topLabels.add(modNameTable);
			topLabels.row();
			topLabels.add(authors);
			topLabels.row();
			topLabels.add(modid);
			
			if(imageCell != null) {
				imageCell.width(Value.percentHeight(1f, topLabels));
				imageCell.height(Value.percentHeight(1f, topLabels));
				topLabels.pack();
			}
			
			rightScrollPane.setActor(table);
			rightScrollPane.setWidth(Value.percentWidth(0.5f, frame).get());
			rightScrollPane.setHeight(Value.percentHeight(0.5f, frame).get());
			masterTable.getCell(rightScrollPane).grow();
			LOGGER.info(table.getCells().size);
			
			table.add().grow();
			
			frame.pack();
		}
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
