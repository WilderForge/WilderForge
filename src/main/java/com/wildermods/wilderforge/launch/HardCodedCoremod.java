package com.wildermods.wilderforge.launch;

import java.io.IOException;

import com.wildermods.wilderforge.launch.exception.CoremodLinkageError;

public abstract class HardCodedCoremod extends LoadableCoremod {

	protected HardCodedCoremod() throws IOException, CoremodLinkageError {
		super();
	}

}
