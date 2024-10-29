package com.wildermods.wilderforge.api.utils.vanillafixes;

import com.wildermods.wilderforge.api.mixins.v1.Cast;
import com.worldwalkergames.util.OSUtil;

public interface TranslateForShellStatus {

	public default boolean isWilderforgePatchingOpenBrowser() {
		return Cast.as(new OSUtil(), getClass()).isWilderforgePatchingOpenBrowser();
	}
	
	public default boolean isWilderforgePatchingShowFile() {
		return Cast.as(new OSUtil(), getClass()).isWilderforgePatchingShowFile();
	}
	
}
