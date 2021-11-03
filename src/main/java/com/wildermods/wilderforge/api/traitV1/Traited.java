package com.wildermods.wilderforge.api.traitV1;

import java.util.HashMap;

import org.jetbrains.annotations.Nullable;

public interface Traited {

	public boolean hasTrait(String name);
	
	public boolean hasTrait(String name, Object value);
	
	@Nullable
	public <T> T getTrait(String name);
	
	public <T> void setTrait(String name, T value) throws IllegalArgumentException;
	
	@Nullable
	public <T> Trait<T> getRawTrait(String name);
	
	public HashMap<String, Trait<?>> getTraits();
	
}
