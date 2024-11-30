package com.wildermods.wilderforge.api.utils;

import java.lang.reflect.Field;
import java.util.HashSet;

import com.google.common.collect.Sets;

public class TypeUtil {

	private static final HashSet<Class<?>> boxedClasses = Sets.newHashSet(
			Long.class, Integer.class, Boolean.class, Double.class, Float.class, Short.class, Byte.class, Character.class, Void.class);
	
	public static final HashSet<Class<?>> unboxedClasses = Sets.newHashSet(
			long.class, int.class, boolean.class, double.class, float.class, short.class, byte.class, char.class, void.class);
	
	private static final HashSet<Class<?>> boxableClasses = Sets.newHashSet();
	static {
		boxableClasses.addAll(boxedClasses);
		boxableClasses.addAll(unboxedClasses);
	}
	
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
	
	public static final boolean isLong(Field f) {
		return isLong(f.getType());
	}
	
	public static final boolean isInt(Field f) {
		return isInt(f.getType());
	}
	
	public static final boolean isBoolean(Field f) {
		return isBoolean(f.getType());
	}
	
	public static final boolean isDouble(Field f) {
		return isDouble(f.getType());
	}
	
	public static final boolean isFloat(Field f) {
		return isFloat(f.getType());
	}
	
	public static final boolean isShort(Field f) {
		return isShort(f.getType());
	}
	
	public static final boolean isByte(Field f) {
		return isByte(f.getType());
	}
	
	public static final boolean isChar(Field f) {
		return isChar(f.getType());
	}
	
	public static final boolean isVoid(Field f) {
		return isVoid(f.getType());
	}
	
	public static final boolean isNumeric(Class<?> type) {
		return representsPrimitive(type) && !isBoolean(type);
	}
	
	public static final boolean isDecimal(Class<?> type) {
		return isFloat(type) || isDouble(type);
	}
	
	public static final boolean isIntegral(Class<?> type) {
		return isNumeric(type) && !isDecimal(type);
	}
	
	public static final boolean isBoxed(Class<?> type) {
		return boxedClasses.contains(type);
	}
	
	public static final boolean isUnboxed(Class<?> type) {
		return unboxedClasses.contains(type);
	}
	
	public static final boolean isNumeric(Field f) {
		return isNumeric(f.getType());
	}
	
	public static final boolean isDecimal(Field f) {
		return isDecimal(f.getType());
	}
	
	public static final boolean isIntegral(Field f) {
		return isIntegral(f.getType());
	}
	
	public static final boolean isBoxed(Field f) {
		return isBoxed(f.getType());
	}
	
	public static final boolean isUnboxed(Field f) {
		return isBoxed(f.getType());
	}
	
	public static final long asIntegralPrimitive(Object o) {
		if(isIntegral(o.getClass())) {
			if(o instanceof Number) {
				return ((Number) o).longValue();
			}
			else if(o instanceof Character) {
				return ((Character) o).charValue();
			}
		}
		throw new IllegalArgumentException(o + "");
	}
	
	public static final double asDecimalPrimitive(Object o) {
		if(isDecimal(o.getClass())) {
			if(o instanceof Number) {
				return ((Number) o).doubleValue();
			}
		}
		throw new IllegalArgumentException(o + "");
	}
	
	public static boolean representsPrimitive(Class<?> type) {
		return boxableClasses.contains(type) && !isVoid(type);
	}
	
	public static boolean representsPrimitive(Field f) {
		return representsPrimitive(f.getType());
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
