package com.wildermods.wilderforge.launch.logging;

import java.io.PrintWriter;
import java.io.StringWriter;

import net.fabricmc.loader.impl.util.log.LogCategory;

public class Logger implements ILogger {

	private final int ordinal;
	
	private final String name;
	
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
		this.name = name;
		this.ordinal = minLevel.ordinal();
	}
	
	@Override
	public void log(LogLevel level, String s) {
		if(shouldLog(level)) {
			System.out.println("[" + Thread.currentThread().getName() + "/" + level + "] [" + name + "] " + s);
		}
	}
	
	public void log(LogLevel level, String s, String tag) {
		if(tag != null) {
			if(shouldLog(level)) {
				System.out.println("[" + Thread.currentThread().getName() + "/" + level + "] [" + name + "/" + tag + "] " + s);
			}
		}
		else {
			log(level, s);
		}
	}

	@Override
	public void catching(LogLevel level, Throwable t) {
		if(shouldLog(level)) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			pw.append("[" + Thread.currentThread().getName() + "/" + level + "] [" + name + "] Catching " );
			t.printStackTrace(pw);
			System.out.println(sw.toString());
		}
	}
	
	public void catching(LogLevel level, Throwable t, String tag) {
		if(tag == null) {
			catching(level, t);
			return;
		}
		if(shouldLog(level)) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			pw.append("[" + Thread.currentThread().getName() + "/" + level + "] [" + name + "/" + tag + "] Catching ");
			t.printStackTrace(pw);
			System.out.println(sw.toString());
		}
	}

	@Override
	public boolean shouldLog(LogLevel level) {
		return level.ordinal() >= ordinal;
	}

	@Override
	public void log(long time, net.fabricmc.loader.impl.util.log.LogLevel level, LogCategory category, String msg, Throwable exc, boolean fromReplay, boolean wasSuppressed) {
		LogLevel l = LogLevel.getLevel(level);
		if(exc == null) {
			log(l, msg, category.name);
		}
		else {
			catching(l, exc, category.name);
		}
	}

}
