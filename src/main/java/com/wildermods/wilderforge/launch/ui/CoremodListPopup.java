package com.wildermods.wilderforge.launch.ui;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.RuntimeSkin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;

import com.google.gson.JsonElement;

import java.io.IOException;

import static com.wildermods.wilderforge.api.modJsonV1.ModJsonConstants.*;
import com.wildermods.wilderforge.api.modLoadingV1.CoremodInfo;
import com.wildermods.wilderforge.api.uiV1.UIButton;
import com.wildermods.wilderforge.launch.InternalOnly;
import com.wildermods.wilderforge.launch.LoadStatus;
import com.wildermods.wilderforge.launch.coremods.Coremod;
import com.wildermods.wilderforge.launch.coremods.Coremods;
import com.worldwalkergames.legacy.context.GameStrings;
import com.worldwalkergames.legacy.context.LegacyViewDependencies;
import com.worldwalkergames.legacy.ui.DialogFrame;
import com.worldwalkergames.legacy.ui.PopUp;
import com.worldwalkergames.legacy.ui.menu.OptionsDialog.Style;
import com.worldwalkergames.ui.FancyImageDrawable;
import com.worldwalkergames.ui.NiceLabel;

@InternalOnly
public class CoremodListPopup extends PopUp {
	
	private Filter filter = new Filter() {};
	private DialogFrame frame;
	private Style style;
	private final LegacyViewDependencies.ScreenInfo screen;
	
	@InternalOnly
	public CoremodListPopup(LegacyViewDependencies dependencies) {
		super(true, dependencies);
		this.screen = dependencies.screenInfo;
	}

	@Override
	public void build() {
		if(frame != null) {
			group.removeActor(frame);
		}
		
		GameStrings I18N = this.dependencies.gameStrings;
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
		Table description = new Table();
		
		modList.defaults().left();
		modList.align(Align.left);
		
		for(Coremod coremod : Coremods.getCoremodsByStatus(LoadStatus.values())) {
			try {
				UIButton<Coremod> modButton = modButton(coremod);
				modList.add(modButton).left();
				modList.row();
			}
			catch(IOException e) {
				throw new AssertionError(e);
			}
		}
		
		frame.add(modList);
		frame.add(description);
			
		frame.pack();
		
		group.add(frame).setVerticalCenter(0).setHorizontalCenter(0);
	}

	UIButton<Coremod> modButton(Coremod coremod) throws IOException {
		CoremodInfo coremodInfo = coremod.getCoremodInfo();
		UIButton<Coremod> modButton = new UIButton<Coremod>("", dependencies.skin, "gearLine");
		modButton.setUserData(coremod);
		Table buttonTable = new Table(dependencies.skin);
		Image modImage = new Image(new FancyImageDrawable("wilderforge/assets/ui/coremodlist/exampleModImage.png", null));
		FileHandle imageFile = null;
		if(coremodInfo.folder != null) {
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
			System.out.println("Could not find " + imageFile.path() + " " + imageFile.type());
			System.out.println(coremod.getClass().getSimpleName());
		}

		modImage.setScaling(Scaling.fill);
		buttonTable.add(modImage).height(dependencies.screenInfo.scale(50f)).padLeft(-dependencies.screenInfo.scale(4f)).padRight(dependencies.screenInfo.scale(6f)).width(dependencies.screenInfo.scale(50f));
		
		NiceLabel nameLabel = new NiceLabel(coremod.getName(), dependencies.skin, "darkInteractive");
		nameLabel.setEllipsis(true);
		buttonTable.add(nameLabel).left().width(dependencies.screenInfo.scale(200f));
		buttonTable.row();
		
		
		modButton.add(buttonTable).left();	
		return modButton;
	}
	
	private static interface Filter {
		
		public default boolean showCoremod(Coremod coremod) {
			return Coremods.getStatus(coremod) == LoadStatus.LOADED;
		}
		
	}
	
}
