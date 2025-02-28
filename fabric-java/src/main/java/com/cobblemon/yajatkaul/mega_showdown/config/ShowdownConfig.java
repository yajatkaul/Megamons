package com.cobblemon.yajatkaul.mega_showdown.config;

import com.cobblemon.yajatkaul.mega_showdown.MegaShowdown;
import com.supermartijn642.configlib.api.ConfigBuilders;
import com.supermartijn642.configlib.api.IConfigBuilder;

import java.util.function.Supplier;

public class ShowdownConfig {
    public static final Supplier<Boolean> multipleMegas;
    public static final Supplier<Boolean> battleModeOnly;
    public static final Supplier<Boolean> battleMode;
    public static final Supplier<Boolean> multiplePrimals;

    static{
        // construct a new config builder
        IConfigBuilder builder = ConfigBuilders.newTomlConfig(MegaShowdown.MOD_ID, "common", false);

        // a boolean value
        multipleMegas = builder.comment("Enable multiple megas at one time").define("multipleMegas", false);
        battleModeOnly = builder.comment("Enable mega evolution only for battles").define("battleModeOnly", false);
        battleMode = builder.comment("Allows you to have outside megas but they devolve on battle and then you can have battle mode style theme").define("battleMode", true);
        multiplePrimals = builder.comment("Allows you to have multiple primals at a time").define("multiplePrimals", true);

        builder.build();
    }
}
