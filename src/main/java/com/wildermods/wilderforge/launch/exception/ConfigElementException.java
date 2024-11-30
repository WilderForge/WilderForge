package com.wildermods.wilderforge.launch.exception;

import java.lang.reflect.Field;

import com.wildermods.wilderforge.api.modLoadingV1.config.Config;

public class ConfigElementException extends Exception {

	public ConfigElementException(Config config, Field field, Object configurationObj, String reason) {
		super("Unable to construct configuration element for field " + field.getName() + " in config for mod " + config.modid() + ". " + reason);
	}
	
	public ConfigElementException(Config config, Field field, Object configurationObj, Throwable cause) {
		super("Unable to construct configuration element for field " + field.getName() + " in config for mod " + config.modid() + ".", cause);
	}
	
}
