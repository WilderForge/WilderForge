package com.wildermods.wilderforge.launch.exception;

/**
 * Thrown when Wilderforge detects a circularity in the dependency hierarchy of a mod being
 * loaded.
 */

@SuppressWarnings("serial")
public class DependencyCircularityError extends CoremodLinkageError {

	public DependencyCircularityError(String message) {
		super(message);
	}

}
