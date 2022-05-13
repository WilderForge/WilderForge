package com.wildermods.wilderforge.api.uiV1;

import com.wildermods.wilderforge.launch.WilderForge;
import com.wildermods.wilderforge.mixins.ui.CampaignHudAccessor;
import com.wildermods.wilderforge.mixins.ui.CampaignScreenAccessor;
import com.wildermods.wilderforge.mixins.ui.MainScreenAccessor;
import com.worldwalkergames.legacy.browse.LegacyBrowserScreen;
import com.worldwalkergames.legacy.game.campaign.ui.CampaignHud;
import com.worldwalkergames.legacy.game.mission.ui.MissionScreen;
import com.worldwalkergames.legacy.ui.CampaignScreen;
import com.worldwalkergames.legacy.ui.MainScreen;
import com.worldwalkergames.legacy.ui.credits.GameResultsScreen;
import com.worldwalkergames.legacy.ui.interval.IntervalScreen;
import com.worldwalkergames.legacy.ui.menu.RootMenuScreen;
import com.worldwalkergames.legacy.ui.titlescreen.ITopLevelScreen;
import com.worldwalkergames.scratchpad.EmbeddedScratchpadScreen;

public final class UI {

	private UI() {
		throw new AssertionError();
	}
	
	public static MainScreen getMainScreen() {
		return WilderForge.getLegacyDesktop().getMainScreen();
	}
	
	public static MainScreenAccessor getMainScreenAccessor() {
		return WilderForge.getLegacyDesktop().getMainScreenAccessor();
	}
	
	public static ITopLevelScreen getCurrentScreen() {
		return getMainScreenAccessor().getScreen();
	}
	
	public static RootMenuScreen getRootMenuScreen() {
		ITopLevelScreen screen = getCurrentScreen();
		if(screen instanceof RootMenuScreen) {
			return (RootMenuScreen) screen;
		}
		return null;
	}
	
	public static CampaignScreen getCampaignScreen() {
		ITopLevelScreen screen = getCurrentScreen();
		if(screen instanceof CampaignScreen) {
			return (CampaignScreen) screen;
		}
		return null;
	}
	
	public static CampaignScreenAccessor getCampaignScreenAccessor() {
		ITopLevelScreen screen = getCurrentScreen();
		if(screen instanceof CampaignScreenAccessor) {
			return (CampaignScreenAccessor) screen;
		}
		return null;
	}
	
	public static MissionScreen getMissionScreen() {
		ITopLevelScreen screen = getCurrentScreen();
		if(screen instanceof MissionScreen) {
			return (MissionScreen) screen;
		}
		return null;
	}
	
	public static LegacyBrowserScreen getLegacyBrowserScreen() {
		ITopLevelScreen screen = getCurrentScreen();
		if(screen instanceof LegacyBrowserScreen) {
			return (LegacyBrowserScreen) screen;
		}
		return null;
	}
	
	public static IntervalScreen getIntervalScreen() {
		ITopLevelScreen screen = getCurrentScreen();
		if(screen instanceof IntervalScreen) {
			return (IntervalScreen) screen;
		}
		return null;
	}
	
	public static GameResultsScreen getGameResultScreen() {
		ITopLevelScreen screen = getCurrentScreen();
		if(screen instanceof GameResultsScreen) {
			return (GameResultsScreen) screen;
		}
		return null;
	}
	
	public static EmbeddedScratchpadScreen getScratchpadScreen() {
		ITopLevelScreen screen = getCurrentScreen();
		if(screen instanceof EmbeddedScratchpadScreen) {
			return (EmbeddedScratchpadScreen) screen;
		}
		return null;
	}
	
	public static CampaignHudAccessor convert(CampaignHud hud) {
		return (CampaignHudAccessor)hud;
	}
	
}
