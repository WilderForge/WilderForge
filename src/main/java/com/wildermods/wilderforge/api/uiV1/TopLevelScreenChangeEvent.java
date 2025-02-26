package com.wildermods.wilderforge.api.uiV1;

import com.wildermods.wilderforge.api.eventV2.Event;
import com.worldwalkergames.legacy.ui.MainScreen;
import com.worldwalkergames.legacy.ui.titlescreen.ITopLevelScreen;

public abstract class TopLevelScreenChangeEvent extends Event {

	protected final MainScreen mainScreen;
	protected final ITopLevelScreen prevScreen;
	protected ITopLevelScreen newScreen;
	
	public TopLevelScreenChangeEvent(boolean cancellable, MainScreen mainScreen, ITopLevelScreen prevScreen, ITopLevelScreen newScreen) {
		super(cancellable);
		this.mainScreen = mainScreen;
		this.prevScreen = prevScreen;
		this.newScreen = newScreen;
	}
	
	public MainScreen getMainScreen() {
		return mainScreen;
	}
	
	public ITopLevelScreen getPrevScreen() {
		return prevScreen;
	}
	
	public ITopLevelScreen getNewScreen() {
		return newScreen;
	}
	


	public static final class Pre extends TopLevelScreenChangeEvent {

		public Pre(MainScreen mainScreen, ITopLevelScreen prevScreen, ITopLevelScreen newScreen) {
			super(true, mainScreen, prevScreen, newScreen);
		}
		
		public void setNewScreen(ITopLevelScreen newScreen) {
			this.newScreen = newScreen;
		}
		
	}
	
	public static final class Post extends TopLevelScreenChangeEvent {

		public Post(MainScreen mainScreen, ITopLevelScreen prevScreen, ITopLevelScreen newScreen) {
			super(false, mainScreen, prevScreen, newScreen);
		}
		
	}
	
}
