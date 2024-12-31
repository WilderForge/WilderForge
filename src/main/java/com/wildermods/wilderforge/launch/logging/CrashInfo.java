package com.wildermods.wilderforge.launch.logging;

import java.awt.GraphicsEnvironment;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.spongepowered.asm.mixin.throwables.MixinException;

import com.wildermods.provider.services.CrashLogService;

import static com.wildermods.wilderforge.api.utils.io.ByteUtils.*;

import com.wildermods.wilderforge.api.modLoadingV1.CoremodInfo;
import com.wildermods.wilderforge.launch.WilderForge;
import com.wildermods.wilderforge.launch.coremods.Coremods;
import com.worldwalkergames.legacy.Version;

public final class CrashInfo implements CrashLogService {

	volatile ThreadDump dump;
	volatile boolean shouldCollectDump = false;
	
	@Override
	public void logCrash(Throwable t) {
		logCrash(t, null);
	}
	
	public void logCrash(Throwable t, ThreadDump dump) {
		StringBuilder s = new StringBuilder("---- WilderForge Crash Report----");
		s.append('\n');
		try {
			s.append(getWittyMessage(t)).append('\n');
		}
		catch(Throwable failedWittyMessage) {
			//swallow, the message is just for the funnies, don't care if it's not added
		};
		s.append('\n');
		s.append("Time: ");
		try {
			s.append(getDate()).append('\n');
		}
		catch(Throwable failedDate) {
			s.append("COULD NOT APPEND TIME DUE TO THE FOLLOWING EXCPETION:").append('\n');
			try {
				s.append(ExceptionUtils.getStackFrames(failedDate));
			}
			catch(Throwable failedStack) {
				s.append("YIKES! Couldn't get the stacktrace either! Printing to console instead!");
				System.out.println(s);
				failedStack.printStackTrace();
			}
			s.append('\n');
		}
		s.append("Description: ");
		try {
			s.append(getLowestDescription(t));
		}
		catch(Throwable failedDescription) {
			s.append('\n');
		}
		s.append('\n');
		s.append('\n');
		try {
			s.append(ExceptionUtils.getStackTrace(t));
		}
		catch(Throwable failedStack) {
			s.append("YIKES! Couldn't get the stacktrace! Printing to console instead!");
			System.out.println(s);
			failedStack.printStackTrace();
		}
		s.append('\n');
		s.append("---- Additonal Information----").append('\n');
		s.append('\n');
		appendSystemDetails(s).append("\n\n");
		appendCoremodDetails(s);
		
		if(this.dump != null || shouldCollectDump) {
			s.append("\n\n");
			appendThreadDump(s);
		}

		try {
			File crashFile = getCrashFile();
			FileWriter fw = new FileWriter(crashFile);
			fw.write(s.toString());
			fw.close();
		}
		catch(Throwable t2) {
			s.append("IN ADDITION TO THE ABOVE ERROR, WILDERLOADER COULD NOT CREATE CRASH REPORT FILE:");
			s.append(ExceptionUtils.getStackTrace(t2));
		}
		WilderForge.LOGGER.fatal(s);
	}
	
	public void doThreadDump(boolean dumpThreads) {
		this.shouldCollectDump = dumpThreads;
	}
	
