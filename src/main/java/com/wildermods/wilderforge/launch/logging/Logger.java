package com.wildermods.wilderforge.launch.logging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.core.config.Configurator;

import net.fabricmc.loader.impl.util.log.LogCategory;

public class Logger implements ILogger {
	
	private final org.apache.logging.log4j.Logger logger;
	
	public Logger(Class clazz) {
		this(clazz.getSimpleName(), LogLevel.INFO);
	}
	
	public Logger(Class clazz, LogLevel minLevel) {
		this(clazz.getSimpleName(), minLevel);
	}
	
	public Logger(String name) {
		this(name, LogLevel.INFO);
	}
	
	public Logger(String name, LogLevel minLevel) {
		this.logger = LogManager.getLogger(name);
		Configurator.setLevel(logger, minLevel.toLog4j());
	}
	
	@Override
	public void log(LogLevel level, String s) {
		logger.log(level.toLog4j(), s);
	}
	
	public void log(LogLevel level, String s, String tag) {
		if(tag != null) {
			logger.log(level.toLog4j(), MarkerManager.getMarker(tag), s);
		}
		else {
			log(level, s);
		}
	}

	@Override
	public void catching(LogLevel level, Throwable t) {
		logger.catching(level.toLog4j(), t);
	}

	@Override
	public void catching(LogLevel level, Throwable t, String marker) {
		logger.log(level.toLog4j(), MarkerManager.getMarker(marker), t.getMessage(), t);
	}
	
	@Override
	public void log(long time, net.fabricmc.loader.impl.util.log.LogLevel level, LogCategory category, String msg, Throwable exc, boolean fromReplay, boolean wasSuppressed) {
		LogLevel l = LogLevel.getLevel(level);
		if(exc == null) {
			log(l, msg, category.name);
		}
		else {
			catching(l, exc);
		}
	}

	@Override
	public boolean shouldLog(LogLevel level) {
		return logger.getLevel().isInRange(LogLevel.FATAL.toLog4j(), level.toLog4j());
	}

}
