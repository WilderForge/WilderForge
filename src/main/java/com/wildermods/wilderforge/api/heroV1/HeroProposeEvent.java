package com.wildermods.wilderforge.api.heroV1;

import com.wildermods.wilderforge.api.eventV1.Event;
import com.worldwalkergames.legacy.game.campaign.model.RecruitChoice.Proposal;

public abstract class HeroProposeEvent extends Event {

	protected Proposal proposal;
	
	public HeroProposeEvent(Proposal proposal, boolean cancellable) {
		super(cancellable);
		this.proposal = proposal;
	}
	
	public Proposal getProposal() {
		return proposal;
	}
	
	public static class Pre extends HeroProposeEvent {

		public Pre(Proposal proposal) {
			super(proposal, true);
		}
		
	}
	
	public static class Post extends HeroProposeEvent {
		
		public Post(Proposal proposal) {
			super(proposal, false);
		}
		
	}

}
