package com.wildermods.wilderforge.launch.exception;

import com.wildermods.wilderforge.launch.Coremod;

/**
 * Thrown when two coremods are loaded with the same modid
 * 
 * It may be the same coremod as in separate files, or
 * completely different coremods which have the same
 * modid. Regardless, loading cannot continue.
 */

@SuppressWarnings("serial")
public class DuplicateCoremodError extends CoremodLinkageError {

	public DuplicateCoremodError(Coremod dupe) {
		super("Two or more coremods found with the same modid: " + dupe);
	}
	
}
