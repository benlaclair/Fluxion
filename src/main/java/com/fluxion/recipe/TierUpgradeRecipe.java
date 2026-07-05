package com.fluxion.recipe;

import com.fluxion.registry.ModRecipeSerializers;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.util.RecipeMatcher;

import java.util.ArrayList;
import java.util.List;

/**
 * The tier-upgrade backbone: a shapeless-style crafting recipe of one "base"
 * machine item plus upgrade ingredients. The base's {@code BlockEntityTag}
 * (stored energy, settings) is copied onto the result, so upgrades never lose
 * machine state. Tier-derived caps are recomputed on placement — stored energy
 * carries over, capacity comes from the new tier.
 *
 * <p>Uses the vanilla CRAFTING recipe type (works in any crafting table, shows
 * in JEI/recipe book via {@link #getIngredients()}) with a custom serializer:
 * {@code fluxion:tier_upgrade}.
 */
public class TierUpgradeRecipe implements CraftingRecipe {
    private final ResourceLocation id;
    private final Ingredient base;
    private final NonNullList<Ingredient> addons;
    private final ItemStack result;

    public TierUpgradeRecipe(ResourceLocation id, Ingredient base, NonNullList<Ingredient> addons, ItemStack result) {
        this.id = id;
        this.base = base;
        this.addons = addons;
        this.result = result;
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        List<ItemStack> inputs = new ArrayList<>();
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) {
                inputs.add(stack);
            }
        }
        if (inputs.size() != addons.size() + 1) {
            return false;
        }
        // Try each input as the base; the rest must exactly match the addons.
        for (int i = 0; i < inputs.size(); i++) {
            if (!base.test(inputs.get(i))) {
                continue;
            }
            List<ItemStack> rest = new ArrayList<>(inputs);
            rest.remove(i);
            if (RecipeMatcher.findMatches(rest, addons) != null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        ItemStack out = result.copy();
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty() && base.test(stack)) {
                CompoundTag beTag = stack.getTagElement("BlockEntityTag");
                if (beTag != null) {
                    out.getOrCreateTag().put("BlockEntityTag", beTag.copy());
                }
                break;
            }
        }
        return out;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height > addons.size();
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return result;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> all = NonNullList.create();
        all.add(base);
        all.addAll(addons);
        return all;
    }

    public Ingredient getBase() {
        return base;
    }

    @Override
    public CraftingBookCategory category() {
        return CraftingBookCategory.MISC;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.TIER_UPGRADE.get();
    }

    public static class Serializer implements RecipeSerializer<TierUpgradeRecipe> {
        @Override
        public TierUpgradeRecipe fromJson(ResourceLocation id, JsonObject json) {
            if (!json.has("base")) {
                throw new JsonSyntaxException("Missing 'base' for tier_upgrade recipe");
            }
            Ingredient base = Ingredient.fromJson(json.get("base"));

            NonNullList<Ingredient> addons = NonNullList.create();
            for (JsonElement element : GsonHelper.getAsJsonArray(json, "ingredients")) {
                addons.add(Ingredient.fromJson(element));
            }
            if (addons.isEmpty()) {
                throw new JsonSyntaxException("No ingredients for tier_upgrade recipe");
            }
            if (addons.size() > 8) {
                throw new JsonSyntaxException("Too many ingredients for tier_upgrade recipe (max 8)");
            }

            ItemStack result = CraftingHelper.getItemStack(GsonHelper.getAsJsonObject(json, "result"), true);
            return new TierUpgradeRecipe(id, base, addons, result);
        }

        @Override
        public TierUpgradeRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            Ingredient base = Ingredient.fromNetwork(buf);
            int count = buf.readVarInt();
            NonNullList<Ingredient> addons = NonNullList.create();
            for (int i = 0; i < count; i++) {
                addons.add(Ingredient.fromNetwork(buf));
            }
            ItemStack result = buf.readItem();
            return new TierUpgradeRecipe(id, base, addons, result);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, TierUpgradeRecipe recipe) {
            recipe.base.toNetwork(buf);
            buf.writeVarInt(recipe.addons.size());
            for (Ingredient addon : recipe.addons) {
                addon.toNetwork(buf);
            }
            buf.writeItem(recipe.result);
        }
    }
}
