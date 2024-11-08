package com.wildermods.wilderforge.mixins.mechanics;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;

import com.wildermods.wilderforge.api.mechanicsV1.ChapterSetEvent;
import com.wildermods.wilderforge.api.mixins.v1.Cast;
import com.wildermods.wilderforge.launch.WilderForge;
import com.worldwalkergames.legacy.game.campaign.model.GameSettings;

@Mixin(GameSettings.class)
public class GameSettingsMixin {

	private @Shadow int chapterNumber;
	
	@Inject(
		method = "setChapterNumber",
		at = @At("HEAD"),
		cancellable = true
	)
	public void onChapterChange(int chapterNumber, CallbackInfo c) {
		ChapterSetEvent e = new ChapterSetEvent(Cast.from(this), chapterNumber);
		WilderForge.MAIN_BUS.fire(e);
		this.chapterNumber = e.getNewChapter();
		c.cancel(); //don't continue with vanilla implementation
	}
	
}
