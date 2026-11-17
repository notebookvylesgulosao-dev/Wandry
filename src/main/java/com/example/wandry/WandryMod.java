package com.example.wandry;

import net.fabricmc.api.ModInitializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public final class WandryMod implements ModInitializer {
    public static final String MOD_ID = "wandry";

    public static final Item MAGMA_WAND = register("magma_wand", new WandItem(
            properties("magma_wand"), WandItem.Spell.MAGMA));
    public static final Item BONE_WAND = register("bone_wand", new WandItem(
            properties("bone_wand"), WandItem.Spell.BONE));
    public static final Item CELESTIAL_WAND = register("celestial_wand", new WandItem(
            properties("celestial_wand"), WandItem.Spell.CELESTIAL));

    public static final Item MAGMA_ORB = register("magma_orb", new Item(properties("magma_orb")));
    public static final Item BONE_ORB = register("bone_orb", new Item(properties("bone_orb")));
    public static final Item CELESTIAL_ORB = register("celestial_orb", new Item(properties("celestial_orb")));

    private static Item.Properties properties(String path) {
        Identifier id = Identifier.fromNamespaceAndPath(MOD_ID, path);
        return new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id));
    }

    private static Item register(String path, Item item) {
        return Registry.register(BuiltInRegistries.ITEM,
                Identifier.fromNamespaceAndPath(MOD_ID, path), item);
    }

    @Override
    public void onInitialize() {
        // All items are registered in the static declarations above.
    }
}
