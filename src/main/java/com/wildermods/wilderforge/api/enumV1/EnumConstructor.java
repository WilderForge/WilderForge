package com.wildermods.wilderforge.api.enumV1;

@FunctionalInterface
public interface EnumConstructor<X extends Enum<X>> {

	public ExtendableEnum.EnumValue<X> newEnumValue(String name, Object[] parameters, String[] names, Class<?>[] types);
	
}
