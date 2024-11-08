package com.wildermods.wilderforge.api.mechanicsV1;

import com.wildermods.wilderforge.api.eventV1.Event;
import com.worldwalkergames.legacy.game.campaign.model.GameSettings;


/**
 * Fired when the chapter number in the game's {@link GameSettings} is set or about to be set.
 * This event allows listeners to react to the chapter change and, if needed, modify the new chapter 
 * number before it is applied.
 * 
 * <p>The event is fired on the {@code WilderForge.MAIN_BUS} to ensure that all registered listeners 
 * have an opportunity to handle or modify the chapter change.</p>
 * 
 * <p>Note: This event is <strong>not cancellable</strong>, meaning that listeners cannot prevent 
 * the chapter number from being set. However, they can modify the chapter number using 
 * {@link #setChapter(int)}. If a listener wants the chapter to remain the same, it can simply 
 * set the chapter number to {@link #getPreviousChapter()}.</p>
 * 
 * <h2>Usage Example:</h2>
 * <pre>
 * {@code
 * @SubscribeEvent
 * public void onChapterSet(ChapterSetEvent event) {
 *     if (event.getNewChapter() > 4) {
 *         event.setChapter(4); // Cap the chapter number at 4
 *     } else if (event.getChapter() < 1) {
 *         event.setChapter(event.getPreviousChapter()); // Keep the chapter number unchanged
 *     }
 * }
 * }
 * </pre>
 * 
 * <p>This example demonstrates how a listener can modify the chapter number to ensure it does not 
 * exceed a certain limit, or keep it unchanged if the new chapter number is undesirable.</p>
 * 
 * @see GameSettings
 */
public class ChapterSetEvent extends Event {

	protected final GameSettings settings;
	protected int chapter;
	
    /**
     * Constructs a new {@code ChapterSetEvent}.
     *
     * @param settings The {@link GameSettings} instance where the chapter change is occurring.
     * @param chapter The new chapter number that the game will be set to.
     */
	public ChapterSetEvent(GameSettings settings, int chapter) {
		super(false);
		this.settings = settings;
		this.chapter = chapter;
	}
	
	/**
	 * @return the new chapter number the game is going to be set to
	 */
	public int getNewChapter() {
		return chapter;
	}

    /**
     * Sets the new chapter number to set the game to.
     *
     * @param chapter The chapter number to set the game to.
     */
	public void setNewChapter(int chapter) {
		this.chapter = chapter;
	}
	
	/**
	 * @return the chapter the game was previously on before this event was fired
	 */
	public int getPreviousChapter() {
		return settings.getChapterNumber();
	}
	
}
