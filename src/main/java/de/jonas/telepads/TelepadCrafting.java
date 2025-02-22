package de.jonas.telepads;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;

import me.gaminglounge.itembuilder.ItemBuilder;
import net.kyori.adventure.text.Component;

public final class TelepadCrafting {

    public static final ItemStack telepad = new ItemBuilder(Material.BEACON)
            .setName(Component.text("Telepad"))
            .addBlockPlaceEvent("telepads:buildTelepad")
            .build();

    public TelepadCrafting() {
        ShapelessRecipe recipe = new ShapelessRecipe(
                new NamespacedKey("telepads", "telepad"),
                telepad);

        recipe.addIngredient(1, Material.ENDER_EYE);
        recipe.addIngredient(1, Material.DIAMOND_BLOCK);
        recipe.addIngredient(1, Material.ENDER_EYE);
        recipe.addIngredient(4, Material.GLASS);
        recipe.addIngredient(3, Material.OBSIDIAN);

        Bukkit.getServer().addRecipe(recipe);
        Bukkit.updateRecipes();

    }

}
