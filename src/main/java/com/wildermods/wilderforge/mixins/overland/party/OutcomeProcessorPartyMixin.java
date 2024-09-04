package com.wildermods.wilderforge.mixins.overland.party;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static com.wildermods.wilderforge.api.mixins.v1.Descriptor.*;
import com.wildermods.wilderforge.api.mechanicsV1.Kerneled;
import com.wildermods.wilderforge.api.overlandV1.party.PartyDisbandEvent;
import com.wildermods.wilderforge.launch.WilderForge;
import com.worldwalkergames.engine.Entity;
import com.worldwalkergames.legacy.game.campaign.model.Party;
import com.worldwalkergames.legacy.game.mechanics.EffectResolver.EffectContext;
import com.worldwalkergames.legacy.game.mechanics.OutcomeProcessor;
import com.worldwalkergames.legacy.game.model.effect.Outcome.Special;

@Mixin(OutcomeProcessor.class)
public abstract class OutcomeProcessorPartyMixin implements Kerneled {
	
	@Redirect(
		at = @At(
			value = "FIELD",
			target = "Lcom/worldwalkergames/legacy/game/model/effect/Outcome$Special;"
					+ "disbandParty:" + BOOLEAN
		),
		method = "processSpecial",
		require = 1
	)
	public boolean disbandParty(Special thiz, EffectContext context, Entity outcomeTarget, Special outcome) {
		if(thiz.disbandParty) {
			Party party = Party.of(outcomeTarget);
			if(WilderForge.MAIN_BUS.fire(new PartyDisbandEvent(party))) {
				getKernelWF().invokeDisbandParty(party);
			}
		}
		return false;
	}
	
}
