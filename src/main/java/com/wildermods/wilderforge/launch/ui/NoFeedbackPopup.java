package com.wildermods.wilderforge.launch.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.RuntimeSkin;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.wildermods.wilderforge.launch.InternalOnly;
import com.worldwalkergames.legacy.context.GameStrings;
import com.worldwalkergames.legacy.context.LegacyViewDependencies;
import com.worldwalkergames.legacy.game.common.ui.OptionButton;
import com.worldwalkergames.legacy.options.Keymap;
import com.worldwalkergames.legacy.ui.DialogFrame;
import com.worldwalkergames.legacy.ui.PopUp;
import com.worldwalkergames.legacy.ui.feedback.FeedbackPopup;
import com.worldwalkergames.ui.NiceLabel;

@InternalOnly
public class NoFeedbackPopup extends PopUp {

	private DialogFrame frame;
	
	@InternalOnly
	public NoFeedbackPopup(LegacyViewDependencies dependencies) {
		super(true, dependencies);
		FeedbackPopup.isActive = true;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void build() {
		GameStrings I18N = this.dependencies.gameStrings;
		LegacyViewDependencies.ScreenInfo screenInfo = this.dependencies.screenInfo;
		RuntimeSkin skin = dependencies.skin;
		
		frame = new DialogFrame(dependencies);
		
			String title = I18N.ui("wilderforge.ui.nofeedback.title");
				Label titleLabel = new Label((CharSequence)title, (Skin)skin, "dialogTitle");  
				frame.addInner((Actor)titleLabel).expandX().left().padBottom(10f);
				Table description = new Table((Skin)skin);
				NiceLabel descriptionLabel = new NiceLabel(I18N.ui("wilderforge.ui.nofeedback.description"), skin, "dialogBody");
				descriptionLabel.setWrap(true);
				description.add(descriptionLabel);

			Table button = new Table();
				button.defaults().expandX();
				OptionButton.Factory bf = new OptionButton.Factory(dependencies, actionBus, "detailButton");
				OptionButton okay = bf.ui(Keymap.Actions.menu_customCloseDialog, "wilderforge.ui.nofeedback.okay", this::userCancel);
				button.add(okay);
		
		frame.addInner(description);
		frame.addInner(button).padTop(screenInfo.scale(15f)).expandX().fillX();
		frame.pack();
		
		group.add(frame).setVerticalCenter(0f).setHorizontalCenter(0f);
	}
	
	@Override
	public void close() {
		FeedbackPopup.isActive = false;
		super.close();
	}

}
