package com.wildermods.wilderforge.launch.logging;

public enum LogLevel {

	TRACE,
	DEBUG,
	INFO,
	WARN,
	ERROR,
	FATAL;
	
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
