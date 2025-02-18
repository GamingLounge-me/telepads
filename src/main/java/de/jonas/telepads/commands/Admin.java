package de.jonas.telepads.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import de.jonas.telepads.DataBasePool;
import de.jonas.telepads.Events;
import de.jonas.telepads.Telepads;
import dev.jorel.commandapi.CommandAPICommand;
import me.gaminglounge.guiapi.Pagenation;
import me.gaminglounge.itembuilder.ItemBuilder;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class Admin {

    public Admin() {
        Telepads telepads = Telepads.INSTANCE;
        FileConfiguration conf = telepads.getConfig();

        new CommandAPICommand("telepads:admin")
                .withPermission(conf.getString("AdminPermission"))
                .executesPlayer((player, arg) -> {
                    player.openInventory(new Pagenation(player).setItems(getItems()).getInventory());
                })
                .register();
    }

    public static List<ItemStack> getItems() {
        DataBasePool db = Telepads.INSTANCE.basePool;
        List<Integer> pads = DataBasePool.getAllTelepads(db);

        List<ItemStack> list = new ArrayList<>();
        if (pads != null) {
            for (int a : pads) {
                String name = DataBasePool.getName(db, a);
                ItemStack item = new ItemBuilder(Material.BEACON)
                        .setName(MiniMessage.miniMessage().deserialize(name))
                        .addBothClickEvent("telepads:teleport_per_portable_gui")
                        .build();
                ItemMeta meta = item.getItemMeta();
                meta.getPersistentDataContainer().set(Events.teleID, PersistentDataType.INTEGER, a);
                item.setItemMeta(meta);
                list.add(item);
            }
        }
        return list;
    }

}
