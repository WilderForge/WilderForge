package com.wildermods.wilderforge.launch.logging;

import org.lwjgl.opengl.GL20;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Graphics.Monitor;
import com.worldwalkergames.legacy.LegacyDesktop;

public class GraphicalInfo {

	public static GraphicalInfo INSTANCE;
	
	public final String graphicInfo;
	
	public GraphicalInfo(LegacyDesktop legacy) {
        StringBuilder s = new StringBuilder();
		try {
			s.append("\tVendor: ").append('\n');
			s.append("\t\t" + Gdx.gl.glGetString(GL20.GL_VENDOR)).append('\n');
		}
		catch(Throwable t) {
			s.append("\t\tCould not obtain vendor information due to a ").append(t.getClass().getCanonicalName()).append('\n');
		}
		try {
			s.append("\tCard: ").append('\n');
			s.append("\t\t" + Gdx.gl.glGetString(GL20.GL_RENDERER)).append('\n');
		}
		catch(Throwable t) {
			s.append("\t\tCould not obtain graphics card information due to a ").append(t.getClass().getCanonicalName()).append('\n');
		}

		try {
			s.append("\tMonitors:").append('\n');
			s.append("\t\tTotal monitors (OpenGL) ");
			Monitor[] monitors = Gdx.graphics.getMonitors();
			s.append(monitors.length).append(':').append('\n');
			for(int i = 0; i < monitors.length; i++) {
				Monitor monitor = monitors[i];
				s.append("\t\t\tMonitor " + i + ":\n");
				s.append("\t\t\t\tName: " + monitor.name).append('\n');
				DisplayMode displayMode = Gdx.graphics.getDisplayMode(monitor);
				s.append("\t\t\t\tResolution: " + displayMode.width + "x" + displayMode.height + "@" + displayMode.refreshRate + "hz").append('\n');
			}
			
		}
		catch(Throwable t) {
			s.append("\t\tCould not OpenGL monitor information due to a ").append(t.getClass().getCanonicalName()).append('\n');
		}
		
		graphicInfo = s.toString();
		
	}
	
	StringBuilder appendGraphicalDetails(StringBuilder s) {
		return s.append(graphicInfo);
	}
	
}
