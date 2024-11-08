package com.wildermods.wilderforge.launch.exception;

@SuppressWarnings("serial")
public class EventTargetError extends Error {
	
	public EventTargetError(String message) {
		super(message);
	}
	
	public EventTargetError(String message, Throwable cause) {
		super(message, cause);
	}
	
	public EventTargetError(Throwable cause) {
		super(cause);
	}
	
}
