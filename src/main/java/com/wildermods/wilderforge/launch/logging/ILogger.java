package com.wildermods.wilderforge.launch.logging;

import static com.wildermods.wilderforge.launch.logging.LogLevel.*;

import net.fabricmc.loader.impl.util.log.LogCategory;
import net.fabricmc.loader.impl.util.log.LogHandler;

public interface ILogger extends LogHandler {
	
	public default void trace(String s) {
		log(TRACE, s);
	}
	
	public default void debug(String s) {
		log(DEBUG, s);
	}
	
	public default void info(String s) {
		log(INFO, s);
	}
	
	public default void warn(String s) {
		log(WARN, s);
	}
	
	public default void error(String s) {
		log(ERROR, s);
	}
	
	public default void fatal(String s) {
		log(FATAL, s);
	}
	
	public default void log(String s) {
		info(s);
	}
	
	public default void trace(Object o) {
		trace(o.toString());
	}
	
	public default void debug(Object o) {
		debug(o.toString());
	}
	
	public default void info(Object o) {
		info(o.toString());
	}
	
	public default void warn(Object o) {
		warn(o.toString());
	}
	
	public default void error(Object o) {
		error(o.toString());
	}
	
	public default void fatal(Object o) {
		fatal(o.toString());
	}
	
	public default void log(Object o) {
		info(o.toString());
	}
	
	public default void trace(String s, String tag) {
		log(TRACE, s);
	}
	
	public default void debug(String s, String tag) {
		log(DEBUG, s);
	}
	
	public default void info(String s, String tag) {
		log(INFO, s);
	}
	
	public default void warn(String s, String tag) {
		log(WARN, s);
	}
	
	public default void error(String s, String tag) {
		log(ERROR, s);
	}
	
	public default void fatal(String s, String tag) {
		log(FATAL, s);
	}
	
	public default void log(String s, String tag) {
		info(s, tag);
	}
	
	public default void trace(Object o, String tag) {
		trace(o.toString(), tag);
	}
	
	public default void debug(Object o, String tag) {
		debug(o.toString(), tag);
	}
	
	public default void info(Object o, String tag) {
		info(o.toString(), tag);
	}
	
	public default void warn(Object o, String tag) {
		warn(o.toString(), tag);
	}
	
	public default void error(Object o, String tag) {
		error(o.toString(), tag);
	}
	
	public default void fatal(Object o, String tag) {
		fatal(o.toString(), tag);
	}
	
	public default void log(Object o, String tag) {
		info(o.toString(), tag);
	}
	
	public void log(LogLevel level, String s);
	
	public void log(LogLevel level, String s, String tag);
	
	public default void log(LogLevel level, Object o) {
		log(level, o.toString());
	}
	
	public default void catching(Throwable t) {
		catching(INFO, t);
	}
	
	public void catching(LogLevel level, Throwable t);

	@Override
	public void log(long time, net.fabricmc.loader.impl.util.log.LogLevel level, LogCategory category, String msg, Throwable exc, boolean isReplayedBuiltin);
	
	public boolean shouldLog(LogLevel level);

	@Override
	public default boolean shouldLog(net.fabricmc.loader.impl.util.log.LogLevel level, LogCategory category) {
		return shouldLog(LogLevel.getLevel(level));
	}

	@Override
	public default void close() {
		//no-op
	}
	
}
