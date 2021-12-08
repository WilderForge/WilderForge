package com.wildermods.wilderforge.api.uiV1.elements.buttons;

import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.wildermods.wilderforge.api.uiV1.UIButton;
import com.wildermods.wilderforge.mixins.PopUpAccessorMixin;
import com.worldwalkergames.legacy.context.LegacyViewDependencies;
import com.worldwalkergames.legacy.ui.PopUp;
import com.worldwalkergames.util.OSUtil;

public class LinkButton extends UIButton<URL> {

	public static final Logger LOGGER = LogManager.getLogger(LinkButton.class);
	
	private final String url;
	
	public LinkButton(String text, PopUp screen, String style, String url) {
		this(text, ((PopUpAccessorMixin)screen).getDependencies(), style, url);
	}
	
	public LinkButton(String text, LegacyViewDependencies dependencies, String style, String url) {
		this(text, dependencies.skin, style, url);
	}
	
	public LinkButton(String text, Skin skin, String style, String url) {
		super(text, skin, style);
		this.url = url;
		
		if(url == null || url.strip().isEmpty()) {
			this.setDisabled(true);
		}
		else {
			this.setDisabled(false);
		}
	}
	
	@Override
	public void clickImpl() {
		try {
			OSUtil.openBrowser(url);
		}
		catch(Exception e) {
			LOGGER.catching(e);
		}
		this.setChecked(false);
	}
	
}
