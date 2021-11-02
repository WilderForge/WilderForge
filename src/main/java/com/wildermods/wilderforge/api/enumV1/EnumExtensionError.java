package com.wildermods.wilderforge.api.enumV1;

@SuppressWarnings("serial")
public class EnumExtensionError extends Error {

	public EnumExtensionError(Throwable t) {
		super("Could not create extended enum ", t);
	}
	
	public EnumExtensionError(String description) {
		super(description);
	}
	
}
