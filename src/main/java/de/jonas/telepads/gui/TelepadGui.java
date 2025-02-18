package de.jonas.telepads.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import de.jonas.telepads.DataBasePool;
import de.jonas.telepads.Telepads;
import me.gaminglounge.itembuilder.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

public class TelepadGui implements InventoryHolder {
    MiniMessage mm = MiniMessage.miniMessage();
    Telepads telepads = Telepads.INSTANCE;
    FileConfiguration conf = telepads.getConfig();
    DataBasePool db = telepads.basePool;
    Location location, destination;
    public int id;
    UUID owner;
    public int level;
    public Inventory telepadGui;
    String destiName;

    public static final NamespacedKey desti = new NamespacedKey("telepads", "destination");
    public static final NamespacedKey src = new NamespacedKey("telepads", "source");

    public TelepadGui(Player p, int idC) {
        MiniMessage mm = MiniMessage.miniMessage();

        id = idC;
        location = DataBasePool.getlocation(db, id);
        String name = DataBasePool.getName(db, id);
        owner = DataBasePool.getOwner(db, id);
        level = DataBasePool.getLevel(db, id);
        destiName = DataBasePool.getDestinationName(db, id);
        destination = DataBasePool.getDestination(db, id);
        List<Component> lore = new ArrayList<>();
        if (destination != null) {
            lore.add(mm.deserialize("<white>" + destiName + "</white>").decoration(TextDecoration.ITALIC, false));
            lore.add(mm.deserialize("<white>" + destination.getWorld().getName() + "</white>")
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(mm.deserialize("<white>X: " + destination.getBlockX() + "</white>")
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(mm.deserialize("<white>Y: " + destination.getBlockY() + "</white>")
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(mm.deserialize("<white>Z: " + destination.getBlockZ() + "</white>")
                    .decoration(TextDecoration.ITALIC, false));
        } else {
            lore.add(mm.deserialize(conf.getString("Messages.setDestination")));
        }

        this.telepadGui = Bukkit.createInventory(this, (3 * 9), mm.deserialize(name));

        int[] place = { 0, 1, 2, 3, 5, 6, 7, 8, 9, 11, 13, 15, 17, 18, 19, 20, 21, 23, 24, 25, 26 };
        for (int a : place) {
            telepadGui.setItem(a,
                    new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                            .setName(Component.empty())
                            .addBothClickEvent("telepads:cancelevent")
                            .build());
        }

        telepadGui.setItem(4,
                new ItemBuilder(Material.ANVIL)
                        .setName(MiniMessage.miniMessage().deserialize(conf.getString("TelepadGUI.customizer.name")))
                        .addLoreLine(
                                MiniMessage.miniMessage().deserialize(conf.getString("TelepadGUI.customizer.lore")))
                        .addBothClickEvent("telepads:open_customizer_gui")
                        .build());

        telepadGui.setItem(14,
                new ItemBuilder(Material.GRASS_BLOCK)
                        .setName(MiniMessage.miniMessage().deserialize(conf.getString("TelepadGUI.destination.name")))
                        .setLore(lore)
                        .addBothClickEvent("telepad:open_initial_destination_gui")
                        .build());

        telepadGui.setItem(10,
                new ItemBuilder(Material.RED_DYE)
                        .setName(MiniMessage.miniMessage().deserialize(conf.getString("TelepadGUI.pickup.name")))
                        .addBothClickEvent("telepads:pick_telepad_up")
                        .build());

        telepadGui.setItem(12,
                new ItemBuilder(Material.ENDER_EYE)
                        .setName(MiniMessage.miniMessage().deserialize(conf.getString("TelepadGUI.publicity.name")))
                        .addBothClickEvent("telepads:open_publish_gui")
                        .build());

        telepadGui.setItem(22,
                new ItemBuilder(Material.BARRIER)
                        .setName(MiniMessage.miniMessage().deserialize(conf.getString("CommonPage.close")))
                        .addBothClickEvent("telepads:closeinv")
                        .build());

        String levelLore;
        if (level == 1) {
            levelLore = conf.getString("Messages.upgrade");
        } else {
            levelLore = "";
        }

        telepadGui.setItem(16,
                new ItemBuilder(Material.EMERALD)
                        .setName(MiniMessage.miniMessage().deserialize(conf.getString("TelepadGUI.levelup.name")))
                        .addLoreLine(MiniMessage.miniMessage().deserialize("Level: " + level))
                        .addLoreLine(mm.deserialize(levelLore,
                                Placeholder.component("cost",
                                        Component.text(conf.getDouble("TelepadGUI.levelup.cost"))))
                                .decoration(TextDecoration.ITALIC, false))
                        .addBothClickEvent("telepad:pad_level_up")
                        .build());
    }

    @Override
    public @NotNull Inventory getInventory() {
        return telepadGui;
    }

}
