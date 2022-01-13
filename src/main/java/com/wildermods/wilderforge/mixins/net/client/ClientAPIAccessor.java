package com.wildermods.wilderforge.mixins.net.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import com.worldwalkergames.legacy.game.api.ClientAPI;

@Mixin(ClientAPI.class)
public interface ClientAPIAccessor {
	
	public @Accessor("isWaiting") boolean isWaiting();
	public @Accessor("isWaiting") void setWaiting(boolean waitingState);
	public @Invoker void callServerRejectedAction();
	
}
