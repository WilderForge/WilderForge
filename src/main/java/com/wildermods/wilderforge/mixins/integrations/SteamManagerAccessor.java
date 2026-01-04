package com.wildermods.wilderforge.mixins.integrations;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.codedisaster.steamworks.SteamFriends;
import com.codedisaster.steamworks.SteamUGC;
import com.codedisaster.steamworks.SteamUser;
import com.codedisaster.steamworks.SteamUserStats;
import com.codedisaster.steamworks.SteamUtils;
import com.codedisaster.steamworks.SteamUtilsCallback;
import com.worldwalkergames.legacy.integations.SteamManager;

@Mixin(SteamManager.class)
public interface SteamManagerAccessor {

	public @Accessor SteamUserStats getSteamUserStats();
	public @Accessor SteamUGC getSteamUGC();
	public @Accessor SteamUser getSteamUser();
	public @Accessor SteamFriends getSteamFriends();
	public @Accessor SteamUtils getSteamUtils();
	public @Accessor SteamUtilsCallback getSteamUtilsCallback();
	
}
