package com.wildermods.wilderforge.launch.services;

import org.spongepowered.asm.launch.platform.container.ContainerHandleModLauncher;
import org.spongepowered.asm.service.modlauncher.MixinServiceModLauncher;

public class WilderMixinServiceModLauncher extends MixinServiceModLauncher {

	@Override
	public ContainerHandleModLauncher getPrimaryContainer() {
		return new WilderHandleModLauncher(this.getName());
	}
	
	private static final class WilderHandleModLauncher extends ContainerHandleModLauncher {

		public WilderHandleModLauncher(String name) {
			super(name);
		}
		
	}
	
}