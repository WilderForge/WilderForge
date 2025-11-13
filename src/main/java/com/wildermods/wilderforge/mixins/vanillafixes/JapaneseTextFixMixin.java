package com.wildermods.wilderforge.mixins.vanillafixes;

import org.spongepowered.asm.mixin.Mixin;

import com.badlogic.gdx.graphics.g2d.freetype.NiceFreeTypeBitmapFontData;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.wildermods.wilderforge.api.mixins.v1.Require;
import com.wildermods.wilderforge.api.modLoadingV1.Mod;

@Mixin(NiceFreeTypeBitmapFontData.class)
@Require(@Mod(modid = "wildermyth", version = "<1.16.560")) //patched in 1.16+560
public class JapaneseTextFixMixin {

	@WrapMethod(method = "isChinese")
	private static boolean checkIfHirigana(char c, Operation<Boolean> original) {
		if(original.call(c) == true) {
			return true; //already detected as non-breaking character, return true
		}
		return c >= 0x3040 && c <= 0x30A0; //if not detected as CJK, return true if hirigana
	}
	
	@WrapMethod(method = "isChinese")
	private static boolean checkIfKatakana(char c, Operation<Boolean> original) {
		if(original.call(c) == true) {
			return true; //already detected as a non-breaking character, return true
		}
		return c >= 0x30A0 && c <= 0x30FF; //if not detected as CJK, return true if katakana
	}
	
}
