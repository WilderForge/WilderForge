package com.wildermods.wilderforge.mixins.vanillafixes;

import org.spongepowered.asm.mixin.Mixin;

import com.badlogic.gdx.graphics.g2d.freetype.NiceFreeTypeBitmapFontData;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

@Mixin(NiceFreeTypeBitmapFontData.class)
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
