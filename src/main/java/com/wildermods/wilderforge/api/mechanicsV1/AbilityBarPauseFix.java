package com.wildermods.wilderforge.api.mechanicsV1;

import com.worldwalkergames.engine.EID;

public interface AbilityBarPauseFix {
	
	public void setPauseEvent(PauseEvent e);
	public boolean mustReset(EID selected);
	public default void clearPauseEvent() {
		setPauseEvent(null);
	}
	
}
