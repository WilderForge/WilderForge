package com.wildermods.wilderforge.launch.logging;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

public record ThreadDump(String dump) {
	
	public static ThreadDump capture() {
		StringBuilder text = new StringBuilder();
		ThreadMXBean threads = ManagementFactory.getThreadMXBean();
		text.append("---- THREAD DUMP ----\n\n");
		for (ThreadInfo dump : threads.dumpAllThreads(true, true, Integer.MAX_VALUE)) {
			String trimmedInfo = FullThreadInfo.from(dump).toString();
			text.append(trimmedInfo.toString());
		}
		return new ThreadDump(text.toString());
	}
	
	@Override
	public String toString() {
		return dump;
	}
	
}
