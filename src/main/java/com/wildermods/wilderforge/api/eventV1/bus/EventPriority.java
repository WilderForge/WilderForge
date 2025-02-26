package com.wildermods.wilderforge.api.eventV1.bus;

/**
 * Use {@link net.minecraftforge.eventbus.api.EventPriority}
 */
@Deprecated(forRemoval = true)
public interface EventPriority {

	public static final int LOWER = -10000;
	public static final int LOW = -5000;
	public static final int NORMAL = 0;
	public static final int HIGH = 5000;
	public static final int HIGHER = 10000;
	
}
