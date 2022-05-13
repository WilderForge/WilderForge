package com.wildermods.wilderforge.api.mechanicsV1;

import com.wildermods.wilderforge.mixins.ui.MainScreenAccessor;
import com.worldwalkergames.legacy.LegacyDesktop;
import com.worldwalkergames.legacy.ui.MainScreen;

public interface LegacyDesktopWF {

	public MainScreen getMainScreen();
	
	public default MainScreenAccessor getMainScreenAccessor() {
		return (MainScreenAccessor) getMainScreen();
	}
	
	public default LegacyDesktop convert() {
		return (LegacyDesktop)this;
	}
	
}
