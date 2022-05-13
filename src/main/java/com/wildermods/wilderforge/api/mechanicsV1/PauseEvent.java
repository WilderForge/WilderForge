package com.wildermods.wilderforge.api.mechanicsV1;

import com.wildermods.wilderforge.api.eventV1.Event;
import com.worldwalkergames.legacy.game.mission.model.Participant;


/**
 * Fired when the game is paused
 */
public class PauseEvent extends Event {
	
	private final Participant requester;
	private boolean showAbilityBar;
	
	public PauseEvent(Participant requester) {
		super(true);
		this.requester = requester;
	}
	
	public boolean wasRequested() {
		return requester != null;
	}
	
	public Participant getRequester() {
		return requester;
	}
	
	public void showAbilityBarIfCancelled(boolean showAbilityBar) {
		this.showAbilityBar = showAbilityBar;
	}
	
	public boolean doesShowAbilityBarIfCancelled() {
		return showAbilityBar;
	}
	
}
