package com.wildermods.wilderforge.launch.coremods;

import java.io.IOException;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import static com.wildermods.wilderforge.api.modJsonV1.ModJsonConstants.*;
import com.wildermods.wilderforge.api.versionV1.MultiVersionRange;
import com.wildermods.wilderforge.launch.InternalOnly;

@InternalOnly
public class Incompatability extends Coremod {
	private final JsonObject modJson;
	private final String incompatModId;
	private final MultiVersionRange incompatVersionRange;
	
	@InternalOnly
	public static Incompatability[] getIncompatabilities(JsonObject json) {
		JsonElement incompatJsonEle = json.get(INCOMPATIBLE);
		if(incompatJsonEle != null) {
			JsonArray array = json.get(INCOMPATIBLE).getAsJsonArray();
			Incompatability[] incompatabilities = new Incompatability[array.size()];
			for(int i = 0; i < array.size(); i++) {
				incompatabilities[i] = new Incompatability(json, array.get(i).getAsJsonObject());
			}
			return incompatabilities;
		}
		return new Incompatability[0];
	}
	
	private Incompatability(JsonObject json, JsonObject incompatability) {
		modJson = json;
		construct(json);
		incompatModId = incompatability.get(MODID).getAsString();
		incompatVersionRange = new MultiVersionRange(incompatability.get(VERSION).getAsString());
		
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
