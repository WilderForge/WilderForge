package com.wildermods.wilderforge.api.mechanicsV1;

import com.wildermods.wilderforge.api.eventV1.Event;
import com.worldwalkergames.legacy.game.mission.model.Participant;

public class UnpauseEvent extends Event {

	private final Participant requester;
	
	public UnpauseEvent(Participant requester) {
		super(true);
		this.requester = requester;
	}
	
	public boolean wasRequested() {
		return requester != null;
	}
	
	public Participant getRequester() {
		return requester;
	}

}
