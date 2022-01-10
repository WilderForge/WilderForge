package com.wildermods.wilderforge.api.overlandV1.party;

import java.util.concurrent.CancellationException;

import com.wildermods.wilderforge.api.eventV1.Event;
import com.worldwalkergames.legacy.game.campaign.model.Party;

/*
 * Fires when the game attempts to disband a party
 * 
 * This event may fire more than once per disbandonment attempt
 */
public class PartyDisbandEvent extends Event {

	//It doesn't make sense to have a POST event, because doing anything
	//to a party after it has been disbanded makes no sense.
	
	private final Party party;
	
	public PartyDisbandEvent(Party party) {
		super(true);
		this.party = party;
	}
	
	public Party getParty() {
		return party;
	}
	
	@Override
	public final void setCancelled(boolean cancelled) {
		if(party.members.size() < 1) {
			CancellationException c = new CancellationException();
			IllegalStateException e = new IllegalStateException("Cannot cancel the disbandonment of a party with zero members!");
			c.initCause(e);
			throw c;
		}
		else if (party.orders.first() == null) {
			CancellationException c = new CancellationException();
			IllegalStateException e = new IllegalStateException("Cannot cancel the disbandonment of a party with no orders!");
			c.initCause(e);
			throw c;
		}
		super.setCancelled(cancelled);
	}
	
	@Override
	public boolean canBeCancelled() {
		if(super.canBeCancelled()) {
			return party.members.size() > 0 && party.orders.first() != null;
		}
		return false;
	}
	
}
