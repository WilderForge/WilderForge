package com.wildermods.wilderforge.api.mechanicsV1;

import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Field;
import com.badlogic.gdx.utils.reflect.ReflectionException;

import com.wildermods.wilderforge.mixins.GameKernelAccessor;

public interface Kerneled {
	
	//TODO: Use shadow classes when mixin 0.9 is released instead of using reflection
	public default GameKernelAccessor getKernelWF() {
		try {
			System.out.println(this.getClass());
			return (GameKernelAccessor) getKernelField().get(this);
		} catch (ReflectionException e) {
			throw new AssertionError();
		}
	}

	public default Field getKernelField() {
		try {
			Field field = ClassReflection.getDeclaredField(getClass(), "kernel");
			field.setAccessible(true);
			return field;
		} catch (ReflectionException e) {
			throw new AssertionError(e);
		}
	}
	
}
