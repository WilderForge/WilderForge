package com.wildermods.wilderforge.api.overlandV1.party;

import com.wildermods.wilderforge.api.eventV1.Event;
import com.wildermods.wilderforge.mixins.overland.party.PartyLogicMixin;
import com.worldwalkergames.engine.EID;
import com.worldwalkergames.legacy.game.campaign.model.Party;

public abstract class PartyMemberRemoveEvent extends Event {
	
	protected final PartyLogicMixin partyLogic;
	protected Party party;
	protected EID member;
	
	public PartyMemberRemoveEvent(boolean cancellable, PartyLogicMixin partyLogic, Party party, EID member) {
		super(cancellable);
		this.partyLogic = partyLogic;
		this.party = party;
		this.member = member;
	}
	
	public final PartyLogicMixin getPartyLogic() {
		return partyLogic;
	}
	
	public final Party getParty() {
		return party;
	}

	public final EID getMember() {
		return member;
	}
	
	public static class Pre extends PartyMemberRemoveEvent {

		public Pre(PartyLogicMixin partyLogic, Party party, EID member) {
			super(true, partyLogic, party, member);
		}
		
	}
	
	public static class Post extends PartyMemberRemoveEvent {

		public Post(PartyLogicMixin partyLogic, Party party, EID member) {
			super(false, partyLogic, party, member);
		}
		
	}
}
