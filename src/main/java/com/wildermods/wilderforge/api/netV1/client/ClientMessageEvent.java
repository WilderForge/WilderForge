package com.wildermods.wilderforge.api.netV1.client;

import com.wildermods.wilderforge.api.eventV1.Event;
import com.wildermods.wilderforge.launch.logging.Logger;
import com.wildermods.wilderforge.mixins.net.client.ClientAPIAccessor;
import com.worldwalkergames.communication.messages.Message;

public abstract class ClientMessageEvent extends Event {

	public static final Logger LOGGER = new Logger(ClientMessageEvent.class);
	
	protected final ClientAPIAccessor client;
	protected Message message;
	protected Boolean handled = null;
	
	public ClientMessageEvent(ClientAPIAccessor client, Message message, boolean cancellable) {
		super(cancellable);
		this.client = client;
		this.message = message;
	}
	
	public Message getMessage() {
		return message;
	}
	
	/**
	 * <p>{@link Boolean#TRUE} if the message has been successfully handled,
	 * <p><code>null</code> if the message has not yet been handled,
	 * <p>{@link Boolean#FALSE} if the message cannot be handled.
	 */
	public Boolean hasBeenHandled() {
		return handled;
	}
	
	/**
	 * The same as calling {@link #setHandled(Boolean.TRUE)}
	 */
	public void setHandled() {
		setHandled(Boolean.TRUE);
	}
	
	/**
	 * Set to true if you wish to handle the message and overwrite vanilla behavior.
	 * 
	 * Set to null if you wish for vanilla to handle the message
	 * 
	 * If set to false, ClientAPI will not handle the message, and instead it will
	 * be handled by vanilla's backup system. This is generally not desired.
	 * @param ret
	 */
	public void setHandled(Boolean hasBeenHandled) {
		handled = hasBeenHandled;
	}
	
	public ClientAPIAccessor getClient() {
		return client;
	}
	
	/**
	 * Fired once when ClientAPI receives a message, before any vanilla 
	 * matches have occured.
	 * 
	 * This event is not cancellable by normal means. To cancel the event
	 * you must call {@link #setHandled(Boolean)}
	 */
	public static class PreVanillaChecks extends ClientMessageEvent {

		public PreVanillaChecks(ClientAPIAccessor client, Message message) {
			super(client, message, false);
		}
		
	}
	
	/**
	 * Fired when ClientAPI has matched a message, but before vanilla
	 * handles it.
	 * 
	 * This should be used to overwrite the behavior of vanilla messages
	 * 
	 * This event is not cancellable by normal means. To cancel the event
	 * you must call {@link #setHandled(Boolean)}
	 */
	public static class OnVanillaCheck extends ClientMessageEvent {

		public OnVanillaCheck(ClientAPIAccessor client, Message message) {
			super(client, message, false);
		}

	}
	
	/**
	 * Fired when ClientAPI could not match a message, but before
	 * it sends it to vanilla's backup message handler.
	 * 
	 * This should be used for modded messages.
	 * 
	 * This event is not cancellable by normal means. To cancel the event
	 * you must call {@link #setHandled(Boolean)}
	 */
	public static class PostVanillaChecks extends ClientMessageEvent {
		
		public PostVanillaChecks(ClientAPIAccessor client, Message message) {
			super(client, message, false);
		}
		
	}
	
}
