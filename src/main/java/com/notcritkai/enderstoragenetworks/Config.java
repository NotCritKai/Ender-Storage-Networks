package com.notcritkai.enderstoragenetworks;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import codechicken.lib.colour.EnumColour;
import codechicken.enderstorage.api.Frequency;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EventBusSubscriber(modid = EnderStorageNetworks.MODID)
public final class Config {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(EnderStorageNetworks.MODID);

    private static final ModConfigSpec.Builder BUILDER =
            new ModConfigSpec.Builder();

    private static final ModConfigSpec.ConfigValue<List<? extends String>>
            MAPPINGS = BUILDER
            .comment(
                    "Infinite EnderTank frequency-to-fluid assignments.",
                    "Format: left/middle/right=namespace:fluid_id",
                    "Example: red/red/red=minecraft:lava"
            )
            .defineListAllowEmpty(
                    "infinite_tanks.mappings",
                    List.of(
                            "red/red/red=minecraft:lava",
                            "blue/blue/blue=minecraft:water"
                    ),
                    value -> value instanceof String
            );

    public static final ModConfigSpec SPEC = BUILDER.build();

    private static volatile Map<Frequency, Fluid> infiniteTankFluids =
            Collections.emptyMap();

    private Config() {
    }

    @SubscribeEvent
    public static void onConfigLoad(final ModConfigEvent event) {
        if (event.getConfig().getSpec() == SPEC) {
            reloadMappings();
        }
    }

    public static Fluid getInfiniteFluid(final Frequency frequency) {
    if (frequency == null) {
        return null;
    }

    Frequency colorOnly = new Frequency(
            frequency.left(),
            frequency.middle(),
            frequency.right()
    );

    return infiniteTankFluids.get(colorOnly);
}



    private static void reloadMappings() {
        Map<Frequency, Fluid> loadedMappings = new HashMap<>();

        for (String mapping : MAPPINGS.get()) {
            parseMapping(mapping, loadedMappings);
        }

        infiniteTankFluids = Map.copyOf(loadedMappings);
        LOGGER.info(
                "Loaded {} infinite EnderTank fluid mapping(s).",
                infiniteTankFluids.size()
        );
    }

    private static void parseMapping(
            String mapping,
            Map<Frequency, Fluid> loadedMappings
    ) {
        String[] parts = mapping.split("=", 2);

        if (parts.length != 2) {
            LOGGER.warn(
                    "Ignoring invalid infinite EnderTank mapping '{}'. "
                            + "Expected frequency=namespace:fluid_id.",
                    mapping
            );
            return;
        }

        Frequency frequency = parseFrequency(parts[0].trim());

        if (frequency == null) {
            return;
        }

        ResourceLocation fluidId = ResourceLocation.tryParse(parts[1].trim());

        if (fluidId == null || !BuiltInRegistries.FLUID.containsKey(fluidId)) {
            LOGGER.warn(
                    "Ignoring infinite EnderTank mapping '{}': fluid '{}' "
                            + "is not registered.",
                    mapping,
                    parts[1].trim()
            );
            return;
        }

        Fluid fluid = BuiltInRegistries.FLUID.get(fluidId);
        loadedMappings.put(frequency, fluid);
    }

    private static Frequency parseFrequency(String text) {
        String[] colors = text.toLowerCase().split("/", -1);

        if (colors.length != 3) {
            LOGGER.warn(
                    "Ignoring invalid EnderTank frequency '{}'. "
                            + "Expected left/middle/right.",
                    text
            );
            return null;
        }

        try {
            return new Frequency(
                EnumColour.valueOf(colors[0].trim().toUpperCase()),
                EnumColour.valueOf(colors[1].trim().toUpperCase()),
                EnumColour.valueOf(colors[2].trim().toUpperCase())
        );

        } catch (IllegalArgumentException exception) {
            LOGGER.warn(
                    "Ignoring invalid EnderTank frequency '{}'.",
                    text
            );
            return null;
        }
    }
}
