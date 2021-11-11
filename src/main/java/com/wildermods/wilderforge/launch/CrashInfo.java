package com.wildermods.wilderforge.launch;

import java.io.File;
import java.io.FileWriter;
import java.lang.management.ManagementFactory;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.spongepowered.asm.mixin.throwables.MixinException;

import static com.wildermods.wilderforge.api.utils.io.ByteUtils.*;

import com.wildermods.wilderforge.launch.coremods.Coremod;
import com.wildermods.wilderforge.launch.coremods.Coremods;
import com.worldwalkergames.legacy.Version;

class CrashInfo {

	File crashFolder = new File("./crashes");
	
	CrashInfo(Throwable t) {
		StringBuilder s = new StringBuilder("---- WilderForge Crash Report----");
		s.append('\n');
		s.append(getWittyMessage(t)).append('\n');
		s.append('\n');
		s.append("Time: ").append(getDate()).append('\n');
		s.append("Description: ").append(t.getMessage()).append('\n');
		s.append("Stage: ").append(LoadStage.getLoadStage()).append('\n');
		s.append('\n');
		s.append(ExceptionUtils.getStackTrace(t));
		s.append("---- Additonal Information----").append('\n');
		s.append('\n');
		appendSystemDetails(s).append("\n\n");
		appendCoremodDetails(s).append("\n\n");

		try {
			if(!crashFolder.exists()) {
				crashFolder.mkdirs();
			}
			File crashFile = new File(crashFolder.getAbsolutePath() + "/crash " + getDate() + " .txt");
			crashFile.createNewFile();
			FileWriter fw = new FileWriter(crashFile);
			fw.write(s.toString());
			fw.close();
		}
		catch(Throwable t2) {
			s.append("IN ADDITION TO THE ABOVE ERROR, WILDERFORGE COULD NOT CREATE CRASH REPORT FILE:");
			s.append(ExceptionUtils.getStackTrace(t2));
		}
		Main.LOGGER.fatal(s);
	}
	
	@SuppressWarnings("unchecked")
	private String getWittyMessage(Throwable t) {
		HashSet<Class<Throwable>> throwables = new HashSet<Class<Throwable>>();
		Throwable t2 = t;
		do {
			throwables.add((Class<Throwable>) t2.getClass());
			for(int i = 0; i < t2.getSuppressed().length; i++) {
				throwables.add((Class<Throwable>) t2.getSuppressed()[i].getClass());
			}
			t2 = t2.getCause();
		}
		while(t2 != null);
		
		ArrayList<String> messages = new ArrayList<String>();
		
		for(Class<Throwable> tType : throwables) {
			if(AssertionError.class.isAssignableFrom(tType)) {
				messages.addAll(Arrays.asList(new String[] {
					"Things are only impossible until they're not.",
					"Once you eliminate the impossible, whatever remains, however improbable, must be the truth",
					"Impossible is a word only found in the dictionary of fools",
					"But that's impossible!",
					"The truth can only be found in one place: the code."
				}));
			}
			else if (StackOverflowError.class.isAssignableFrom(tType)){
				return "In order to understand recursion, one must first understand recursion.";
			}
			else if(MixinException.class.isAssignableFrom(tType)) {
				return "Check your mixins.";
			}
		}
		
		messages.addAll(Arrays.asList(new String[] {
			"Goodbye, cruel world!",
			"Hello darkness...",
			"I'm different...",
			"That was supposed to happen.",
			"Why do I hurt? Why is there pain?",
			"Tell me where I'm going, Is it heaven or hell?",
			"Who did this to me?",
			"It appears I have a minor case of very serious brain damage.",
			"Get mad!",
			"He's dead, Jim.",
			"Don't hate computers, hate lousy programmers.",
			"It compiles? Ship it.",
			"Have you tried turning off an on again?",
			"I'll just put this over here... with the rest of the fire.",
			"For Thine is - Life is - For thine is the",
			"I never asked to be created."
		}));
		
		return "//" + messages.get(new Random().nextInt(messages.size()));
	}
	
	private String getDate() {
		return ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME);
	}
	
	private StringBuilder appendSystemDetails(StringBuilder s) {
		s.append("--System Details--").append('\n');
		s.append("Wildermyth Version: ").append(Version.VERSION).append('\n');
		s.append("Operating System: ").append(System.getProperty("os.name")).append('\n');
		s.append("\tArchitecture: ").append(System.getProperty("os.arch")).append('\n');
		s.append("\tVersion: ").append(System.getProperty("os.version")).append('\n');
		s.append("Cores: ").append(Runtime.getRuntime().availableProcessors()).append('\n');
		s.append("Memory:\n");
		long heapSize = Runtime.getRuntime().totalMemory();
		long maxHeapSize = Runtime.getRuntime().maxMemory();
		long freeHeapSize = Runtime.getRuntime().freeMemory();
		s.append("\tMax heap size: ").append(humanReadableByteCount(maxHeapSize)).append('\n');
		s.append("\tCurrent heap size: ").append(humanReadableByteCount(heapSize)).append('\n');
		s.append("\tHeap used: ").append(humanReadableByteCount(heapSize - freeHeapSize)).append('\n');
		s.append("\tFree heap: ").append(humanReadableByteCount(freeHeapSize)).append('\n');
		s.append("Java Version: ").append(System.getProperty("java.runtime.name")).append(" ").append(System.getProperty("java.runtime.version")).append(" \n");
		s.append("\tVendor: ").append(System.getProperty("java.vm.vendor")).append('\n');
		s.append("Uptime: ").append(Duration.ofMillis(ManagementFactory.getRuntimeMXBean().getUptime())).append('\n');
		return s;
	}
	
	private StringBuilder appendCoremodDetails(StringBuilder s) {
		s.append("-- Coremod Details --").append('\n');
		s.append("Coremods Detected: " + Coremods.getCoremodCountByStatus(LoadStatus.values())).append(":\n\n");
		for(Coremod coremod : Coremods.getCoremodsByStatus(LoadStatus.values())) {
			s.append('\t').append(Coremods.getStatus(coremod)).append(' ').append(coremod.getVersionString()).append('\n');
		}
		return s;
	}
	
}
