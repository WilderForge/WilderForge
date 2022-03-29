package com.wildermods.wilderforge.launch.logging;

import java.util.LinkedHashMap;

import static com.wildermods.wilderforge.launch.logging.LogLevel.*;

import com.badlogic.gdx.ApplicationLogger;
import com.codedisaster.steamworks.SteamAPIWarningMessageHook;
import com.wildermods.wilderforge.launch.InternalOnly;
import com.worldwalkergames.logging.ALogger;
import com.worldwalkergames.logging.FilteringConsumer;

@InternalOnly
public class LoggerOverrider extends FilteringConsumer implements ApplicationLogger, SteamAPIWarningMessageHook {
	
	public static final LinkedHashMap<String, Logger> LOGGERS = new LinkedHashMap<String, Logger>();
	
	@InternalOnly
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
		ret = new Logger(name);
		LOGGERS.putIfAbsent(name, ret);
		return ret;
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
