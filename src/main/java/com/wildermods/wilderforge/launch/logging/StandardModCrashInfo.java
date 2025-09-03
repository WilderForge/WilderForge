package com.wildermods.wilderforge.launch.logging;

import com.badlogic.gdx.utils.Array;
import com.wildermods.wilderforge.api.modLoadingV1.MissingCoremod;
import com.wildermods.wilderforge.launch.WilderForge;
import com.wildermods.wilderforge.launch.coremods.Coremods;
import com.worldwalkergames.legacy.context.LegacyViewDependencies;
import com.worldwalkergames.legacy.game.campaign.model.GameSettings.ModEntry;
import com.worldwalkergames.legacy.game.mods.ModInfo;

public class StandardModCrashInfo {

	public String toString() {
		
		StringBuilder s = new StringBuilder();
		
		LegacyViewDependencies dependencies = WilderForge.getViewDependencies();
		if(dependencies != null) {
			Array<ModEntry> activeMods = dependencies.dataContext.getActiveMods();
			Array<ModInfo> standardMods = dependencies.dataContext.retrieveAllMods(false, false);
			
			int standardModCount = standardMods.size - Coremods.getCoremodCount();
			int activeModCount = activeMods.size - Coremods.getCoremodCount();
			int inactiveModCount = standardModCount - activeModCount;
			
			s.append('\n');
			s.append("Standard Mods Detected: " + standardModCount + ". " + activeModCount + "/" + standardModCount + " active");
			s.append('\n');
			for(ModInfo mod : standardMods) {
				if(Coremods.getCoremod(mod) instanceof MissingCoremod) {
					String modid = mod.modId;
					String modid2 = modid;
					float loadOrder = mod.loadOrder;
					s.append('\t');
					Iterable<ModEntry> modEntry = activeMods.select((entry) -> {
						return entry.modId.equals(modid);
					});
					if(modEntry.iterator().hasNext()) {
						loadOrder = modEntry.iterator().next().getLoadNumber();
						s.append("active");
					}
					else {
						s.append("inactive");
					}
					
					try {
						Long.parseLong(modid); //if no exception, then this is a numerical id
						if(mod.url != null) {
							if(mod.url.startsWith("https://wildermyth.com/wiki/")) {
								modid2 = mod.url.replace("https://wildermyth.com/wiki/", "");
							}
							if(modid2.startsWith("index.php?title=")) {
								modid2 = modid2.replace("index.php?title=", ""); //really old standard mods directed to index.php
							}
						}
					}
					catch(NumberFormatException e) {} //swallow
					
					s.append(" ");
					s.append(modid2 + " [version " + mod.modVersion + "." + mod.modVersionMinor + "] [order " + loadOrder + "]");
					s.append('\n');
				}
			}
		}
		
		return s.toString();
	}
	
}
