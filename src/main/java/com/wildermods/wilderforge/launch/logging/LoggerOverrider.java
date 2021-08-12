package com.wildermods.wilderforge.launch.logging;

import java.util.LinkedHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.MessageFormatMessageFactory;
import org.apache.logging.log4j.Level;
import static org.apache.logging.log4j.Level.*;

import com.worldwalkergames.logging.FilteringConsumer;

public class LoggerOverrider extends FilteringConsumer implements ApplicationLogger {
	
	public static final LinkedHashMap<String, Logger> LOGGERS = new LinkedHashMap<String, Logger>();
	
	public LoggerOverrider(Filter... filters) {
		super(filters);
	}
	
	@Override
	protected void log(String[] tags, int level, long time, String message, Object... parameters) {
		String tag = tags[0];
		if(tag == null) {
			tag = "UNKNOWN-WILDERFORGE";
		}
		getLogger(tag).log(getLevel(level), message, parameters);
	}
	
	private Logger getLogger(String name) { 
		Logger ret;
		if((ret = LOGGERS.get(name)) != null) {
			return ret;
		}
		ret = LogManager.getLogger(name, new MessageFormatMessageFactory());
		LOGGERS.putIfAbsent(name, ret);
		return ret;
	}
	
	private final Level getLevel(int level) {
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
