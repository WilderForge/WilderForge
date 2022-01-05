package com.wildermods.wilderforge.api.mechanicsV1;

import java.lang.reflect.Field;

import com.wildermods.wilderforge.mixins.GameKernelAccessor;

public interface Kerneled {
	
	//TODO: Use shadow classes when mixin 0.9 is released instead of using reflection
	public default GameKernelAccessor getKernelWF() {
		try {
			System.out.println(this.getClass());
			return (GameKernelAccessor) getKernelField().get(this);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new AssertionError();
		}
	}

	public default Field getKernelField() throws NoSuchFieldException {
		Field kernelField = null;
		Class<?> c = getClass();
		while(c.getSuperclass() != null && kernelField == null) {
			try { 
				kernelField = c.getDeclaredField("kernel");
				kernelField.setAccessible(true);
			}
			catch(NoSuchFieldException e) {}
			c = c.getSuperclass();
		}
		if(kernelField == null) {
			throw new NoSuchFieldException("No field named kernel in " + getClass().getName() + " or it's superclasses!");
		}
		return kernelField;
	}
	
}
