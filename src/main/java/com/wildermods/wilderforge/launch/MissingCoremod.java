package com.wildermods.wilderforge.launch;

import java.io.IOException;

import com.google.gson.JsonObject;
import com.wildermods.wilderforge.api.versionV1.Version;

public class MissingCoremod extends Coremod {

	MissingCoremod(String modid) {
		construct(modid, modid, Version.MISSING);
	}
	
	@Override
	public @InternalOnly JsonObject getModJson() throws IOException {
		return new JsonObject();
	}

}
