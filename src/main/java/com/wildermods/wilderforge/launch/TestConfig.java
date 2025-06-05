package com.wildermods.wilderforge.launch;

import com.wildermods.wilderforge.api.modLoadingV1.config.Config;
import com.wildermods.wilderforge.api.modLoadingV1.config.ConfigEntry.Range;

@Config(modid = "wilderforge")
public class TestConfig {

	public boolean test = false;
	
	public String test2 = "Test String";
	
	@Range(min = 0, max = 10)
	public int test3 = 5;
	
}
