package com.wildermods.wilderforge.api.uiV1;

import com.wildermods.wilderforge.api.eventV2.Event;
import com.worldwalkergames.legacy.context.LegacyViewDependencies;
import com.worldwalkergames.ui.NiceButtonBase;

public abstract class ButtonEvent extends Event {

	protected final LegacyViewDependencies dependencies;
	protected final NiceButtonBase button;
	
	public ButtonEvent(boolean cancellable, LegacyViewDependencies dependencies, NiceButtonBase button) {
		super(cancellable);
		this.dependencies = dependencies;
		this.button = button;
	}
	
	public LegacyViewDependencies getLegacyViewDependencies() {
		return dependencies;
	}
	
	public NiceButtonBase getButton() {
		return button;
	}
	
	public boolean isDisabled() {
		return button.isDisabled();
	}
	
	public static final class ButtonTryClickEvent extends ButtonEvent {

		public ButtonTryClickEvent(LegacyViewDependencies dependencies, NiceButtonBase button) {
			super(true, dependencies, button);
		}
		
	}
	
	public static final class ButtonClickEvent extends ButtonEvent {

		public ButtonClickEvent(LegacyViewDependencies dependencies, NiceButtonBase button) {
			super(false, dependencies, button);
		}
		
	}

}
