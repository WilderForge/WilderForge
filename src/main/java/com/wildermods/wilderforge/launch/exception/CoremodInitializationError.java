package com.wildermods.wilderforge.launch.exception;

import com.wildermods.wilderforge.launch.coremods.Coremod;

/**
 * Thrown whenwilderforge attempts to load a coremod, but the coremod has
 * thrown an exception when it was being loaded
 */

@SuppressWarnings("serial")
public class CoremodInitializationError extends CoremodLinkageError {

	public CoremodInitializationError(Coremod coremod, Throwable t) {
		super("Could not load " + coremod.value() + " because it threw an exception ", t);
	}
	
}
