package com.wildermods.wilderforge.api.traitV1;

public class Trait<V> {

	private final String name;
	private Class<? extends V> type;
	private V value;
	
	public Trait(String name) {
		this(name, null);
	}
	
	@SuppressWarnings("unchecked")
	public Trait(String name, V value) {
		this.name = name;
		if(value == null) {
			this.type = null;
		}
		else {
			this.type = (Class<? extends V>) value.getClass();
		}
		setValue(value);
	}
	
	public String getName() {
		return name;
	}
	
	public V getValue() {
		return value;
	}
	
	public void setValue(V value) {
		if(value != null && type == null) {
			throw new IllegalArgumentException("Traits instantiated with a null type cannot be set to anything other than null.");
		}
		if(value == null || type.isInstance(value)) {
			this.value = value;
		}
		else {
			throw new IllegalArgumentException("Trait " + name + " is expecting an instance of " + type.getSimpleName() + ", got an instance of " + value.getClass().getSimpleName() + " instead.");
		}
	}
	
	@Override
	public final boolean equals(Object o) {
		if(o instanceof Trait) {
			return ((Trait<?>) o).name.equals(name);
		}
		return false;
	}
	
	@Override
	public final int hashCode() {
		return name.hashCode();
	}
	
}
