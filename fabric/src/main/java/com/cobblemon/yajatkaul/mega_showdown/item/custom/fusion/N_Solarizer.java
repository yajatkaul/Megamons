package com.cobblemon.yajatkaul.mega_showdown.item.custom.fusion;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.pokemon.feature.FlagSpeciesFeature;
import com.cobblemon.mod.common.api.pokemon.feature.FlagSpeciesFeatureProvider;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.yajatkaul.mega_showdown.advancement.AdvancementHelper;
import com.cobblemon.yajatkaul.mega_showdown.datamanage.DataManage;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

import java.util.List;
import java.util.Set;

public class N_Solarizer extends Item {
    public N_Solarizer(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnEntity(ItemStack arg, PlayerEntity player, LivingEntity context, Hand hand) {
        if (player.getWorld().isClient) {
            return ActionResult.PASS;
        }

        if (!(context instanceof PokemonEntity pk)) {
            return ActionResult.PASS;
        }

        Pokemon pokemon = pk.getPokemon();
        if (pokemon.getOwnerPlayer() != player) {
            return ActionResult.PASS;
        }

        PlayerPartyStore playerPartyStore = Cobblemon.INSTANCE.getStorage().getParty((ServerPlayerEntity) player);
        boolean currentValue = arg.getOrDefault(DataManage.N_SOLAR, false);

        if (currentValue && pokemon.getSpecies().getName().equals("Necrozma") && !player.getAttached(DataManage.N_SOLAR_POKEMON).equals(new Pokemon())) {
            arg.set(DataManage.N_SOLAR, false);
            pokemon.setForcedAspects(Set.of("dusk-fusion"));

            AdvancementHelper.grantAdvancement((ServerPlayerEntity) player, "fusion");

            arg.set(DataComponentTypes.CUSTOM_NAME, Text.translatable("item.mega_showdown.n_solarizer.inactive"));
        } else if (!currentValue && pokemon.getSpecies().getName().equals("Solgaleo")) {
            arg.set(DataManage.N_SOLAR, true);
            player.setAttached(DataManage.N_SOLAR_POKEMON, pk.getPokemon());
            playerPartyStore.remove(pk.getPokemon());
            arg.set(DataComponentTypes.CUSTOM_NAME, Text.translatable("item.mega_showdown.n_solarizer.charged"));
        } else if (pokemon.getSpecies().getName().equals("Necrozma") && pokemon.getForcedAspects().contains("dusk-fusion")) {
            FlagSpeciesFeatureProvider featureProvider = new FlagSpeciesFeatureProvider(List.of("ultra"));
            FlagSpeciesFeature feature = featureProvider.get(pokemon);

            if(feature != null){
                boolean enabled = featureProvider.get(pokemon).getEnabled();

                if(enabled) {
                    return ActionResult.PASS;
                }
            }

            playerPartyStore.add(player.getAttached(DataManage.N_SOLAR_POKEMON));
            player.setAttached(DataManage.N_SOLAR_POKEMON, new Pokemon());
            pokemon.setForcedAspects(Set.of());
            arg.set(DataComponentTypes.CUSTOM_NAME, Text.translatable("item.mega_showdown.n_solarizer.inactive"));
        } else {
            return ActionResult.PASS;
        }

        player.setStackInHand(hand, arg);
        return ActionResult.SUCCESS;
    }
}
