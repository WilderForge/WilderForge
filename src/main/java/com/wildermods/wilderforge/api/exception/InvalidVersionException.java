package com.wildermods.wilderforge.api.exception;

/**
 * Thrown to indicate that a version string is not valid
 */
@SuppressWarnings("serial")
public class InvalidVersionException extends RuntimeException {

	public InvalidVersionException(String s) {
		super(s);
	}
	
	public InvalidVersionException(String s, Throwable t) {
		super(s, t);
	}
	
}
