package com.wildermods.wilderforge.launch.resources;

import java.util.Locale;

import com.wildermods.wilderforge.api.modLoadingV1.CoremodInfo;
import com.wildermods.wilderforge.launch.InternalOnly;

@InternalOnly
public interface CoremodCompatibleResourceBundle {

	@InternalOnly
	public void addResources(CoremodInfo coremod, String assetPath, Locale locale);
	
}
