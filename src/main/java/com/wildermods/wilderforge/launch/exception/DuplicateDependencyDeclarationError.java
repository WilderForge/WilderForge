package com.wildermods.wilderforge.launch.exception;

import com.wildermods.wilderforge.launch.Coremod;

@SuppressWarnings("serial")
public class DuplicateDependencyDeclarationError extends CoremodLinkageError {

	public DuplicateDependencyDeclarationError(Coremod declarer, Coremod declaree) {
		super(declarer + " has declared " + declaree + " as a dependency more than once!");
	}
	
}
