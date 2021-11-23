package com.wildermods.wilderforge.api.uiV1;

import com.worldwalkergames.legacy.ui.component.TextLayout;

public interface IStyle {

	public Float padLeft();
	public Float padRight();
	public Float padTop();
	public float padBottom();
	public boolean highlightOnOver();
	public TextLayout.Style textStyle();
	
}
