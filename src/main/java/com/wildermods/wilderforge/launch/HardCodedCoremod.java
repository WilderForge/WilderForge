package com.wildermods.wilderforge.launch;

import java.io.IOException;

import com.google.gson.JsonObject;
import com.wildermods.wilderforge.launch.exception.CoremodLinkageError;

@InternalOnly
@SuppressWarnings("deprecation")
public abstract class HardCodedCoremod extends Coremod {

	protected HardCodedCoremod() throws IOException, CoremodLinkageError {
		super();
	}
	
	@Override
	public abstract JsonObject getModJson() throws IOException;

}
