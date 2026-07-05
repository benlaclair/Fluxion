package com.fluxion.registry;

import com.fluxion.Fluxion;
import com.fluxion.recipe.TierUpgradeRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Fluxion.MOD_ID);

    public static final RegistryObject<RecipeSerializer<TierUpgradeRecipe>> TIER_UPGRADE =
            RECIPE_SERIALIZERS.register("tier_upgrade", TierUpgradeRecipe.Serializer::new);

    private ModRecipeSerializers() {
    }
}
