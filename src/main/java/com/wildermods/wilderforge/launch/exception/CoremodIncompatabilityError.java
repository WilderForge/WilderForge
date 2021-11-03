package com.wildermods.wilderforge.launch.exception;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import static com.wildermods.wilderforge.api.modJsonV1.ModJsonConstants.*;
import com.wildermods.wilderforge.api.versionV1.MultiVersionRange;
import com.wildermods.wilderforge.api.versionV1.MultiVersionRange.VersionRange;
import com.wildermods.wilderforge.launch.Coremod;
import com.wildermods.wilderforge.launch.Incompatability;

public class CoremodIncompatabilityError extends CoremodLinkageError {

	public CoremodIncompatabilityError(Incompatability incompatability, Coremod incompatibleMod) {
		super("'" + incompatability.value() + "' is marked as incompatible with '" + getIncompatabilityRange(incompatability, incompatibleMod) + " - '" + incompatibleMod + "' is on version '" + incompatibleMod.getVersion() +"'");
	}
	
	private static String getIncompatabilityRange(Coremod incompatability, Coremod incompatibleMod) {
		try {
			VersionRange versionRange;
			JsonObject incompatJObj;
			for(JsonElement incompat : incompatability.getModJson().get(INCOMPATIBLE).getAsJsonArray()) {
				JsonObject incompatJEntry = incompat.getAsJsonObject();
				if(incompatJEntry.get(MODID).getAsString().equals(incompatibleMod.getModId())) {
					return incompatibleMod.value()  + " " + new MultiVersionRange(incompatJEntry.get(VERSION).getAsString());
				}
			}
			throw new AssertionError("Did not find version but it was defined earlier?");
		}
		catch(Throwable t) {
			throw new CoremodFormatError(t);
		}
	}
	
}
