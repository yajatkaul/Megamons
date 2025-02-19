package com.cobblemon.yajatkaul.mega_showdown.cobbleEvents;

import com.cobblemon.mod.common.api.events.pokemon.HeldItemEvent;
import com.cobblemon.mod.common.api.events.pokemon.TradeCompletedEvent;
import com.cobblemon.mod.common.api.events.storage.ReleasePokemonEvent;
import com.cobblemon.mod.common.api.pokemon.feature.FlagSpeciesFeature;
import com.cobblemon.mod.common.api.pokemon.feature.FlagSpeciesFeatureProvider;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import com.cobblemon.yajatkaul.mega_showdown.Config;
import com.cobblemon.yajatkaul.mega_showdown.datamanage.DataManage;
import com.cobblemon.yajatkaul.mega_showdown.utility.Utils;
import kotlin.Unit;
import net.minecraft.server.level.ServerPlayer;

import javax.xml.crypto.Data;
import java.util.List;

public class CobbleEventsHandler {
    public static Unit onMegaTraded(TradeCompletedEvent tradeCompletedEvent) {
        if(!Config.multipleMegas){
            ServerPlayer player1 = tradeCompletedEvent.getTradeParticipant1Pokemon().getOwnerPlayer();
            ServerPlayer player2 = tradeCompletedEvent.getTradeParticipant2Pokemon().getOwnerPlayer();

            if(player1 == null || player2 == null || player2.level().isClientSide || player1.level().isClientSide){
                return Unit.INSTANCE;
            }

            Pokemon pokemon1 = tradeCompletedEvent.getTradeParticipant1Pokemon();
            Pokemon pokemon2 = tradeCompletedEvent.getTradeParticipant2Pokemon();

            boolean mega1 = false;
            boolean mega2 = false;

            List<String> megaKeys = List.of("mega-x", "mega-y", "mega");

            for (String key : megaKeys) {
                FlagSpeciesFeatureProvider featureProvider = new FlagSpeciesFeatureProvider(List.of(key));

                FlagSpeciesFeature feature = featureProvider.get(pokemon1);
                FlagSpeciesFeature feature2 = featureProvider.get(pokemon2);
                if(feature != null){
                    boolean enabled = featureProvider.get(pokemon1).getEnabled();
                    if (enabled && feature.getName().equals("mega")) {
                        mega1 = true;
                    }else if(enabled && feature.getName().equals("mega-x")){
                        mega1 = true;
                    } else if (enabled && feature.getName().equals("mega-y")) {
                        mega1 = true;
                    }
                }

                if(feature2 != null){
                    boolean enabled = featureProvider.get(pokemon2).getEnabled();

                    if (enabled && feature2.getName().equals("mega")) {
                        mega2 = true;
                    }else if(enabled && feature2.getName().equals("mega-x")){
                        mega2 = true;
                    } else if (enabled && feature2.getName().equals("mega-y")) {
                        mega2 = true;
                    }
                }
            }

            if(mega1){
                player1.setData(DataManage.MEGA_DATA, false);
                player1.setData(DataManage.MEGA_POKEMON, new Pokemon());
                DevolveOnTrade(pokemon1);
            }
            if(mega2){
                player2.setData(DataManage.MEGA_DATA, false);
                player2.setData(DataManage.MEGA_POKEMON, new Pokemon());
                DevolveOnTrade(pokemon2);
            }
        }

        return Unit.INSTANCE;
    }

    public static void DevolveOnTrade(Pokemon pokemon){
        new FlagSpeciesFeature("mega", false).apply(pokemon);
        new FlagSpeciesFeature("mega-x", false).apply(pokemon);
        new FlagSpeciesFeature("mega-y", false).apply(pokemon);
    }

    public static Unit onHeldItemChange(HeldItemEvent.Post event) {
        if(Config.battleModeOnly){
            return Unit.INSTANCE;
        }

        Pokemon pokemon = event.getPokemon();

        if(pokemon.getEntity() == null){
            return Unit.INSTANCE;
        }

        Species species = Utils.MEGA_STONE_IDS.get(pokemon.heldItem().getItem());
        if(pokemon.getEntity().level().isClientSide){
            return Unit.INSTANCE;
        }

        List<String> megaKeys = List.of("mega-x", "mega-y", "mega");

        for (String key : megaKeys) {
            FlagSpeciesFeatureProvider featureProvider = new FlagSpeciesFeatureProvider(List.of(key));
            ServerPlayer player = pokemon.getOwnerPlayer();

            FlagSpeciesFeature feature = featureProvider.get(pokemon);
            if(feature != null){
                boolean enabled = featureProvider.get(pokemon).getEnabled();

                if (enabled && feature.getName().equals("mega") && (species != pokemon.getSpecies() || event.getReceived() != event.getReturned())) {
                    player.setData(DataManage.MEGA_DATA, false);
                    player.setData(DataManage.MEGA_POKEMON, new Pokemon());

                    new FlagSpeciesFeature("mega", false).apply(pokemon);

                }else if(enabled && feature.getName().equals("mega-x") && (species != pokemon.getSpecies() || event.getReceived() != event.getReturned())){
                    player.setData(DataManage.MEGA_DATA, false);
                    player.setData(DataManage.MEGA_POKEMON, new Pokemon());

                    new FlagSpeciesFeature("mega-x", false).apply(pokemon);

                } else if (enabled && feature.getName().equals("mega-y") && (species != pokemon.getSpecies() || event.getReceived() != event.getReturned())) {
                    player.setData(DataManage.MEGA_DATA, false);
                    player.setData(DataManage.MEGA_POKEMON, new Pokemon());

                    new FlagSpeciesFeature("mega-y", false).apply(pokemon);
                }
            }
        }


        return Unit.INSTANCE;
    }

    public static Unit onReleasePokemon(ReleasePokemonEvent.Post post) {
        if(!post.getPlayer().hasData(DataManage.MEGA_POKEMON) || post.getPlayer().level().isClientSide){
            return Unit.INSTANCE;
        }
        if(!post.getPlayer().level().isClientSide && post.getPlayer().getData(DataManage.MEGA_POKEMON) == post.getPokemon()){
            post.getPlayer().setData(DataManage.MEGA_DATA, false);
            post.getPlayer().setData(DataManage.MEGA_POKEMON, new Pokemon());
        }

        return Unit.INSTANCE;
    }
}
