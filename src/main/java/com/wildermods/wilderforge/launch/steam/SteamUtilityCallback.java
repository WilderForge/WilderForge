package com.wildermods.wilderforge.launch.steam;

import com.codedisaster.steamworks.SteamUtilsCallback;
import com.wildermods.provider.util.logging.Logger;
import com.wildermods.wilderforge.launch.InternalOnly;

@InternalOnly
public class SteamUtilityCallback implements SteamUtilsCallback {
	
	Logger LOGGER = new Logger(SteamUtilityCallback.class);
	
	@Override
	@InternalOnly
	public void onSteamShutdown() {
		LOGGER.log("Steam client shutdown");
	}

}
