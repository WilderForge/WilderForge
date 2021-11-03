package com.wildermods.wilderforge.launch;

import java.io.IOException;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.wildermods.wilderforge.api.versionV1.MultiVersionRange;

class Incompatability extends Coremod {
	private final JsonObject modJson;
	private final String incompatModId;
	private final MultiVersionRange incompatVersionRange;
	
	@InternalOnly
	public static Incompatability[] getIncompatabilities(JsonObject json) {
		JsonElement incompatJsonEle = json.get("incompatability");
		if(incompatJsonEle != null) {
			JsonArray array = json.get("incompatability").getAsJsonArray();
			Incompatability[] incompatabilities = new Incompatability[array.size()];
			for(int i = 0; i < array.size(); i++) {
				incompatabilities[i] = new Incompatability(json, array.get(i).getAsJsonObject());
			}
			return incompatabilities;
		}
		return new Incompatability[0];
	}
	
	private Incompatability(JsonObject json, JsonObject incompatability) {
		construct(json);
		modJson = json;
		incompatModId = incompatability.get("modid").getAsString();
		incompatVersionRange = new MultiVersionRange(incompatability.get("version").getAsString());
	}

	@Override
	@InternalOnly
	public JsonObject getModJson() throws IOException {
		return modJson;
	}
	
	public String getIncompatibleModId() {
		return incompatModId;
	}
	
	public MultiVersionRange getIncompatRange() {
		return incompatVersionRange;
	}
	
	public boolean possiblyIncompatible(Coremod coremod) {
		return coremod.value().equals(incompatModId);
	}
	
	public boolean isIncompatible(Coremod coremod) {
		return incompatVersionRange.isWithinRange(coremod.version);
	}

}
