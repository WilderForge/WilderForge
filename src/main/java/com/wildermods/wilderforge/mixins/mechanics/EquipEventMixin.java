package com.wildermods.wilderforge.mixins.mechanics;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.wildermods.wilderforge.api.mechanicsV1.AttachmentEvent.ArmorEquipLegalityCheckEvent;
import com.wildermods.wilderforge.api.mechanicsV1.AttachmentEvent.AttachmentEventReturnable;
import com.wildermods.wilderforge.api.mechanicsV1.AttachmentEvent.CanEquipCheckEvent;
import com.wildermods.wilderforge.api.mechanicsV1.AttachmentEvent.CanEquipInactiveCheckEvent;
import com.wildermods.wilderforge.api.mechanicsV1.AttachmentEvent.ItemEquipLegalityCheckEvent;
import com.wildermods.wilderforge.api.mechanicsV1.AttachmentEvent.CheckRequiredAndForbiddenAspectsEvent;
import com.wildermods.wilderforge.api.mixins.v1.Cast;
import com.wildermods.wilderforge.launch.WilderForge;
import com.worldwalkergames.legacy.game.model.Attachments;
import com.worldwalkergames.legacy.game.model.item.Item;

@Mixin(Attachments.class)
public class EquipEventMixin {

	@WrapMethod(method = "legalToEquip")
	private @Unique boolean fireGeneralLegalityEquipCheckEvent(Item item, Operation<Boolean> original) {
		return fireBoolReturnableAttachmentEvent(new ItemEquipLegalityCheckEvent(Cast.from(this), item, original.call(item)));
	}
	
	@WrapMethod(method = "canEquip")
	private boolean fireCanCurrentlyEquipCheckEvent(Item item, Operation<Boolean> original) {
		return fireBoolReturnableAttachmentEvent(new CanEquipCheckEvent(Cast.from(this), item, original.call(item)));
	}
	
	@WrapMethod(method = "canEquipInactive")
	private boolean fireCanEquipInactiveCheckEvent(Item item, Operation<Boolean> original) {
		return fireBoolReturnableAttachmentEvent(new CanEquipInactiveCheckEvent(Cast.from(this), item, original.call(item)));
	}
	
	@WrapMethod(method = "isArmorLegal")
	private boolean fireArmorLegalityEquipCheckEvent(Item item, Operation<Boolean> original) {
		return fireBoolReturnableAttachmentEvent(new ArmorEquipLegalityCheckEvent(Cast.from(this), item, original.call(item)));
	}
	
	@SuppressWarnings("deprecation")
	@WrapMethod(method = "checkRequiredAndForbiddenAspects", require = 1, expect = 1) //require and expect set to 0 because this method doesn't exist on versions prior to 1.16+561
	private boolean fireCheckRequiredAndForbiddenAspectsEvent(Item item, Operation<Boolean> original) {
		return fireBoolReturnableAttachmentEvent(new CheckRequiredAndForbiddenAspectsEvent(Cast.from(this), item, original.call(item)));
	}
	
	private @Unique Boolean fireBoolReturnableAttachmentEvent(AttachmentEventReturnable<Boolean> e) {
		switch(WilderForge.MAIN_BUS.fire(e).getResult()) {
			case ALLOW:
				return true;
			case DENY:
				return false;
			case DEFAULT:
			default:
				return e.getVanillaResult();
		}
	}
	
}
