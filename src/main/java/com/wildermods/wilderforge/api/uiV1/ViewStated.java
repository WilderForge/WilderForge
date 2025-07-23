package com.wildermods.wilderforge.api.uiV1;

import com.wildermods.wilderforge.api.modLoadingV1.CoremodAware;
import com.worldwalkergames.legacy.control.ClientContext.ViewState;

public interface ViewStated extends CoremodAware {

	public ViewState getViewState();
	
}
