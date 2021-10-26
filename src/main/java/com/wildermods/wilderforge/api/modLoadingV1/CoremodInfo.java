package com.wildermods.wilderforge.api.modLoadingV1;

import com.worldwalkergames.legacy.game.mods.ModInfo;
import com.worldwalkergames.legacy.server.context.ServerDataContext;
import com.worldwalkergames.legacy.server.context.ServerDataContext.ModLocation;

import java.lang.annotation.Annotation;

import com.wildermods.wilderforge.launch.Coremod;
import com.wildermods.wilderforge.launch.Coremods;
import com.wildermods.wilderforge.launch.LoadStatus;

public class CoremodInfo extends ModInfo implements com.wildermods.wilderforge.api.modLoadingV1.Coremod {

	public final Coremod coremod;
	
	
	public CoremodInfo(Coremod coremod) {
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
		customCreditLines = new String[]{};
		name = modId;
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
