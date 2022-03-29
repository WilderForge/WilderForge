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
		trace(o);
	}
	
	public default void debug(Object o) {
		debug(o);
	}
	
	public default void info(Object o) {
		info(o);
	}
	
	public default void warn(Object o) {
		warn(o);
	}
	
	public default void error(Object o) {
		error(o);
	}
	
	public default void fatal(Object o) {
		fatal(o);
	}
	
	public default void log(Object o) {
		info(o);
	}
	
	public void log(LogLevel level, String s);
	
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
