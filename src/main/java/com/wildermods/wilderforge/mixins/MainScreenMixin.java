package com.wildermods.wilderforge.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.wildermods.wilderforge.api.mixins.v1.Cast;
import com.wildermods.wilderforge.api.mixins.v1.Impossible;
import com.wildermods.wilderforge.api.uiV1.TopLevelScreenChangeEvent;
import com.wildermods.wilderforge.launch.WilderForge;
import com.worldwalkergames.legacy.context.ClientDataContext;
import com.worldwalkergames.legacy.control.ClientContext;
import com.worldwalkergames.legacy.ui.LegacyUIRoot;
import com.worldwalkergames.legacy.ui.MainScreen;
import com.worldwalkergames.legacy.ui.titlescreen.ITopLevelScreen;

@Mixin(MainScreen.class)
public abstract class MainScreenMixin extends LegacyUIRoot {

	protected @Shadow ITopLevelScreen content; 
	
	@WrapMethod(method = "setContent")
	private void onTopScreenChange(ITopLevelScreen newScreen, Operation<Void> original) {
		TopLevelScreenChangeEvent.Pre e = new TopLevelScreenChangeEvent.Pre(Cast.from(this), content, newScreen);
		if(WilderForge.MAIN_BUS.fire(e)) {
			return;
		}
		original.call(e.getNewScreen());
		WilderForge.MAIN_BUS.fire(new TopLevelScreenChangeEvent.Post(Cast.from(this), content, newScreen));
	}
	
	public MainScreenMixin(ClientDataContext dataContext, ClientContext control) {
		super(dataContext, control);
		Impossible.error();
	}

}
