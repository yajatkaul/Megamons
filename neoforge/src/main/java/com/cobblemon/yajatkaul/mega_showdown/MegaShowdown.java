package com.cobblemon.yajatkaul.mega_showdown;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.pokemon.feature.FlagSpeciesFeature;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.battles.runner.ShowdownService;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.yajatkaul.mega_showdown.battle.BattleHandling;
import com.cobblemon.yajatkaul.mega_showdown.battle.ButtonLogic;
import com.cobblemon.yajatkaul.mega_showdown.networking.NetworkHandler;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.cobblemon.yajatkaul.mega_showdown.block.ModBlocks;
import com.cobblemon.yajatkaul.mega_showdown.datamanage.DataManage;
import com.cobblemon.yajatkaul.mega_showdown.showdown.ShowdownUtils;
import com.cobblemon.yajatkaul.mega_showdown.item.ModCreativeModeTabs;
import com.cobblemon.yajatkaul.mega_showdown.item.ModItems;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

@Mod(MegaShowdown.MOD_ID)
public final class MegaShowdown {
    public static final Logger LOGGER = LoggerFactory.getLogger("Mega Showdown");
    public static final String MOD_ID = "mega_showdown";

    public MegaShowdown(IEventBus modEventBus, @NotNull ModContainer modContainer) {
        NeoForge.EVENT_BUS.register(this);

        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);

        DataManage.register(modEventBus);

        ModCreativeModeTabs.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        modEventBus.addListener(NetworkHandler::register);
    }


    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        ShowdownUtils.loadMegaStoneIds();
        CobblemonEvents.HELD_ITEM_POST.subscribe(Priority.NORMAL, ShowdownUtils::onHeldItemChange);
        CobblemonEvents.POKEMON_RELEASED_EVENT_POST.subscribe(Priority.NORMAL, ShowdownUtils::onReleasePokemon);

        CobblemonEvents.BATTLE_FAINTED.subscribe(Priority.NORMAL, BattleHandling::devolveFainted);
        CobblemonEvents.TRADE_COMPLETED.subscribe(Priority.NORMAL, ShowdownUtils::onMegaTraded);

        // Battle mode only
        if(Config.battleMode){
            CobblemonEvents.BATTLE_STARTED_POST.subscribe(Priority.NORMAL, BattleHandling::getBattleInfo);
            CobblemonEvents.BATTLE_VICTORY.subscribe(Priority.NORMAL, BattleHandling::getBattleEndInfo);
            CobblemonEvents.BATTLE_FLED.subscribe(Priority.NORMAL, BattleHandling::deVolveFlee);
        }
    }

    @SubscribeEvent
    private void onServerJoin(PlayerEvent.PlayerLoggedInEvent playerLoggedInEvent) {
        if(Config.battleMode){
            if(playerLoggedInEvent.getEntity() instanceof ServerPlayer player){
                PlayerPartyStore playerPartyStore = Cobblemon.INSTANCE.getStorage().getParty(player);

                player.removeData(DataManage.MEGA_DATA);
                player.removeData(DataManage.BATTLE_ID);

                BattleHandling.battlePokemonUsed.clear();

                for (Pokemon pokemon : playerPartyStore) {
                    new FlagSpeciesFeature("mega", false).apply(pokemon);
                    new FlagSpeciesFeature("mega-x", false).apply(pokemon);
                    new FlagSpeciesFeature("mega-y", false).apply(pokemon);
                }
            }
        }
    }


    @SubscribeEvent
    private void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event){
        if(Config.battleMode){
            if(event.getEntity() instanceof ServerPlayer player){
                PlayerPartyStore playerPartyStore = Cobblemon.INSTANCE.getStorage().getParty(player);

                player.removeData(DataManage.MEGA_DATA);
                player.removeData(DataManage.BATTLE_ID);

                BattleHandling.battlePokemonUsed.clear();

                for (Pokemon pokemon : playerPartyStore) {
                    new FlagSpeciesFeature("mega", false).apply(pokemon);
                    new FlagSpeciesFeature("mega-x", false).apply(pokemon);
                    new FlagSpeciesFeature("mega-y", false).apply(pokemon);
                }
            }
        }
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            NeoForge.EVENT_BUS.addListener(ButtonLogic::megaEvoButton);
        }
    }
}