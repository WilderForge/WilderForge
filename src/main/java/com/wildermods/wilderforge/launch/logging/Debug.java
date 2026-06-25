package com.wildermods.wilderforge.launch.logging;

import com.wildermods.provider.util.logging.ILogger;
import com.wildermods.provider.util.logging.LogLevel;
import com.wildermods.wilderforge.launch.WilderForge;

public class Debug extends Error {

	private Debug(String reason) {
		super(reason);
	}
	
	public static void trace() {
		trace(null);
	}
	
	public static void trace(String reason) {
		trace(WilderForge.LOGGER, reason);
	}
	
	public static void trace(ILogger logger, String reason) {
		if(reason == null || reason.isBlank()) {
			reason = "Tracing code path";
		}
		else {
			reason = "Tracing code path - " + reason;
		}
		logger.catching(LogLevel.INFO, new Debug(reason));
	}
	
}
