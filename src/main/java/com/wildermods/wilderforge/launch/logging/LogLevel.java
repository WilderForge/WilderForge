package com.wildermods.wilderforge.launch.logging;

import org.apache.logging.log4j.Level;

public enum LogLevel {

	TRACE(Level.TRACE),
	DEBUG(Level.DEBUG),
	INFO(Level.INFO),
	WARN(Level.WARN),
	ERROR(Level.ERROR),
	FATAL(Level.FATAL);
	
	private final Level log4jLevel;
	
	LogLevel(Level log4jLevel) {
		this.log4jLevel = log4jLevel;
	}
	
	public Level toLog4j() {
		return log4jLevel;
	}

	public static LogLevel getLevel(net.fabricmc.loader.impl.util.log.LogLevel level) {
		switch(level) {
		case TRACE:
			return TRACE;
		case DEBUG:
			return DEBUG;
		case INFO:
			return INFO;
		case WARN:
			return WARN;
		case ERROR:
			return ERROR;
		default:
			return INFO;
		}
	}
	
	public static LogLevel getLevel(org.apache.logging.log4j.Level level) {
		LogLevel logLevel = LogLevel.valueOf(level.name());
		if(logLevel == null) {
			logLevel = INFO;
		}
		return logLevel;
	}
	
	public static final LogLevel getLevel(int level) {
		switch (level) {
			case 0:
				return TRACE;
			case 1:
				return DEBUG;
			case 2:
				return INFO;
			case 3:
				return WARN;
			case 4:
				return ERROR;
			case 5:
				return FATAL;
			default:
				return INFO;
		}
	}
	
}
