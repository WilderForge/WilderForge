package com.wildermods.wilderforge.api.modLoadingV1.config;

import java.lang.annotation.Annotation;

import com.wildermods.wilderforge.api.eventV2.Event;
import com.wildermods.wilderforge.api.modLoadingV1.CoremodInfo;
import com.wildermods.wilderforge.api.modLoadingV1.config.ModConfigurationEntryBuilder.ConfigurationContext;

public class ConfigSavedEvent extends Event implements Config {

	public ConfigurationContext config;
	
	public ConfigSavedEvent(ConfigurationContext config) {
		super(false);
		this.config = config;
	}
	
	public CoremodInfo getCoremod() {
		return config.getCoremod();
	}
	
	public Object getConfiguration() {
		return config.configurationObj;
	}

	@Override
	public String modid() {
		return config.modid();
	}
	
	@Override
	public Class<? extends Annotation> annotationType() {
		return null;
	}
}
