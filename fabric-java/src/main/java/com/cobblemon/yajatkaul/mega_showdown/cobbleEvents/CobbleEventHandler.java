package com.cobblemon.yajatkaul.mega_showdown.cobbleEvents;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.events.battles.*;
import com.cobblemon.mod.common.api.events.battles.instruction.MegaEvolutionEvent;
import com.cobblemon.mod.common.api.events.battles.instruction.ZMoveUsedEvent;
import com.cobblemon.mod.common.api.events.pokemon.HeldItemEvent;
import com.cobblemon.mod.common.api.events.pokemon.TradeCompletedEvent;
import com.cobblemon.mod.common.api.events.storage.ReleasePokemonEvent;
import com.cobblemon.mod.common.api.pokemon.feature.FlagSpeciesFeature;
import com.cobblemon.mod.common.api.pokemon.feature.FlagSpeciesFeatureProvider;
import com.cobblemon.mod.common.api.pokemon.feature.SpeciesFeature;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.api.storage.player.GeneralPlayerData;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.net.messages.client.battle.BattleUpdateTeamPokemonPacket;
import com.cobblemon.mod.common.net.messages.client.pokemon.update.AbilityUpdatePacket;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import com.cobblemon.yajatkaul.mega_showdown.MegaShowdown;
import com.cobblemon.yajatkaul.mega_showdown.advancement.AdvancementHelper;
import com.cobblemon.yajatkaul.mega_showdown.config.ShowdownConfig;
import com.cobblemon.yajatkaul.mega_showdown.datamanage.DataManage;
import com.cobblemon.yajatkaul.mega_showdown.item.MegaStones;
import com.cobblemon.yajatkaul.mega_showdown.item.ZMoves;
import com.cobblemon.yajatkaul.mega_showdown.item.custom.MegaBraceletItem;
import com.cobblemon.yajatkaul.mega_showdown.item.custom.ZRingItem;
import com.cobblemon.yajatkaul.mega_showdown.megaevo.MegaLogic;
import com.cobblemon.yajatkaul.mega_showdown.utility.Utils;
import dev.emi.trinkets.api.TrinketsApi;
import kotlin.Unit;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class CobbleEventHandler {
    public static Unit onMegaTraded(TradeCompletedEvent tradeCompletedEvent) {
        if(!ShowdownConfig.multipleMegas.get()){
            ServerPlayerEntity player1 = tradeCompletedEvent.getTradeParticipant1Pokemon().getOwnerPlayer();
            ServerPlayerEntity player2 = tradeCompletedEvent.getTradeParticipant2Pokemon().getOwnerPlayer();

            if(player1 == null || player2 == null || player2.getWorld().isClient || player1.getWorld().isClient){
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
                player1.setAttached(DataManage.MEGA_DATA, false);
                player1.setAttached(DataManage.MEGA_POKEMON, new Pokemon());
                DevolveOnTrade(pokemon1);
            }
            if(mega2){
                player2.setAttached(DataManage.MEGA_DATA, false);
                player2.setAttached(DataManage.MEGA_POKEMON, new Pokemon());
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
        // Battle mode only
        if(ShowdownConfig.battleModeOnly.get()){
            return Unit.INSTANCE;
        }
        Pokemon pokemon = event.getPokemon();

        if(pokemon.getEntity() == null){
            return Unit.INSTANCE;
        }

        if(pokemon.getEntity().getWorld().isClient){
            return Unit.INSTANCE;
        }

        Species species = Utils.MEGA_STONE_IDS.get(pokemon.heldItem().getItem());


        List<String> megaKeys = List.of("mega-x", "mega-y", "mega");

        for (String key : megaKeys) {
            FlagSpeciesFeatureProvider featureProvider = new FlagSpeciesFeatureProvider(List.of(key));
            ServerPlayerEntity player = pokemon.getOwnerPlayer();

            FlagSpeciesFeature feature = featureProvider.get(pokemon);
            if(feature != null){
                boolean enabled = featureProvider.get(pokemon).getEnabled();

                if (enabled && feature.getName().equals("mega") && (species != pokemon.getSpecies() || event.getReceived() != event.getReturned())) {
                    player.setAttached(DataManage.MEGA_DATA, false);
                    player.setAttached(DataManage.MEGA_POKEMON, null);

                    new FlagSpeciesFeature("mega", false).apply(pokemon);

                }else if(enabled && feature.getName().equals("mega-x") && (species != pokemon.getSpecies() || event.getReceived() != event.getReturned())){
                    player.setAttached(DataManage.MEGA_DATA, false);
                    player.setAttached(DataManage.MEGA_POKEMON, null);

                    new FlagSpeciesFeature("mega-x", false).apply(pokemon);

                } else if (enabled && feature.getName().equals("mega-y") && (species != pokemon.getSpecies() || event.getReceived() != event.getReturned())) {
                    player.setAttached(DataManage.MEGA_DATA, false);
                    player.setAttached(DataManage.MEGA_POKEMON, null);

                    new FlagSpeciesFeature("mega-y", false).apply(pokemon);
                }
            }
        }

        return Unit.INSTANCE;
    }

    public static Unit onReleasePokemon(ReleasePokemonEvent.Post post) {
        if(post.getPlayer().getWorld().isClient){
            return Unit.INSTANCE;
        }

        if(!post.getPlayer().hasAttached(DataManage.PRIMAL_POKEMON)){
            post.getPlayer().setAttached(DataManage.PRIMAL_POKEMON, new Pokemon());
        }

        if(!post.getPlayer().hasAttached(DataManage.MEGA_POKEMON)){
            post.getPlayer().setAttached(DataManage.MEGA_POKEMON, new Pokemon());
        }

        if(post.getPlayer().getAttached(DataManage.MEGA_POKEMON) == post.getPokemon()){
            post.getPlayer().removeAttached(DataManage.MEGA_DATA);
            post.getPlayer().removeAttached(DataManage.MEGA_POKEMON);
        }

        if(post.getPlayer().getAttached(DataManage.PRIMAL_POKEMON) == post.getPokemon()){
            post.getPlayer().setAttached(DataManage.PRIMAL_DATA, false);
            post.getPlayer().setAttached(DataManage.PRIMAL_POKEMON, new Pokemon());
        }

        return Unit.INSTANCE;
    }

    public static Unit primalEvent(HeldItemEvent.Post post) {
        if(post.getReturned() == post.getReceived() || post.getPokemon().getOwnerPlayer() == null){
            return Unit.INSTANCE;
        }

        ServerPlayerEntity player = post.getPokemon().getOwnerPlayer();
        Species species = post.getPokemon().getSpecies();

        if(species.getName().equals(Utils.getSpecies("kyogre").getName()) && post.getReceived().isOf(MegaStones.BLUE_ORB)){
            if(player.getAttached(DataManage.PRIMAL_DATA) && !ShowdownConfig.multiplePrimals.get()){
                player.sendMessage(
                        Text.literal("You can only have one primal at a time").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFF0000))),
                        true
                );
                return Unit.INSTANCE;
            }
            new FlagSpeciesFeature("primal", true).apply(post.getPokemon());
            primalRevertAnimation(post.getPokemon().getEntity(), ParticleTypes.BUBBLE);
            AdvancementHelper.grantAdvancement(player, "primal_evo");
            player.setAttached(DataManage.PRIMAL_DATA, true);
        }
        else if(species.getName().equals(Utils.getSpecies("groudon").getName()) && post.getReceived().isOf(MegaStones.RED_ORB)){
            if(player.getAttached(DataManage.PRIMAL_DATA) && !ShowdownConfig.multiplePrimals.get()){
                player.sendMessage(
                        Text.literal("You can only have one primal at a time").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFF0000))),
                        true
                );
                return Unit.INSTANCE;
            }
            new FlagSpeciesFeature("primal", true).apply(post.getPokemon());
            primalRevertAnimation(post.getPokemon().getEntity(), ParticleTypes.CAMPFIRE_COSY_SMOKE);
            AdvancementHelper.grantAdvancement(player, "primal_evo");
            player.setAttached(DataManage.PRIMAL_DATA, true);
        }else{
            SpeciesFeature feature = post.getPokemon().getFeature("primal");
            if(feature == null){
                return Unit.INSTANCE;
            }

            new FlagSpeciesFeature("primal", false).apply(post.getPokemon());
            primalRevertAnimation(post.getPokemon().getEntity(), ParticleTypes.END_ROD);
            player.setAttached(DataManage.PRIMAL_DATA, false);
        }

        return Unit.INSTANCE;
    }

    public static void primalRevertAnimation(LivingEntity context, SimpleParticleType particleType) {
        if (context.getWorld() instanceof ServerWorld serverWorld) {
            Vec3d entityPos = context.getPos(); // Get entity position

            // Get entity's size
            double entityWidth = context.getWidth();
            double entityHeight = context.getHeight();
            double entityDepth = entityWidth; // Usually same as width for most mobs

            // Scaling factor to slightly expand particle spread beyond the entity's bounding box
            double scaleFactor = 1.2; // Adjust this for more spread
            double adjustedWidth = entityWidth * scaleFactor;
            double adjustedHeight = entityHeight * scaleFactor;
            double adjustedDepth = entityDepth * scaleFactor;

            // Play sound effect
            serverWorld.playSound(
                    null, entityPos.x, entityPos.y, entityPos.z,
                    SoundEvents.BLOCK_BEACON_ACTIVATE, // Change this if needed
                    SoundCategory.PLAYERS, 1.5f, 0.5f + (float) Math.random() * 0.5f
            );

            // Adjust particle effect based on entity size
            int particleCount = (int) (175 * adjustedWidth * adjustedHeight); // Scale particle amount

            for (int i = 0; i < particleCount; i++) {
                double xOffset = (Math.random() - 0.5) * adjustedWidth; // Random X within slightly expanded bounding box
                double yOffset = Math.random() * adjustedHeight; // Random Y within slightly expanded bounding box
                double zOffset = (Math.random() - 0.5) * adjustedDepth; // Random Z within slightly expanded bounding box

                serverWorld.spawnParticles(
                        particleType,
                        entityPos.x + xOffset,
                        entityPos.y + yOffset,
                        entityPos.z + zOffset,
                        1, // One particle per call for better spread
                        0, 0, 0, // No movement velocity
                        0.1 // Slight motion
                );
            }
        }
    }

    public static Unit getBattleEndInfo(BattleVictoryEvent battleVictoryEvent) {
        battleVictoryEvent.getBattle().getPlayers().forEach(serverPlayer -> {
            PlayerPartyStore playerPartyStore = Cobblemon.INSTANCE.getStorage().getParty(serverPlayer);
            for (Pokemon pokemon: playerPartyStore){
                if(pokemon.getEntity() != null){
                    pokemon.getEntity().removeStatusEffect(StatusEffects.GLOWING);
                }
            }

            for (BattlePokemon battlePokemon : battleVictoryEvent.getBattle().getActor(serverPlayer.getUuid()).getPokemonList()) {
                if (battlePokemon.getOriginalPokemon().getEntity() == null ||
                        battlePokemon.getOriginalPokemon().getEntity().getWorld().isClient) {
                    continue;
                }

                Pokemon pokemon = battlePokemon.getOriginalPokemon();

                List<String> megaKeys = List.of("mega-x", "mega-y", "mega");

                for (String key : megaKeys) {
                    FlagSpeciesFeatureProvider featureProvider = new FlagSpeciesFeatureProvider(List.of(key));
                    FlagSpeciesFeature feature = featureProvider.get(pokemon);

                    if(feature != null){
                        boolean enabled = featureProvider.get(pokemon).getEnabled();

                        if(enabled){
                            MegaLogic.Devolve(pokemon.getEntity(), serverPlayer, true);

                            if(!ShowdownConfig.multipleMegas.get()){
                                break;
                            }
                        }
                    }
                }
            }
        });

        return Unit.INSTANCE;
    }

    public static Unit devolveFainted(BattleFaintedEvent battleFaintedEvent) {
        Pokemon pokemon = battleFaintedEvent.getKilled().getOriginalPokemon();
        ServerPlayerEntity serverPlayer = battleFaintedEvent.getKilled().getOriginalPokemon().getOwnerPlayer();

        if(serverPlayer == null || serverPlayer.getWorld().isClient){
            return Unit.INSTANCE;
        }

        MegaLogic.Devolve(pokemon.getEntity(), serverPlayer, true);

        return Unit.INSTANCE;
    }

    public static Unit deVolveFlee(BattleFledEvent battleFledEvent) {
        battleFledEvent.getBattle().getPlayers().forEach(serverPlayer -> {
            for (BattlePokemon battlePokemon : battleFledEvent.getBattle().getActor(serverPlayer.getUuid()).getPokemonList()) {
                if (battlePokemon.getOriginalPokemon().getEntity() == null ||
                        battlePokemon.getOriginalPokemon().getEntity().getWorld().isClient) {
                    continue;
                }

                Pokemon pokemon = battlePokemon.getOriginalPokemon();

                List<String> megaKeys = List.of("mega-x", "mega-y", "mega");

                for (String key : megaKeys) {
                    FlagSpeciesFeatureProvider featureProvider = new FlagSpeciesFeatureProvider(List.of(key));
                    FlagSpeciesFeature feature = featureProvider.get(pokemon);

                    if(feature != null){
                        boolean enabled = featureProvider.get(pokemon).getEnabled();

                        if(enabled){
                            MegaLogic.Devolve(pokemon.getEntity(), serverPlayer, true);

                            if(!ShowdownConfig.multipleMegas.get()){
                                break;
                            }
                        }
                    }
                }
            }

        });

        return Unit.INSTANCE;
    }

    public static Unit battleStarted(BattleStartedPreEvent battleEvent) {
        for(ServerPlayerEntity player: battleEvent.getBattle().getPlayers()){
            if(ShowdownConfig.battleMode.get()){
                PlayerPartyStore playerPartyStore = Cobblemon.INSTANCE.getStorage().getParty(player);

                for (Pokemon pokemon : playerPartyStore) {
                    if(pokemon.getSpecies().getName().equals("Rayquaza")){
                        continue;
                    }
                    List<String> megaKeys = List.of("mega-x", "mega-y", "mega");

                    for (String key : megaKeys) {
                        FlagSpeciesFeatureProvider featureProvider = new FlagSpeciesFeatureProvider(List.of(key));
                        FlagSpeciesFeature feature = featureProvider.get(pokemon);

                        if(feature != null){
                            boolean enabled = featureProvider.get(pokemon).getEnabled();

                            if(enabled){
                                MegaLogic.Devolve(pokemon.getEntity(), player, true);

                                if(!ShowdownConfig.multipleMegas.get()){
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            GeneralPlayerData data = Cobblemon.INSTANCE.getPlayerDataManager().getGenericData(player);

            if((ShowdownConfig.scuffedMode.get() || ShowdownConfig.battleMode.get() || ShowdownConfig.battleModeOnly.get()) && MegaLogic.Possible(player, true) && (player.getAttached(DataManage.MEGA_DATA) == null || !player.getAttached(DataManage.MEGA_DATA))){
                data.getKeyItems().add(Identifier.of("cobblemon","key_stone"));
            }else{
                data.getKeyItems().remove(Identifier.of("cobblemon","key_stone"));
            }

            boolean hasZItemTrinkets = TrinketsApi.getTrinketComponent(player).map(trinkets ->
                    trinkets.isEquipped(item -> item.getItem() instanceof ZRingItem)).orElse(false);

            if((player.getOffHandStack().isOf(ZMoves.Z_RING) || hasZItemTrinkets) && ShowdownConfig.zMoves.get()){
                data.getKeyItems().add(Identifier.of("cobblemon","z_ring"));
            }else{
                data.getKeyItems().remove(Identifier.of("cobblemon","z_ring"));
            }
        }

        return Unit.INSTANCE;
    }

    public static Unit megaEvolution(MegaEvolutionEvent megaEvolutionEvent) {
        PokemonBattle battle = megaEvolutionEvent.getBattle();
        Pokemon pokemon = megaEvolutionEvent.getPokemon().getEffectedPokemon();

        ServerPlayerEntity player = megaEvolutionEvent.getPokemon().getOriginalPokemon().getOwnerPlayer();

        if(player == null){
            return Unit.INSTANCE;
        }

        MegaLogic.Evolve(pokemon.getEntity(), player, true);

        battle.sendUpdate(new AbilityUpdatePacket(megaEvolutionEvent.getPokemon()::getEffectedPokemon, pokemon.getAbility().getTemplate()));
        battle.sendUpdate(new BattleUpdateTeamPokemonPacket(pokemon));

        return Unit.INSTANCE;
    }

    public static Unit zMovesUsed(ZMoveUsedEvent zMoveUsedEvent) {
        LivingEntity pokemon = zMoveUsedEvent.getPokemon().getEffectedPokemon().getEntity();

        pokemon.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, Integer.MAX_VALUE, 0,false, false));

        World world = pokemon.getWorld();
        Scoreboard scoreboard = world.getScoreboard();

        // Create or get the yellow team
        Team yellowTeam = scoreboard.getTeam("yellowGlow");
        if (yellowTeam == null) {
            yellowTeam = scoreboard.addTeam("yellowGlow");
            yellowTeam.setColor(Formatting.YELLOW); // Using Formatting instead of TextFormatting
        }

        // Add the entity to the yellow team
        scoreboard.addScoreHolderToTeam(pokemon.getUuid().toString(), yellowTeam);
        return Unit.INSTANCE;
    }
}
