package de.jonas.telepads.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import de.jonas.telepads.DataBasePool;
import de.jonas.telepads.Telepads;
import me.gaminglounge.configapi.Language;
import me.gaminglounge.itembuilder.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class CustomizeGUI implements InventoryHolder {
    Telepads telepads = Telepads.INSTANCE;
    FileConfiguration conf = telepads.getConfig();

    Inventory inv;
    public int id, blockID;
    String pl, name;

    public CustomizeGUI(int id, int level, Player player) {
        DataBasePool db = Telepads.INSTANCE.basePool;
        name = DataBasePool.getName(db, id);
        String blockID = DataBasePool.getBlockID(db, id);
        Material block;
        if (blockID == null) {
            block = Material.BEACON;
        } else {
            block = Material.getMaterial(blockID.toUpperCase());
        }

        this.id = id;

        inv = Bukkit.createInventory(this, (9 * 5), Component.text(""));

        int[] place = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 13, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27,
                28, 29, 30, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44 };
        for (int a : place) {
            inv.setItem(a,
                    new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                            .setName(Component.empty())
                            .addBothClickEvent("telepads:cancelevent")
                            .build());
        }

        inv.setItem(12,
                new ItemBuilder(Material.getMaterial("NAME_TAG"))
                        .setName(MiniMessage.miniMessage()
                                .deserialize(Language.getValue(telepads, player, "telepad.gui.change.name")))
                        .addBothClickEvent("telepads:click_change_name")
                        .build());

        inv.setItem(14,
                new ItemBuilder(block)
                        .setName(MiniMessage.miniMessage()
                                .deserialize(Language.getValue(telepads, player, "telepad.gui.change.block")))
                        .addBothClickEvent("telepads:click_block")
                        .build());

        inv.setItem(31,
                new ItemBuilder(Material.BARRIER)
                        .setName(MiniMessage.miniMessage().
                                        deserialize(Language.getValue(telepads, player, "close")))
                        .addBothClickEvent("telepads:open_telepad_gui")
                        .build());

    }

    @Override
    public @NotNull Inventory getInventory() {
        return inv;
    }

}
