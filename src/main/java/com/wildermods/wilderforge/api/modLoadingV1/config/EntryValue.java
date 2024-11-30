package com.wildermods.wilderforge.api.modLoadingV1.config;

import java.util.function.BiFunction;

import com.wildermods.wilderforge.api.modLoadingV1.config.ModConfigurationEntryBuilder.ConfigurationUIEntryContext;

public interface EntryValue<T> {
	public BiFunction<ConfigurationUIEntryContext, T, String> getStringConverter();
	
	public default String convertToString(ConfigurationUIEntryContext obj, T val) throws Throwable {
		return getStringConverter().apply(obj, val);
	}
	
}
