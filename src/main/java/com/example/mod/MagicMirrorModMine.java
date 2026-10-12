package com.example.mod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MagicMirrorModMine implements ModInitializer {
    public static final String MOD_ID = "magic-mirror-mod-mine";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final SoundEvent MIRROR_USE_SOUND = Registry.register(
            BuiltInRegistries.SOUND_EVENT,
            Identifier.fromNamespaceAndPath(MOD_ID, "magic-mirror"),
            SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(MOD_ID, "magic-mirror"))
    );
    public static Item SIMPLE_MAGIC_MIRROR;
    public static Item SUPER_MAGIC_MIRROR;

    @Override
    public void onInitialize() {
        Identifier simpleId = Identifier.fromNamespaceAndPath(MOD_ID, "simple_magic_mirror");
        ResourceKey<Item> simpleKey = ResourceKey.create(Registries.ITEM, simpleId);
        Item.Properties simpleProps = new Item.Properties().setId(simpleKey);
        SimpleMagicMirrorItem simpleItem = new SimpleMagicMirrorItem(simpleProps);
        SIMPLE_MAGIC_MIRROR = Registry.register(BuiltInRegistries.ITEM, simpleKey, simpleItem);

        Identifier superId = Identifier.fromNamespaceAndPath(MOD_ID, "super_magic_mirror");
        ResourceKey<Item> superKey = ResourceKey.create(Registries.ITEM, superId);
        Item.Properties SuperProps = new Item.Properties().setId(superKey);
        SuperMagicMirrorItem superItem = new SuperMagicMirrorItem(SuperProps);
        SUPER_MAGIC_MIRROR = Registry.register(BuiltInRegistries.ITEM, superKey, superItem);

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
                .register(content -> {
                    content.accept(SIMPLE_MAGIC_MIRROR);
                    content.accept(SUPER_MAGIC_MIRROR);
                });

        LOGGER.info("magic-mirror-mod-mine - initialized successfully!");
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
