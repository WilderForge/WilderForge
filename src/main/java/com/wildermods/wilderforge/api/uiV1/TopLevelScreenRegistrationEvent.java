package com.wildermods.wilderforge.api.uiV1;

import com.wildermods.wilderforge.api.eventV2.Event;
import com.worldwalkergames.engine.DrivableContainer;
import com.worldwalkergames.legacy.ui.titlescreen.ITopLevelScreen;

public class TopLevelScreenRegistrationEvent extends Event {

	public TopLevelScreenRegistrationEvent() {
		super(false);
	}
	
	public <T extends DrivableContainer & ViewStated & ITopLevelScreen> void register(Class<T> screenClass) {
		
	}

}
