package com.wildermods.wilderforge.launch.exception;

/**
 * Thrown when Wilderforge attempts to load a coremod and determines that the file is malformed or otherwise
 * cannot be interpreted as a valid coremod.
 */

@SuppressWarnings("serial")
public class CoremodFormatError extends CoremodLinkageError {

	public CoremodFormatError(String s) {
		super(s);
	}
	
	public CoremodFormatError(String s, Throwable t) {
		super(s, t);
	}
	
	public CoremodFormatError(Throwable t) {
		super(t);
	}

}
