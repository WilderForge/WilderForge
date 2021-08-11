package com.wildermods.wilderforge.launch.exception;

/**
 * Subclasses of this error are thrown when wilderforge
 * is unable to load a dependency
 */

@SuppressWarnings("serial")
public class CoremodLinkageError extends Error {
	
	public CoremodLinkageError(String s) {
		super(s);
	}
	
	public CoremodLinkageError(String s, Throwable t) {
		super(s, t);
	}
	
	public CoremodLinkageError(Throwable t) {
		super(t);
	}

}
