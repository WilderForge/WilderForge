package com.wildermods.wilderforge.mixins.vanillafixes;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.ResourceBundle;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.worldwalkergames.legacy.server.context.ServerDataContext;

@Mixin(ServerDataContext.ModAwareResourceBundle.class)
public abstract class ServerDataContextVanillaFixMixin extends ResourceBundle {

	private @Shadow Map<String, Object> lookup;
	
	//Fixes https://github.com/WilderForge/WilderForge/issues/44
	@Inject(
		at = @At("HEAD"),
		method = "getKeys",
		cancellable = true
	)
	public void fixIllegalAccessError(CallbackInfoReturnable<Enumeration<String>> c) {
		ResourceBundle parent = this.parent;
		HashSet<String> keys = new HashSet<String>();

		
		if(lookup != null && lookup.keySet() != null) {
			keys.addAll(lookup.keySet());
		}
		
		if(parent != null && parent.getKeys() != null) {
			keys.addAll(Collections.list(parent.getKeys()));
		}
		
		c.setReturnValue(Collections.enumeration(keys));
	}
}
