package com.wildermods.wilderforge.launch.exception;

import com.wildermods.wilderforge.api.versionV1.MultiVersionRange;
import com.wildermods.wilderforge.launch.Coremod;

/**
 * Thrown when a coremod dependency is outside of a declared version range.
 */

@SuppressWarnings("serial")
public class CoremodVersionError extends CoremodLinkageError {
	
	public CoremodVersionError(Coremod requirer, Coremod badCoremod, MultiVersionRange versionRange) {
		super(requirer + " has dependency " + badCoremod + " within version range " + versionRange + ". " + badCoremod + " is version " + badCoremod.getVersion());
	}

}
