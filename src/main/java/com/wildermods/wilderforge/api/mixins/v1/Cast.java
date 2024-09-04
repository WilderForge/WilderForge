package com.wildermods.wilderforge.api.mixins.v1;

public class Cast {

	@SuppressWarnings("unchecked")
	public static <T> T from(Object o) {
		return (T)(Object)o;
	}
	
}
