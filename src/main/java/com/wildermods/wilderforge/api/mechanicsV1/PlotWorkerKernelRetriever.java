package com.wildermods.wilderforge.api.mechanicsV1;

import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Field;
import com.badlogic.gdx.utils.reflect.ReflectionException;

import com.wildermods.wilderforge.mixins.GameKernelAccessor;
import com.worldwalkergames.legacy.game.mechanics.PlotWorker;

public interface PlotWorkerKernelRetriever {
	
	public static Field KERNEL_FIELD = getKernelField();
	
	//TODO: Use shadow classes when mixin 0.9 is released instead of using reflection
	public default GameKernelAccessor getKernelWF() {
		try {
			return (GameKernelAccessor) KERNEL_FIELD.get(this);
		} catch (ReflectionException e) {
			throw new AssertionError();
		}
	}

	public static Field getKernelField() {
		try {
			return ClassReflection.getDeclaredField(PlotWorker.class, "kernel");
		} catch (ReflectionException e) {
			throw new AssertionError(e);
		}
	}
	
}
