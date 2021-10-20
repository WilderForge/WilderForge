package com.wildermods.wilderforge.launch.steam;

import com.codedisaster.steamworks.SteamUtilsCallback;

public class SteamUtilityCallback implements SteamUtilsCallback {

	@Override
	public void onSteamShutdown() {
		System.out.println("Steam client shutdown");
	}

}
