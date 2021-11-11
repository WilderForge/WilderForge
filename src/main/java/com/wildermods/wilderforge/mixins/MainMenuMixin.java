package com.wildermods.wilderforge.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.wildermods.wilderforge.launch.LoadStatus;
import com.wildermods.wilderforge.launch.coremods.Coremods;
import com.wildermods.wilderforge.launch.ui.CoremodListPopup;
import com.worldwalkergames.legacy.context.LegacyViewDependencies;
import com.worldwalkergames.legacy.game.common.ui.OptionButton;
import com.worldwalkergames.legacy.ui.menu.IRootMenuNavigation;
import com.worldwalkergames.legacy.ui.menu.RootMenuPanel;

@Mixin(targets = "com.worldwalkergames.legacy.ui.menu.MainMenu", remap = false)
public abstract class MainMenuMixin extends RootMenuPanel {
	
	@Inject(
		at = @At(value = "INVOKE", target = 
			"Lcom/badlogic/gdx/scenes/scene2d/ui/Table;"
				+ "add("
					+ "Lcom/badlogic/gdx/scenes/scene2d/Actor;"
				+ ")"
			+ "Lcom/badlogic/gdx/scenes/scene2d/ui/Cell;",
			ordinal = 2
		),
		method = "build()V"
	)
	/*
	 * Adds coremod button to the main menu
	 */
	protected void build(CallbackInfo c) {
		this.stack.padTop(30);
		OptionButton.Factory buttonFactory = new OptionButton.Factory(dependencies, null, "mainMenuButton");
		OptionButton<Object> button = buttonFactory.ui(null, "wilderforge.mainMenu.coremods");
		button.clicked.add(this, () -> Coremods.getCoremodCountByStatus(LoadStatus.LOADED));
		this.stack.add(button).row();
	}
	
	
	/*
	 * Required compile-time constructor
	 */
	public MainMenuMixin(LegacyViewDependencies dependencies, IRootMenuNavigation navigation) {
		super(dependencies, navigation);
		throw new AssertionError("what the actual fuck this shouldn't ever get called");
	}
	
}
