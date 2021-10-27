package com.wildermods.wilderforge.api.modLoadingV1;

import com.worldwalkergames.legacy.game.mods.ModInfo;
import com.worldwalkergames.legacy.server.context.ServerDataContext;
import com.worldwalkergames.legacy.server.context.ServerDataContext.ModLocation;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Arrays;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wildermods.wilderforge.launch.Coremod;
import com.wildermods.wilderforge.launch.Coremods;
import com.wildermods.wilderforge.launch.InternalOnly;
import com.wildermods.wilderforge.launch.LoadStatus;
import com.wildermods.wilderforge.launch.Main;
import com.wildermods.wilderforge.launch.WilderForge;
import com.wildermods.wilderforge.launch.exception.CoremodLinkageError;

public class CoremodInfo extends ModInfo implements com.wildermods.wilderforge.api.modLoadingV1.Coremod {

	public final Coremod coremod;
	
	@InternalOnly
	public CoremodInfo(Coremod coremod) {
		JsonObject json;
		try {
			json = coremod.getModJson();
		} catch (IOException e) {
			throw new CoremodLinkageError(e);
		}
		this.coremod = coremod;
		super.modId = coremod.value();
		if(Coremods.getStatus(coremod) == LoadStatus.LOADED) {
			modLocation = ServerDataContext.ModLocation.core;
		}
		else {
			modLocation = ServerDataContext.ModLocation.missing;
		}
		modSteamWorkshopId = null;
		alwaysOn = true;
		showInStoryDialog = true;
		showInModConfig = true;
		listInCredits = true;
		JsonElement credits = json.get("credits");
		customCreditLines = credits != null ? WilderForge.gson.fromJson(json.get("credits"), String[].class) : null;
		Main.LOGGER.info(Arrays.toString(customCreditLines));
		name = coremod.getName();
		modLocation = ModLocation.core;
	}


	@Override
	public Class<? extends Annotation> annotationType() {
		return com.wildermods.wilderforge.api.modLoadingV1.Coremod.class;
	}


	@Override
	public String value() {
		return modId;
	}
	
}
