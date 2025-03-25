package com.wildermods.wilderforge.api.uiV1;

import com.wildermods.wilderforge.api.eventV2.Event;
import com.worldwalkergames.ui.popup.IPopUp;

public class PopUpEvent extends Event {

	private final IPopUp popup;
	
	public PopUpEvent(boolean cancellable, IPopUp popup) {
		super(cancellable);
		this.popup = popup;
	}

	public IPopUp getPopup() {
		return popup;
	}
	
	public static class PopUpRemoveEvent extends PopUpEvent {

		public PopUpRemoveEvent(boolean cancellable, IPopUp popup) {
			super(cancellable, popup);
		}
		
		public static class Pre extends PopUpRemoveEvent {

			public Pre(IPopUp popup) {
				super(true, popup);
			}
			
		}
		
		public static class Post extends PopUpRemoveEvent {
			
			public Post(IPopUp popup) {
				super(false, popup);
			}
			
		}
		
	}
	
	public static class PopUpAddEvent extends PopUpEvent {
		
		public static interface Pre {
			public void setPopUp(IPopUp popup);
		}
		
		public static interface Post {}
		
		protected IPopUp popup;
		
		public PopUpAddEvent(boolean cancellable, IPopUp popup) {
			super(cancellable, popup);
		}
		
		public static class PushFrontEvent extends PopUpAddEvent {

			protected boolean skipFadeIn;
			
			public PushFrontEvent(boolean cancellable, IPopUp popup, boolean skipFadeIn) {
				super(cancellable, popup);
				this.skipFadeIn = skipFadeIn;
			}
			
			public final boolean isSkipFadeIn() {
				return skipFadeIn;
			}
			
			public static class Pre extends PushFrontEvent implements PopUpAddEvent.Pre {

				public Pre(IPopUp popup, boolean skipFadeIn) {
					super(true, popup, skipFadeIn);
				}
				
				public void setSkipFadeIn(boolean skipFadeIn) {
					this.skipFadeIn = skipFadeIn;
				}

				@Override
				public void setPopUp(IPopUp popup) {
					this.popup = popup;
				}
				
			}
			
			public static class Post extends PushFrontEvent implements PopUpAddEvent.Post {

				public Post(IPopUp popup, boolean skipFadeIn) {
					super(false, popup, skipFadeIn);
				}
				
			}
			
		}
		
		public static class PushBackEvent extends PopUpAddEvent {

			public PushBackEvent(boolean cancellable, IPopUp popup) {
				super(cancellable, popup);
			}
			
			public static class Pre extends PushBackEvent implements PopUpAddEvent.Pre {

				public Pre(IPopUp popup) {
					super(true, popup);
				}

				@Override
				public void setPopUp(IPopUp popup) {
					this.popup = popup;
				}
				
			}
			
			public static class Post extends PushBackEvent implements PopUpAddEvent.Post {

				public Post(IPopUp popup) {
					super(false, popup);
				}
				
			}
			
		}
		
	}
	
}
