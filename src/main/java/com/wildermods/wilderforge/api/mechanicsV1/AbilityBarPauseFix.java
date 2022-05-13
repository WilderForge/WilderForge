package com.wildermods.wilderforge.api.mechanicsV1;

public interface AbilityBarPauseFix {
	
	public void setPauseEvent(PauseEvent e);
	public default void clearPauseEvent() {
		setPauseEvent(null);
	}
	
}
