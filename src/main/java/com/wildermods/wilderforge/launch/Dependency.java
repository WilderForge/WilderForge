package com.wildermods.wilderforge.launch;

import java.io.IOException;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import static com.wildermods.wilderforge.api.modJsonV1.ModJsonConstants.*;
import com.wildermods.wilderforge.api.versionV1.MultiVersionRange;
import com.wildermods.wilderforge.launch.exception.CoremodFormatError;

@SuppressWarnings("deprecation")
public class Dependency extends Coremod {

	private final JsonObject modJson;
	private final boolean required;
	private final MultiVersionRange versionRange;
	
	public Dependency(boolean required, JsonObject json) {
		this.required = required;
		this.modJson = json;
		JsonElement modid = json.get(MODID);
		JsonElement versionRange = json.get(VERSION);
		if(modid != null) {
			this.modid = modid.getAsString();
		}
		else {
			throw new CoremodFormatError("No modid detected for a dependency");
		}
		if(versionRange != null) {
			this.versionRange = new MultiVersionRange(versionRange.getAsString());
		}
		else {
			throw new CoremodFormatError("No version detected for dependency" + this.modid);
		}
	}
	
	public MultiVersionRange getVersionRange() {
		return versionRange;
	}
	
	public boolean required() {
		return required;
	}

	@Override
	public JsonObject getModJson() throws IOException {
		return modJson;
	}
	
}
