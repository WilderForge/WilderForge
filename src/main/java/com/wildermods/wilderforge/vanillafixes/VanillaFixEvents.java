package com.wildermods.wilderforge.vanillafixes;

import com.wildermods.provider.util.logging.Logger;
import com.wildermods.wilderforge.api.uiV1.TopLevelScreenChangeEvent;
import com.wildermods.wilderforge.launch.WilderForge;
import com.worldwalkergames.legacy.ui.detail.CharacterSheetPopup;
import com.worldwalkergames.ui.popup.IPopUp;
import com.worldwalkergames.ui.popup.PopUpManager;

import net.minecraftforge.eventbus.api.SubscribeEvent;

public class VanillaFixEvents {

	private static final Logger LOGGER = new Logger(VanillaFixEvents.class);
	
	/**
	 * Fixes #132
	 */
	@SubscribeEvent
	public static void onMainScreenChange(TopLevelScreenChangeEvent.Pre e) {
		final PopUpManager popupManager = WilderForge.getViewDependencies().popUpManager;
		if(popupManager != null) {
			for(IPopUp popup : popupManager.getPopups()) {
				if(popup instanceof CharacterSheetPopup) {
					LOGGER.info("Removing character sheet popup before scene change. (Changing from " + e.getPrevScreen() + " to " + e.getNewScreen() + ")");
					popupManager.removePopUp(popup);
				}
			}
		}
	}
	
}
