package com.wildermods.wilderforge.mixins.integrations;

import org.spongepowered.asm.mixin.Mixin;

import com.worldwalkergames.legacy.integations.SteamManager;

@Mixin(SteamManager.class)
public abstract class SteamManagerMixin implements SteamManagerAccessor {

}
