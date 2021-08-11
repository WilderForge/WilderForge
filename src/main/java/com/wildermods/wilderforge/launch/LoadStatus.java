package com.wildermods.wilderforge.launch;

import com.worldwalkergames.logging.ALogger;

public enum LoadStatus {
	
	/**
	 * Wilderforge does not know about a mod with that id.
	 * 
	 * If a coremod appears as unreferenced, it generally means
	 * that something has gone wrong.
	 */
	UNREFERENCED,

	/**
	 * Wilderforge has a reference to a mod with this name but has not discovered it.
	 * 
	 * This generally means that the mod in question has been declared as a required
	 * or optional dependency of another mod, but wilderforge hasn't discovered it yet. 
	 * 
	 * Coremods that are referenced as a dependency but do not exist at runtime
	 * (aka UNDISCOVERED) will remain in this state.
	 * 
	 * Implementation detail: this is also the state of the 'wildermyth' and 'wilderforge' 
	 * coremods as soon as launching is attempted.
	 */
	UNDISCOVERED,
	
	/**
	 * The coremod in question has been discovered, loading has not yet been attempted.
	 */
	DISCOVERED,
	
	/**
	 * The coremod in question is loading.
	 */
	LOADING,
	
	/**
	 * The coremod in question was found, but could not load due to an error.
	 */
	ERRORED,
	
	/**
	 * The coremod in question was found, but not loaded.
	 */
	NOT_LOADED,
	
	/**
	 * The coremod in question was found and loaded successfully.
	 */
	LOADED;
	
	ALogger logger;
	
}
