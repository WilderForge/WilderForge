package com.wildermods.wilderforge.launch.logging;

import com.wildermods.wilderforge.launch.WilderForge;

public class Debug extends Error {

	private Debug(String reason) {
		super(reason);
	}
	
	public static void trace() {
		trace(null);
	}
	
	public static void trace(String reason) {
		if(reason == null || reason.isBlank()) {
			reason = "Tracing code path";
		}
		else {
			reason = "Tracing code path - " + reason;
		}
		WilderForge.LOGGER.catching(LogLevel.INFO, new Debug(reason));
	}
	
}
