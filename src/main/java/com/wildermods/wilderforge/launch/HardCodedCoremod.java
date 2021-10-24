package com.wildermods.wilderforge.launch;

import java.io.IOException;

import com.wildermods.wilderforge.launch.exception.CoremodLinkageError;

@InternalOnly
public abstract class HardCodedCoremod extends Coremod {

	protected HardCodedCoremod() throws IOException, CoremodLinkageError {
		super();
	}

}
