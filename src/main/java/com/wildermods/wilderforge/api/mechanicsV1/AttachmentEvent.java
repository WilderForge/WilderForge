package com.wildermods.wilderforge.api.mechanicsV1;

import net.minecraftforge.eventbus.api.Cancelable;

import com.wildermods.provider.util.logging.Logger;
import com.wildermods.wilderforge.api.eventV3.Event;
import com.wildermods.wilderforge.launch.WilderForge;
import com.worldwalkergames.legacy.game.model.Attachments;
import com.worldwalkergames.legacy.game.model.item.Item;
import com.worldwalkergames.legacy.server.context.ServerDataContext.ItemDTO;

/**
 * Base event for interactions involving {@link Attachments} and equippable {@link Item} instances.
 * <p>
 * This event provides access to both the runtime {@link Item} and its associated {@link ItemDTO}
 * from the item registry. Subclasses represent specific legality check contexts and may be used
 * to override or extend equip logic.
 * <p>
 * This event is fired on the {@linkplain com.wildermods.wilderforge.launch.WilderForge#MAIN_BUS MAIN_BUS};
 */
public abstract class AttachmentEvent extends Event {

	private final Logger LOGGER = new Logger(getClass());
	private final Attachments attachments;
	private Item item;
	private ItemDTO itemDTO;

	/**
	 * Constructs an AttachmentEvent.
	 *
	 * @param attachments The {@link Attachments} instance (usually a component of an entity).
	 * @param item        The item being evaluated or processed.
	 * @param cancellable Ignored in this implementation; events are non-cancellable.
	 * @throws IllegalStateException If the {@link ItemDTO} for the provided item cannot be resolved.
	 */
	public AttachmentEvent(Attachments attachments, Item item, boolean cancellable) {
		super(false);
		this.attachments = attachments;
		setItem(item);
	}

	protected void setItem(Item item) {
		ItemDTO itemDTO = WilderForge.getViewDependencies().dataContext.getItemData(null).itemsById.get(item.itemId);
		if (itemDTO == null) {
			LOGGER.warn("Could not locate itemDTO for " + item.itemId + ". Item will remain " + (this.item != null ? this.item.itemId : null));
		}
		this.item = item;
		this.setItemDTO(itemDTO);
	}

	protected void setItemDTO(ItemDTO itemDTO) {
		this.itemDTO = itemDTO;
	}

	/**
	 * @return The {@link Attachments} component of the entity associated with this event.
	 */
	public Attachments getAttachments() {
		return attachments;
	}

	/**
	 * @return The runtime {@link Item} being considered during the event.
	 */
	public Item getItem() {
		return item;
	}

	/**
	 * @return The {@link ItemDTO} backing the item, fetched from the registry. May be null.
	 */
	public ItemDTO getItemDTO() {
		return itemDTO;
	}
	
	/**
	 * A variant of {@link AttachmentEvent} that includes the result of the vanilla implementation.
	 * <p>
	 * This class is used for attachment check events that produce a meaningful return value
	 * (such as a boolean indicating whether an item can be equipped).
	 * <p>
	 * Listeners may inspect the {@linkplain #getVanillaResult() vanillaResult} to determine
	 * what the unmodified (vanilla) game logic would have returned, allowing for conditional
	 * overrides or enhancements without fully replacing the underlying behavior.
	 * <p>
	 * Note: The {@code vanillaResult} is resolved <strong>before</strong> the event is fired.
	 * If other mixins or coremods have already modified the underlying method, this result
	 * may not exactly match pure unmodded behavior.
	 *
	 * @param <R> The result type returned by the underlying vanilla logic (e.g., {@link Boolean}).
	 */
	public static abstract class AttachmentEventReturnable<R> extends AttachmentEvent {
		
		/**
		 * The vanilla implementation's result for this event.
		 * 
		 * Note: vanilla implementation could potentially be modified by coremods not using this api.
		 */
		protected final R vanillaResult;
		
		public AttachmentEventReturnable(Attachments attachments, Item item, R vanillaResult, boolean cancellable) {
			super(attachments, item, cancellable);
			this.vanillaResult = vanillaResult;
		}
		
