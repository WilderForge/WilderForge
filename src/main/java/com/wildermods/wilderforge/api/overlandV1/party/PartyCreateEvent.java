package com.wildermods.wilderforge.api.overlandV1.party;

import com.badlogic.gdx.utils.Array;
import com.wildermods.wilderforge.api.eventV1.Event;
import com.worldwalkergames.legacy.game.campaign.event.PartyProposal;
import com.worldwalkergames.legacy.game.campaign.model.Party;
import com.worldwalkergames.legacy.game.campaign.model.Party.TravelGroup;

public abstract class PartyCreateEvent extends Event {
	
	protected final PartyProposal proposal;
	protected final Array<Party.TravelGroup> oldTravelGroups;
	protected boolean removeJobs;
	
	public PartyCreateEvent(PartyProposal proposal, boolean removeJobs, Array<Party.TravelGroup> oldTravelGroups, boolean cancellable) {
		super(cancellable);
		this.proposal = proposal;
		this.oldTravelGroups = oldTravelGroups;
		this.removeJobs = removeJobs;
		
	}
	
	public boolean isRemovingJobs() {
		return this.removeJobs;
	}
	
	public PartyProposal getPartyProposal() {
		return proposal;
	}
	
	public Array<Party.TravelGroup> getOldTravelGroups() {
		return oldTravelGroups;
	}
	
	/**
	 * Fires directly before a party is added to the game kernel.
	 * 
	 * While this event is cancellable, cancelling it is not recommended.
	 * 
	 * Cancelling this event will prevent all heros from doing the current 
	 * task the user has selected. You will have to work around this yourself
	 * if you decide to cancel this event.
	 */
	public static final class Pre extends PartyCreateEvent {

		public Pre(PartyProposal proposal, boolean removeJobs, Array<TravelGroup> oldTravelGroups) {
			super(proposal, removeJobs, oldTravelGroups, true);
		}
		
		public void setRemoveJobs(boolean removeJobs) {
			this.removeJobs = removeJobs;
		}
		
	}
	
	/**
	 * Fired directly after a party is added to the game kernel.
	 */
	public static final class Post extends PartyCreateEvent {

		public Post(PartyProposal proposal, boolean removeJobs, Array<TravelGroup> oldTravelGroups) {
			super(proposal, removeJobs, oldTravelGroups, false);
		}
		
	}
	
}
