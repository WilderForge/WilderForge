package com.wildermods.wilderforge.launch.exception;

import com.wildermods.wilderforge.launch.coremods.Coremod;

@SuppressWarnings("serial")
public class DuplicateDependencyDeclarationError extends CoremodLinkageError {

	public DuplicateDependencyDeclarationError(Coremod declarer, Coremod declaree) {
		super(declarer + " has declared " + declaree + " as a dependency more than once!");
	}
	
	public DuplicateDependencyDeclarationError(String declarer, Coremod declaree) {
		super(declarer + " has delcared " + declaree + " as a dependency more than once!");
	}
	
}
