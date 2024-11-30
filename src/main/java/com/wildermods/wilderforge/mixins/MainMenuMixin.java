package com.wildermods.wilderforge.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.wildermods.wilderforge.api.mixins.v1.Descriptor.*;

import com.wildermods.wilderforge.api.mixins.v1.Impossible;
import com.wildermods.wilderforge.launch.ui.CoremodListPopup;
import com.worldwalkergames.legacy.context.LegacyViewDependencies;
import com.worldwalkergames.legacy.game.common.ui.OptionButton;
import com.worldwalkergames.legacy.ui.menu.IRootMenuNavigation;
import com.worldwalkergames.legacy.ui.menu.MainMenu;
import com.worldwalkergames.legacy.ui.menu.RootMenuPanel;

@Mixin(value = MainMenu.class, remap = false)
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
		method = "build()" + VOID
	)
	/*
	 * Adds coremod button to the main menu
	 */
	protected void addCoremodButton(CallbackInfo c) {
		this.stack.padTop(30);
		OptionButton.Factory buttonFactory = new OptionButton.Factory(dependencies, null, "mainMenuButton");
		OptionButton<Object> button = buttonFactory.ui(null, "wilderforge.mainMenu.coremods");
		button.clicked.add(this, () -> dependencies.popUpManager.pushFront(new CoremodListPopup(dependencies), false));
		this.stack.add(button).row();
	}
	
	
	/*
	 * Required compile-time constructor
	 */
	public MainMenuMixin(LegacyViewDependencies dependencies, IRootMenuNavigation navigation) {
		super(dependencies, navigation);
		Impossible.error();
	}
	
}
