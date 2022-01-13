package com.wildermods.wilderforge.api.heroV1;

import com.wildermods.wilderforge.api.eventV1.Event;
import com.worldwalkergames.engine.Entity;
import com.worldwalkergames.engine.IEntity;
import com.worldwalkergames.legacy.game.campaign.model.Hero;
import com.worldwalkergames.legacy.game.campaign.model.Position;
import com.worldwalkergames.legacy.game.mechanics.EffectResolver.EffectContext;
import com.worldwalkergames.legacy.game.model.effect.Outcome.ChangeControl;

public abstract class HeroEvent extends Event {
	
	protected Entity hero;
	
	public HeroEvent(Entity hero, boolean cancellable) {
		super(cancellable);
		this.hero = hero;
	}
	
	public Entity getHeroEntity() {
		return hero;
	}
	
	public Hero getEntityAsHero() {
		return Hero.of(hero);
	}

	/**
	 * Fires when a hero is recruited.
	 * 
	 * Note that sometimes instead of this event,
	 * {@link ControlChange} is fired instead.
	 * 
	 * The most notable example of this is when
	 * a new hero is recruited from a town.
	 *
	 */
	public static abstract class Recruit extends HeroEvent {
		
		protected IEntity place;
		
		public Recruit(Entity hero, boolean cancellable) {
			this(hero, Position.of(hero), cancellable);
		}
		
		public Recruit(Entity hero, IEntity place, boolean cancellable) {
			super(hero, cancellable);
			this.place = place;
		}
		
		public IEntity getPlace() {
			return this.place;
		}
		
		public static class Pre extends Recruit {

			public Pre(Entity recruit) {
				this(recruit, Position.of(recruit));
			}
			
			public Pre(Entity recruit, IEntity place) {
				super(recruit, place, true);
			}
			
			public void setRecruit(Entity recruit) {
				this.hero = recruit;
			}
			
			public void setPlace(IEntity place) {
				this.place = place;
			}
			
		}
		
		public static class Post extends Recruit {

			public Post(Entity recruit) {
				this(recruit, Position.of(recruit));
			}
			
			public Post(Entity recruit, IEntity place) {
				super(recruit, place, false);
			}
			
		}
		
	}
	
	/**
	 * Fired when a hero's controller changes.
	 * 
	 * Note that this event is also fired when
	 * a hero is recruited from a town. Likely
	 * because the hero exists in the town, but
	 * is not part of the party. 
	 * 
	 * Its controller is simply changed to make
	 * the new hero part of the party.
	 * 
	 **/
	public static abstract class ControlChange extends HeroEvent {
		
		protected final EffectContext context;
		protected ChangeControl changeControl;
		
		public ControlChange(EffectContext context, Entity hero, ChangeControl changeControl, boolean cancellable) {
			super(hero, cancellable);
			this.context = context;
			this.changeControl = changeControl;
		}
		
		public EffectContext getEffectContext() {
			return context;
		}
		
		public ChangeControl getChangeControl() {
			return changeControl;
		}
		
		public static final class Pre extends ControlChange {

			public Pre(EffectContext context, Entity hero, ChangeControl changeControl) {
				super(context, hero, changeControl, true);
			}
			
		}
		
		public static final class Post extends ControlChange {

			public Post(EffectContext context, Entity hero, ChangeControl changeControl) {
				super(context, hero, changeControl, false);
			}
			
		}
		
	}
	
}
