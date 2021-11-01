package com.wildermods.wilderforge.api.enumV1;

public class EnumExtensionFailureError extends Error {

	EnumExtensionFailureError(Class<Enum<?>> eenum, Throwable t) {
		super("Could not create extended enum ", t);
	}
	
}
