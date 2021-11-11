package com.wildermods.wilderforge.api.eventV1;

import com.wildermods.wilderforge.launch.coremods.Coremod;
import com.wildermods.wilderforge.launch.coremods.Coremods;

/**
 * An event used to communicate between mods. Can store any type of data.
 * 
 * data is final. If you want mutable data, make your data type an array.
 * This is to make it harder for mods to inadvertently modify data they 
 * don't specifically intend to modify.
 *
 * @param <T> the type of data this communication event holds
 */
public final class ModCommunicationEvent<T extends Object> extends ModEvent {

	private final T data;
	
	public ModCommunicationEvent(String from, T data) {
		this(Coremods.getCoremod(from), data);
	}
	
	/**
	 * @param from the mod sending the event
	 * @param data the data to be sent
	 */
	public ModCommunicationEvent(Coremod from, T data) {
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
