package com.wildermods.wilderforge.mixins;

import java.lang.annotation.Annotation;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.wildermods.wilderforge.api.mixins.v1.Cast;
import com.wildermods.wilderforge.api.mixins.v1.Impossible;
import static com.wildermods.wilderforge.api.mixins.v1.Initializer.*;
import static com.wildermods.wilderforge.api.mixins.v1.Descriptor.*;
import com.wildermods.wilderforge.api.uiV1.ViewStated;

import com.worldwalkergames.legacy.control.ClientContext.ViewState;

@Mixin(ViewState.class)
public class ViewStateEnumExtensionMixin implements ViewStated {

	static {
		ViewState.values();
	}
	
	private static @Shadow @Final @Mutable ViewState[] $VALUES;
	
	private @Final @Unique String modid;
	
	@Invoker(CONSTRUCTOR)
	private static ViewState newViewState(String internalName, int internalID) {
		return Impossible.error();
	}
	
	@Inject(
		method = STATIC_INIT,
		at = @At(
			value = "FIELD",
			opcode = Opcodes.PUTSTATIC,
			target = "Lcom/worldwalkergames/legacy/control/ClientContext$ViewState;"
					+ ENUM_VALUES
					+ ARRAY_OF + "Lcom/worldwalkergames/legacy/control/ClientContext$ViewState;"
		)
		
	)
	private static void addCustomViewStates(CallbackInfo ci) {
		
	}

	@Override
	public String modid() {
		return modid;
	}

	@Override
	public ViewState getViewState() {
		return Cast.from(this);
	}
	
	@Override
	public Class<? extends Annotation> annotationType() {
		return null;
	}
	
}
