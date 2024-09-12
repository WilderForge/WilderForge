package com.wildermods.wilderforge.mixins.net.server;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;

import com.wildermods.wilderforge.api.mixins.v1.Impossible;
import com.wildermods.wilderforge.api.netV1.server.ServerBirthEvent;
import com.wildermods.wilderforge.api.netV1.server.ServerDeathEvent;
import com.wildermods.wilderforge.launch.WilderForge;
import com.worldwalkergames.legacy.context.ClientDataContext;
import com.worldwalkergames.legacy.control.ClientContext;
import com.worldwalkergames.legacy.control.ClientControl;

@Mixin(ClientControl.class)
public abstract class HostEventsMixin extends ClientContext {
	
	@Inject(
		at = @At("RETURN"),
		method = "connectAndLogin",
		cancellable = false
	)
	public void onServerInitialize(CallbackInfo c) {
		if(selectedHost != null) {
			if(selectedHost.local) {
				WilderForge.MAIN_BUS.fire(new ServerBirthEvent(this.selectedHost));
			}
		}
	}
	
	@Inject(
		at = @At(
			value = "INVOKE",
			target = "dispose"
		),
		method = "disconnect"
	)
	public void onShutDownPost(CallbackInfo c) {
		WilderForge.MAIN_BUS.fire(new ServerDeathEvent(this.selectedHost));
	}
	
	public HostEventsMixin(ClientDataContext dataContext) {
		super(Impossible.error());
	}

}
