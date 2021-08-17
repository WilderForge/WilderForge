package com.wildermods.wilderforge.launch.exception;

import com.wildermods.wilderforge.launch.Coremod;

@SuppressWarnings("serial")
public class CoremodNotFoundError extends CoremodLinkageError {

	public CoremodNotFoundError(Coremod declarer, Coremod requiredDep) {
		super(declarer + " is missing required dependency " + requiredDep);
	}
	
}
