package de.jonas.telepads.gui;

import java.awt.Event;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import de.jonas.telepads.DataBasePool;
import de.jonas.telepads.Events;
import de.jonas.telepads.Telepads;
import me.gaminglounge.configapi.Language;
import me.gaminglounge.guiapi.GuiFromMap;
import me.gaminglounge.itembuilder.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class MainGui implements InventoryHolder {
    MiniMessage mm = MiniMessage.miniMessage();
    Telepads telepads = Telepads.INSTANCE;
    FileConfiguration conf = telepads.getConfig();
    DataBasePool db = telepads.basePool;

    public Inventory inv;

    public int id;
    public Material block;
    public Location location, destination;
    public UUID owner;
    public String destiName;
    public Component name;

    public MainGui(Player p) {
        location = DataBasePool.getlocation(db, id);
        name = mm.deserialize(DataBasePool.getName(db, id));
        owner = DataBasePool.getOwner(db, id);
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
            lore.add(mm.deserialize(Language.getValue(telepads, p, ""))); // Messages.setDestination
        }

        Map<Integer, ItemStack> items = new HashMap<>();

        items.put(0, new ItemBuilder(Material.NAME_TAG)
                .setName(mm.deserialize(Language.getValue(telepads, p, "")))
                .addBothClickEvent(Events.CHANGE_NAME)
                .build());

        items.put(1, new ItemBuilder(block)
                .setName(mm.deserialize(Language.getValue(telepads, p, "")))
                .addBothClickEvent(Events.CHANGE_BLOCK)
                .build());

        items.put(2, new ItemBuilder(Material.RED_DYE)
                .setName(mm.deserialize(Language.getValue(telepads, p, ""))) // TelepadGUI.pickup.name
                .addBothClickEvent(Events.PICKUP)
                .build());

        items.put(3, new ItemBuilder(Material.ENDER_EYE)
                .setName(mm.deserialize(Language.getValue(telepads, p, ""))) // TelepadGUI.publicity.name
                .addBothClickEvent(Events.PUBLISH_GUI)
                .build());

        inv = new GuiFromMap(this, 6).setItems(items).getInventory();

    }

    @Override
    public @NotNull Inventory getInventory() {
        return inv;
    }

}
