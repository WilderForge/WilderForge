package com.wildermods.wilderforge.launch.logging;

import java.util.LinkedHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.MessageFormatMessageFactory;
import org.apache.logging.log4j.Level;
import static org.apache.logging.log4j.Level.*;

import com.badlogic.gdx.ApplicationLogger;
import com.codedisaster.steamworks.SteamAPIWarningMessageHook;
import com.worldwalkergames.logging.ALogger;
import com.worldwalkergames.logging.FilteringConsumer;

public class LoggerOverrider extends FilteringConsumer implements ApplicationLogger, SteamAPIWarningMessageHook {
	
	public static final LinkedHashMap<String, Logger> LOGGERS = new LinkedHashMap<String, Logger>();
	
	public LoggerOverrider(Filter... filters) {
		super(filters);
	}
	
	@Override
	protected void log(String[] tags, int level, long time, String message, Object[] parameters) {
		String tag = tags[0];
		if(tag == null) {
			tag = "UNKNOWN-WILDERFORGE";
		}
		getLogger(tag).log(getLevel(level), ALogger.compile(message, parameters));
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

	@Override
	public void log(String tag, String message) {
		log(new String[] {tag}, 2, 0, message, new Object[0]);
	}

	@Override
	public void log(String tag, String message, Throwable exception) {
		getLogger(tag).catching(INFO, exception);
	}

	@Override
	public void error(String tag, String message) {
		log(new String[] {tag}, 4, 0, message, new Object[0]);
	}

	@Override
	public void error(String tag, String message, Throwable exception) {
		getLogger(tag).catching(ERROR, exception);
	}

	@Override
	public void debug(String tag, String message) {
		log(new String[] {tag}, 1, 0, message, new Object[0]);
	}

	@Override
	public void debug(String tag, String message, Throwable exception) {
		getLogger(tag).catching(DEBUG, exception);
	}

	/**
	 * Fires when a steam api warning message occurs
	 */
	@Override
	public void onWarningMessage(int severity, String message) {
		getLogger("Steam").log(getLevel(severity), message.substring(message.lastIndexOf(']')));
	}
	
}
