package com.wildermods.wilderforge.launch.exception;

import com.wildermods.wilderforge.api.configV1.Config;
import com.wildermods.wilderforge.api.configV1.ConfigEntry;
import com.wildermods.wilderforge.api.configV1.BadConfigValueEvent;

/**
 * Indicates one of the following:
 * 
 * 1. The class annotated with @{@link Config} is malformed and
 * therefore unreadable. It is not possible to create the
 * configuration object.
 * 
 * 2. A value read from the configuration file does not match
 * its defined {@link ConfigEntry}, and it was not properly
 * corrected after the relevant {@link BadConfigValueEvent} was fired.
 * 
 */
@SuppressWarnings("serial")
public class ConfigurationError extends VerifyError {

	public ConfigurationError(String message, Throwable cause) {
		super(message);
		initCause(cause);
	}
	
	public ConfigurationError(String message) {
		super(message);
	}
	
	public static class InvalidRangeError extends ConfigurationError {

		public InvalidRangeError(String message) {
			super(message);
		}
		
	}
	
}
