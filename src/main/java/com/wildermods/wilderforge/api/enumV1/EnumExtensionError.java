package com.wildermods.wilderforge.api.enumV1;

@SuppressWarnings("serial")
public class EnumExtensionError extends Error {

	EnumExtensionError(Class<Enum<?>> eenum, Throwable t) {
		super("Could not create extended enum ", t);
	}
	
}
