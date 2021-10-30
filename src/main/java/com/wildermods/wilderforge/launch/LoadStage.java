package com.wildermods.wilderforge.launch;

public enum LoadStage {

	STARTUP,
	PRE_INIT,
	INIT,
	POST_INIT,
	EXITED;
	
	static LoadStage currentStage = STARTUP;
	
	public static LoadStage getLoadStage() {
		return currentStage;
	}
	
	@InternalOnly
	public static void setLoadStage(LoadStage newStage) {
		if(currentStage.compareTo(newStage) == -1) {
			currentStage = newStage;
			return;
		}
		throw new IllegalStateException("Attempted to go from LoadStage " + currentStage + " to " + newStage);
	}
	
}
