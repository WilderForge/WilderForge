package com.wildermods.wilderforge.mixins;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.badlogic.gdx.files.FileHandle;
import com.wildermods.wilderforge.api.modLoadingV1.CoremodInfo;
import com.wildermods.wilderforge.launch.InternalOnly;
import com.wildermods.wilderforge.launch.Main;
import com.wildermods.wilderforge.launch.coremods.Coremods;
import com.wildermods.wilderforge.launch.resources.CoremodCompatibleResourceBundle;

import com.worldwalkergames.legacy.server.context.ServerDataContext;
import com.worldwalkergames.legacy.server.context.ServerDataContext.ModAwareResourceBundle;

@Mixin(value = ModAwareResourceBundle.class, remap = false)
public class ModAwareResourceBundleMixin implements CoremodCompatibleResourceBundle {

	private @Shadow Map<String, Object> lookup; 
	public @Shadow HashMap<String, HashMap<String, Object>> contentByMod;
	
	
	@Inject(at = @At("RETURN"), method = 
			"<init>("
				+ "Lcom/worldwalkergames/legacy/server/context/ServerDataContext;"
				+ "Ljava/lang/String;"
				+ "Lcom/badlogic/gdx/files/FileHandle;"
				+ "Z"
				+ "Ljava/util/Locale;"
			+ ")V",
		require = 1)
	public void constructor(ServerDataContext parentObj, String assetPath, FileHandle bundleForDependencies, boolean fromAllMods, Locale locale, CallbackInfo c) {
		Main.LOGGER.debug("ASSET PATH: " + assetPath);
		for(CoremodInfo coremod : Coremods.getAllCoremods()) {
			addResources(coremod, assetPath, locale);
		}
	}
	
	@Unique
	@Override
	@InternalOnly
	public void addResources(CoremodInfo coremod, String assetPath, Locale locale) {
		String newAssetPath = "assets/" + coremod.modId + "/" + assetPath.replace("assets/", "");
		System.out.println("Loading resources for coremod " + coremod + " in " + newAssetPath);
		ResourceBundle resources = coremod.getResourceBundle(newAssetPath, locale);
		HashMap<String, Object> coremodValues = new HashMap<String, Object>();
		
		if(resources != null) {
			Iterator<String> keys = resources.getKeys().asIterator();
			while(keys.hasNext()) {
				String key = keys.next();
				System.out.println(key);
				Object value = resources.getObject(key);
				lookup.put(key, value);
				coremodValues.put(key, value);
			}
			this.contentByMod.put(coremod.modId, coremodValues);
		}
		else {
			Main.LOGGER.info("Coremod " + coremod + " has no resources");
		}
	}
	
}