	public boolean isDumpingThreads() {
		return dump != null || shouldCollectDump;
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
				return "//In order to understand recursion, one must first understand recursion.";
			}
			else if(MixinException.class.isAssignableFrom(tType)) {
				return "//Check your mixins.";
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
			"It appears I have a very minor case of serious brain damage.",
			"Get mad!",
			"He's dead, Jim.",
			"Don't hate computers, hate lousy programmers.",
			"It compiles? Ship it.",
			"Have you tried turning off an on again?",
			"I'll just put this over here... with the rest of the fire.",
			"For Thine is - Life is - For thine is the",
			"I never asked to be created.",
			"Sunlight on a broken column",
			"I am ready to meet my maker, but whether my maker is ready for the great ordeal of meeting me is another matter.",
			"A man who makes no mistakes does not usually make anything.",
			"I'm not really dead, as long as you remember me."
			
		}));
		
		return "//" + messages.get(new Random().nextInt(messages.size()));
	}
	
	private String getDate() {
		return ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT);
	}
	
	private StringBuilder appendSystemDetails(StringBuilder s) {
		try {
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
			appendGraphicalDetails(s);
			s.append("Java Version: ").append(System.getProperty("java.runtime.name")).append(" ").append(System.getProperty("java.runtime.version")).append(" \n");
			s.append("\tVendor: ").append(System.getProperty("java.vm.vendor")).append('\n');
			s.append("Uptime: ").append(Duration.ofMillis(ManagementFactory.getRuntimeMXBean().getUptime())).append('\n');
		}
		catch(Throwable t) {
			t.printStackTrace();
		}
		return s;
	}
	
	private void appendGraphicalDetails(StringBuilder s) {
		s.append("Graphical information:\n");
		if(GraphicalInfo.INSTANCE != null) {
			GraphicalInfo.INSTANCE.appendGraphicalDetails(s);
		}
		else {
			s.append("\t\tTotal monitors (OpenGL):\n");
			s.append("\t\t\tOpenGL context not fully intialized, no opengl information available\n");
		}
		try {
			GraphicsEnvironment g = GraphicsEnvironment.getLocalGraphicsEnvironment();
			s.append("\t\tTotal monitors (Java): " + g.getScreenDevices().length).append('\n');
			for(int i = 0; i < g.getScreenDevices().length; i++) {
				GraphicsDevice monitor = g.getScreenDevices()[i];
				DisplayMode displayMode = monitor.getDisplayMode();
				s.append("\t\t\tMonitor " + i + "\n");
				s.append("\t\t\t\tName: " + monitor.getIDstring()).append('\n');
				s.append("\t\t\t\tResolution: " + displayMode.getWidth() + "x" + displayMode.getHeight() + "@" + displayMode.getRefreshRate() + "hz").append('\n');
			}
		}
		catch(Throwable t) {
			s.append("\t\tCould not obtain Java monitor information due to a ").append(t.getClass().getCanonicalName()).append('\n');
		}
	}
	
	private void appendThreadDump(StringBuilder s) {
		s.append(initializeThreadDump());
	}
	
	private StringBuilder appendCoremodDetails(StringBuilder s) {
		s.append("-- Coremod Details --").append('\n');
		s.append("Coremods Detected: " + Coremods.getCoremodCount()).append(":\n\n");
		for(CoremodInfo coremod : Coremods.getAllCoremods()) {
			s.append('\t').append(coremod.modId).append(' ').append(coremod.getMetadata().getVersion()).append('\n');
		}
		return s;
	}
	
	private String getLowestDescription(Throwable t) {
		String description = null;
		do {
			if(t.getMessage() != null) {
				description = t.getMessage();
			}
			t = t.getCause();
		}
		while(t.getCause() != null);
		if(t.getMessage() != null) {
			description = t.getMessage();
		}
		return description;
	}
	
	private File getCrashFile() {
		File file = new File("./crashes/" + DateTimeFormatter.ISO_DATE.format(LocalDateTime.now()) + " (1).crash.log");
		for(int i = 2; file.exists(); i++) {
			file = new File("./crashes/" + DateTimeFormatter.ISO_DATE.format(LocalDateTime.now()) + " (" + i + ").crash.log");
		}
		try {
			file.getParentFile().mkdirs();
			file.createNewFile();
		} catch (IOException e) {
			System.err.println(file.getAbsolutePath());
			throw new AssertionError(e);
		}
		return file;
	}
	
	public ThreadDump initializeThreadDump() {
		if(dump == null) {
			dump = ThreadDump.capture();
		}
		return dump;
	}
	
}
