package com.wildermods.wilderforge.api.uiV1.elements.buttons;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.wildermods.wilderforge.api.uiV1.UIButton;
import com.wildermods.wilderforge.api.utils.vanillafixes.TranslateForShellStatus;
import com.wildermods.wilderforge.launch.logging.Logger;
import com.wildermods.wilderforge.mixins.PopUpAccessorMixin;
import com.worldwalkergames.legacy.context.LegacyViewDependencies;
import com.worldwalkergames.legacy.ui.PopUp;
import com.worldwalkergames.util.OSUtil;

public class LinkButton extends UIButton<URL> implements TranslateForShellStatus {

	public static final Logger LOGGER = new Logger(LinkButton.class);
	
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
		String url = this.url;
		try {
			if(!isWilderforgePatchingOpenBrowser()) { //we are running a patched version of the game, remove the scheme
				url = removeScheme(url);
			}
			OSUtil.openBrowser(url);
		}
		catch(Exception e) {
			LOGGER.catching(e);
		}
		this.setChecked(false);
	}
	
	private static String removeScheme(String url) {
		if (url == null || url.isEmpty()) {
			return url; // Return the original URL if it's null or empty
		}

		try {
			URI uri = new URI(url);
			// return the URL without the scheme
			return uri.getRawSchemeSpecificPart(); // Get everything after the scheme
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return url; // Return the original URL if there's a syntax error
		}
	}
	
}
