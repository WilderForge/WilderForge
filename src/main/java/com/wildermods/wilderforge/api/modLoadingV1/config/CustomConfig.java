package com.wildermods.wilderforge.api.modLoadingV1.config;

import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Function;

import com.worldwalkergames.legacy.context.LegacyViewDependencies;
import com.worldwalkergames.legacy.ui.PopUp;

@Target(TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomConfig {
	public String modid();
	public Class<? extends Function<LegacyViewDependencies, ? extends PopUp>> popup();
}
