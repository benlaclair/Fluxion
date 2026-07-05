package com.fluxion.datagen;

import com.fluxion.registry.ModRecipeSerializers;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/** Datagen builder emitting {@code fluxion:tier_upgrade} recipe JSON. */
public class TierUpgradeRecipeBuilder {
    private final Ingredient base;
    private final ItemLike result;
    private final List<Ingredient> addons = new ArrayList<>();

    private TierUpgradeRecipeBuilder(ItemLike base, ItemLike result) {
        this.base = Ingredient.of(base);
        this.result = result;
    }

    public static TierUpgradeRecipeBuilder upgrade(ItemLike base, ItemLike result) {
        return new TierUpgradeRecipeBuilder(base, result);
    }

    public TierUpgradeRecipeBuilder addIngredient(ItemLike item) {
        return addIngredient(item, 1);
    }

    public TierUpgradeRecipeBuilder addIngredient(ItemLike item, int count) {
        for (int i = 0; i < count; i++) {
            addons.add(Ingredient.of(item));
        }
        return this;
    }

    public void save(java.util.function.Consumer<FinishedRecipe> writer, ResourceLocation id) {
        if (addons.isEmpty()) {
            throw new IllegalStateException("Tier upgrade recipe " + id + " has no ingredients");
        }
        writer.accept(new Result(id, base, List.copyOf(addons), result));
    }

    private record Result(ResourceLocation id, Ingredient base, List<Ingredient> addons,
                          ItemLike result) implements FinishedRecipe {
        @Override
        public void serializeRecipeData(JsonObject json) {
            json.add("base", base.toJson());
            JsonArray ingredients = new JsonArray();
            for (Ingredient addon : addons) {
                ingredients.add(addon.toJson());
            }
            json.add("ingredients", ingredients);
            JsonObject resultObj = new JsonObject();
            resultObj.addProperty("item",
                    ForgeRegistries.ITEMS.getKey(result.asItem()).toString());
            json.add("result", resultObj);
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }

        @Override
        public RecipeSerializer<?> getType() {
            return ModRecipeSerializers.TIER_UPGRADE.get();
        }

        @Nullable
        @Override
        public JsonObject serializeAdvancement() {
            return null;
        }

        @Nullable
        @Override
        public ResourceLocation getAdvancementId() {
            return null;
        }
    }
}
