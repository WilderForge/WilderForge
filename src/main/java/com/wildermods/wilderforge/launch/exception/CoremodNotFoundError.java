package com.wildermods.wilderforge.launch.exception;

import com.wildermods.wilderforge.launch.coremods.Coremod;

@SuppressWarnings("serial")
public class CoremodNotFoundError extends CoremodLinkageError {

	public CoremodNotFoundError(Coremod declarer, Coremod requiredDep) {
		super(declarer + " is missing required dependency " + requiredDep);
	}
	
	public CoremodNotFoundError(Coremod declarer, String requiredDep) {
		super(declarer + " is missing required dependency " + requiredDep);
	}
	
}
