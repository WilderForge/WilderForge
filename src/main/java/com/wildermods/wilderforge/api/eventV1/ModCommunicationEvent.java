package com.wildermods.wilderforge.api.eventV1;

import com.wildermods.wilderforge.api.eventV2.ModEvent;
import com.wildermods.wilderforge.api.modLoadingV1.CoremodInfo;
import com.wildermods.wilderforge.launch.coremods.Coremods;

/**
 * @deprecated, use {@link com.wildermods.wilderforge.api.eventV2.ModCommunicationEvent}
 * @param <T>
 */
@Deprecated(forRemoval = true)
public class ModCommunicationEvent<T extends Object> extends ModEvent {

	private final T data;
	
	public ModCommunicationEvent(String from, T data) {
		this(Coremods.getCoremod(from), data);
	}
	
	/**
	 * @param from the mod sending the event
	 * @param data the data to be sent
	 */
	public ModCommunicationEvent(CoremodInfo from, T data) {
		super(from, false);
		this.data = data;
	}

	/**
	 * @return the data stored in this event. The data will not have changed, unless an array type is returned. If an array is returned, the contents and size of the array may have been modified.
	 */
	public T getData() {
		return data;
	}
	
}
