package com.wildermods.wilderforge.api.mixins.v1;

public class Impossible {

	public static final <T> T error() throws AssertionError {
		throw new AssertionError();
	}
	
}
