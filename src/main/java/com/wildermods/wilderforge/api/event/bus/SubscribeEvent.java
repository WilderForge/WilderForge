package com.wildermods.wilderforge.api.event.bus;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(METHOD)
public @interface SubscribeEvent {
	/**
	 * <p>Whether this event subscriber can receive events that have been cancelled
	 * 
	 * <p>Setting this when subscribing to an event that cannot be cancelled has no effect.
	 * 
	 * @return true to receive cancelled events, false otherwise. Default: false
	 */
	boolean acceptCancelled() default false;
	
	/**
	 * <p>The priority of the event subscriber for the annotated method.
	 * 
	 * <p>Lower priority subscribers receive events sooner than higher priority subscribers. This is to
	 * ensure that higher priority subscribers have the final say on if the event is cancelled or not.
	 * 
	 * <p>If two or more subscribers have the same priority, the order that they will fire in is undefined.
	 * 
	 * <p>It is highly recommended to keep the priority at 0 unless you need to receive an event before or
	 * after another subscriber processes the event.
	 * 
	 * @see {@link EventPriority} for helper priorities.
	 * 
	 * @return The priority of the subscriber for the annotated method. Default: 0 ({@link EventPriority#NORMAL})
	 */
	int priority() default EventPriority.NORMAL;
}
