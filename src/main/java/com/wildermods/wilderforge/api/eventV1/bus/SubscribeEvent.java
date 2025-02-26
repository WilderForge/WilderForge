package com.wildermods.wilderforge.api.eventV1.bus;

import static java.lang.annotation.ElementType.METHOD;
import net.minecraftforge.eventbus.api.EventPriority;

import java.lang.annotation.Target;

/**
 * Use {@link net.minecraftforge.eventbus.api.SubscribeEvent}
 */
@Deprecated(forRemoval = true)
@Target(METHOD)
public @interface SubscribeEvent {
	
	/**
	 * Use {@link net.minecraftforge.eventbus.api.SubscribeEvent#receiveCanceled()}
	 */
	@Deprecated(forRemoval = true)
	boolean acceptCancelled() default false;
	
	@Deprecated(forRemoval = false)
	int priority() default EventPriority.NORMAL;
}
