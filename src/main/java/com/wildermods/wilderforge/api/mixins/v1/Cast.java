package com.wildermods.wilderforge.api.mixins.v1;

public class Cast {

	@SuppressWarnings("unchecked")
	public static <T> T from(Object o) {
		return (T)o;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T as(Object o, Class<T> type) {
		return (T)o;
	}
	
}
