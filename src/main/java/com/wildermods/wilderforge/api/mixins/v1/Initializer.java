package com.wildermods.wilderforge.api.mixins.v1;

public interface Initializer {

	public static final String CONSTRUCTOR = "<init>";
	public static final String DEFAULT_CONSTRUCTOR = CONSTRUCTOR + "()" + Descriptor.VOID;
	public static final String STATIC_INIT = "<clinit>";
	
}
