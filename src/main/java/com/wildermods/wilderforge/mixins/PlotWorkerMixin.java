package com.wildermods.wilderforge.mixins;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Field;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.worldwalkergames.legacy.game.mechanics.PlotWorker;

@Mixin(value = PlotWorker.class)
public abstract class PlotWorkerMixin {
	
	private static @Final @Unique Field KERNEL_FIELD = getKernelField();
	
	//TODO: Use shadow classes when mixin 0.9 is released instead of using reflection
	public GameKernelAccessor getKernelWF() {
		try {
			return (GameKernelAccessor) KERNEL_FIELD.get(this);
		} catch (ReflectionException e) {
			throw new AssertionError();
		}
	}

	private static final Field getKernelField() {
		try {
			return ClassReflection.getDeclaredField(PlotWorker.class, "kernel");
		} catch (ReflectionException e) {
			throw new AssertionError(e);
		}
	}
	
}
