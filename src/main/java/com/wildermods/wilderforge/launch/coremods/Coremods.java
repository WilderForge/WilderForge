package com.wildermods.wilderforge.launch.coremods;

import com.wildermods.wilderforge.api.modLoadingV1.CoremodInfo;
import com.wildermods.wilderforge.api.modLoadingV1.MissingCoremod;
import com.wildermods.wilderforge.api.modLoadingV1.Mod;
import com.worldwalkergames.legacy.game.mods.ModInfo;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

public final class Coremods {

	private Coremods() {}
	
	public static CoremodInfo[] getAllCoremods() {
		ModContainer[] modContainers = FabricLoader.getInstance().getAllMods().toArray(new ModContainer[]{});
		CoremodInfo[] coremods = new CoremodInfo[modContainers.length];
		for(int i = 0; i < modContainers.length; i++) {
			coremods[i] = new CoremodInfo(modContainers[i]);
		}
		return coremods;
	}
	
	public static CoremodInfo getCoremod(String modid) {
		if(modid == null) {
			return new CoremodInfo(FabricLoader.getInstance().getModContainer("wildermyth").get()); //wildermyth's CORE modid is null
		}
		ModContainer modContainer = FabricLoader.getInstance().getModContainer(modid).orElse(null);
		return modContainer != null ? new CoremodInfo(modContainer) : new MissingCoremod();
	}
	
	public static CoremodInfo getCoremod(Mod mod) {
		return getCoremod(mod.modid());
	}
	
	public static CoremodInfo getCoremod(ModInfo mod) {
		return getCoremod(mod.modId);
	}
	
	public static int getCoremodCount() {
		return FabricLoader.getInstance().getAllMods().size();
	}
	
}