		/**
		 * Returns the result that the vanilla (unmodified) implementation of the method
		 * would have produced, prior to any overrides by event listeners.
		 * <p>
		 * Note that if other coremods or mixins altered the original method before this event
		 * was fired, the result may not reflect truly unmodified vanilla behavior.
		 *
		 * @return The result that the underlying game logic would have returned.
		 */
		public R getVanillaResult() {
			return vanillaResult;
		}
	}

	/**
	 * Event fired during a general legality check to determine whether the specified entity
	 * is legally allowed to equip the item, disregarding runtime constraints like slot availability. 
	 * Corresponds to {@linkplain Attachments#legalToEquip(Item) legalToEquip()} logic.
	 * <p>
	 * This event is <strong>not</strong> {@linkplain Cancelable cancellable} but does {@linkplain HasResult have a result}.
	 * Listeners can override the default logic by setting the result:
	 * <ul>
	 *     <li>{@linkplain Result#ALLOW ALLOW} - force the item to be considered legal.</li>
	 *     <li>{@linkplain Result#DEFAULT DEFAULT} - defer to vanilla game logic ({@linkplain AttachmentEventReturnable#vanillaResult vanillaResult})</li>
	 *     <li>{@linkplain Result#DENY DENY} - prevent the item from being equipped.</li>
	 * </ul>
	 * <p>
	 * This event is fired on the {@linkplain com.wildermods.wilderforge.launch.WilderForge#MAIN_BUS MAIN_BUS}.
	 */
	@HasResult
	public static class ItemEquipLegalityCheckEvent extends AttachmentEventReturnable<Boolean> {
		/**
		 * @param attachments The attachments of the entity component being queried.
		 * @param item        The item being checked for legality.
		 */
		public ItemEquipLegalityCheckEvent(Attachments attachments, Item item, boolean vanillaResult) {
			super(attachments, item, vanillaResult, false);
		}
	}

	/**
	 * Event fired when checking whether an item can currently be equipped on this entity, including runtime 
	 * constraints like slot usage and forbidden status.
	 * <p>
	 * Corresponds to {@linkplain Attachments#canEquip canEquip()} logic.
	 * <p>
	 * This event is not {@linkplain Cancelable cancellable} but does {@linkplain HasResult have a result}.
	 * {@linkplain Result#ALLOW ALLOW} will allow the item to be equipped.
	 * {@linkplain Result#DEFAULT DEFAULT} will defer to the vanilla implementation ({@linkplain AttachmentEventReturnable#vanillaResult vanillaResult}).
	 * {@linkplain Result#DENY} will prevent the item from being equipped.
	 * <p>
	 * This event is fired on the {@linkplain com.wildermods.wilderforge.launch.WilderForge#MAIN_BUS MAIN_BUS};
	 */
	@HasResult
	public static class CanEquipCheckEvent extends AttachmentEventReturnable<Boolean> {
		/**
		 * @param attachments The attachments of the entity component being queried.
		 * @param item        The item being checked for current equip eligibility.
		 */
		public CanEquipCheckEvent(Attachments attachments, Item item, boolean vanillaResult) {
			super(attachments, item, vanillaResult, false);
		}
	}

	/**
	 * Event fired specifically when checking armor legality.
	 * Corresponds to {@linkplain Attachments#isArmorLegal isArmorLegal()} logic.
	 * <p>
	 * This event is not {@linkplain Cancelable cancellable} but does {@linkplain HasResult have a result}.
	 * {@linkplain Result#ALLOW ALLOW} force the armor to be considered legal.
	 * {@linkplain Result#DEFAULT DEFAULT} defer to vanilla game logic ({@linkplain AttachmentEventReturnable#vanillaResult vanillaResult}).
	 * {@linkplain Result#DENY} prevent the item from being equipped.
	 * <p>
	 * This event is fired on the {@linkplain com.wildermods.wilderforge.launch.WilderForge#MAIN_BUS MAIN_BUS};
	 */
	@HasResult
	public static class ArmorEquipLegalityCheckEvent extends AttachmentEventReturnable<Boolean> {
		/**
		 * @param attachments The attachments of the entity component being queried.
		 * @param item        The armor item being checked for legality.
		 */
		public ArmorEquipLegalityCheckEvent(Attachments attachments, Item item, boolean vanillaResult) {
			super(attachments, item, vanillaResult, false);
		}
	}
}
