package com.wildermods.wilderforge.mixins.engine;

import java.util.HashSet;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.badlogic.gdx.utils.Array;
import com.worldwalkergames.engine.DrivableContainer;
import com.worldwalkergames.engine.IDrivable;

@Mixin(DrivableContainer.class)
public abstract class DrivableContainerMixin {

	protected @Shadow Array<IDrivable> children;
	
	@SuppressWarnings("unchecked")
	public <Child extends IDrivable> Child getChild(Class<Child> type) {
		for(IDrivable child : children) {
			if(type.isInstance(child)) {
				return (Child)child;
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public <Child extends IDrivable> HashSet<Child> getChildren(Class<Child> type) {
		HashSet<Child> ret = new HashSet<Child>();
		for(IDrivable child : children) {
			if(type.isInstance(child)) {
				ret.add((Child)child);
			}
		}
		return ret;
	}
}
