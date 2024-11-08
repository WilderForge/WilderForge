package com.wildermods.wilderforge.api.utils;

import java.util.HashSet;

import com.google.common.collect.Sets;

public class TypeUtil {

	private static final HashSet<Class<?>> boxableClasses = Sets.newHashSet(long.class, Long.class, int.class, Integer.class,
			boolean.class, Boolean.class, double.class, Double.class, float.class, Float.class, short.class, Short.class,
			byte.class, Byte.class, char.class, Character.class, void.class, Void.class);
	
	public static final boolean isLong(Class<?> type) {
		return type == long.class || type == Long.class;
	}
	
	public static final boolean isInt(Class<?> type) {
		return type == int.class || type == Integer.class;
	}
	
	public static final boolean isBoolean(Class<?> type) {
		return type == boolean.class || type == Boolean.class;
	}
	
	public static final boolean isDouble(Class<?> type) {
		return type == double.class || type == Double.class;
	}
	
	public static final boolean isFloat(Class<?> type) {
		return type == float.class || type == Float.class;
	}
	
	public static final boolean isShort(Class<?> type) {
		return type == short.class || type == Short.class;
	}
	
	public static final boolean isByte(Class<?> type) {
		return type == byte.class || type == Byte.class;
	}
	
	public static final boolean isChar(Class<?> type) {
		return type == char.class || type == Character.class;
	}
	
	public static final boolean isVoid(Class<?> type) {
		return type == void.class || type == Void.class;
	}
	
	public static boolean representsPrimitive(Class<?> type) {
		return boxableClasses.contains(type) && !isVoid(type);
	}
	
	public static Number castDown(Object value, Class<? extends Number> type) {
		if(isLong(value.getClass()) && isInt(type)) {
			return ((Long)value).intValue();
		}
		if(isDouble(value.getClass()) && isFloat(type)) {
			return ((Double)value).floatValue();
		}
		throw new IllegalArgumentException();
	}
	
}
