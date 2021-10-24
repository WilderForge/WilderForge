package com.wildermods.wilderforge.launch.steam;

import com.codedisaster.steamworks.SteamUtilsCallback;
import com.wildermods.wilderforge.launch.InternalOnly;

@InternalOnly
public class SteamUtilityCallback implements SteamUtilsCallback {
	
	@Override
	@InternalOnly
	public void onSteamShutdown() {
		System.out.println("Steam client shutdown");
	}

}
