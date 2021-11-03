package com.wildermods.wilderforge.launch.exception;

import com.wildermods.wilderforge.api.versionV1.Version;
import com.wildermods.wilderforge.launch.Coremod;

public class CoremodIncompatabilityError extends CoremodLinkageError {

	public CoremodIncompatabilityError(Coremod declarer, Coremod incompatability) {
		super(declarer.value() + " is incompatible with " + incompatability.value() + incompatability.getVersion());
	}
	
	private static Version getIncompatabilityRange(Coremod declarer, Coremod incompatability) {
		try {
			return Version.getVersion(declarer.getModJson().get("incompatible").getAsJsonObject().get(incompatability.value()).getAsString());
		}
		catch(Throwable t) {
			throw new CoremodFormatError(t);
		}
	}
	
}
