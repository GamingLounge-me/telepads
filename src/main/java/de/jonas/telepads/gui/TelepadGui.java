package de.jonas.telepads.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import de.jonas.stuff.Stuff;
import de.jonas.stuff.interfaced.ClickEvent;
import de.jonas.stuff.utility.ItemBuilder;
import de.jonas.stuff.utility.PagenationInventory;
import de.jonas.stuff.utility.UseNextChatInput;
import de.jonas.telepads.DataBasePool;
import de.jonas.telepads.Telepads;
import de.jonas.telepads.commands.GiveBuildItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

public class TelepadGui implements InventoryHolder {

    MiniMessage mm = MiniMessage.miniMessage();
    Telepads telepads = Telepads.INSTANCE;
    FileConfiguration conf = telepads.getConfig();
    DataBasePool db = telepads.basePool;

    public static final NamespacedKey desti = new NamespacedKey("telepads", "destination");
    public static final NamespacedKey src = new NamespacedKey("telepads", "source");

    public TelepadGui(Player p, int idC) {
        this.telepadGui = Bukkit.createInventory(this, (3 * 9), mm.deserialize(name));

        telepadGui.setItem(22,
                new ItemBuilder()
                        .setMaterial(Material.BARRIER)
                        .setName(conf.getString("CommonPage.close"))
                        .whenClicked("telepads:closeinv")
                        .build());

        String levelLore;
        if (level == 1) {
            levelLore = conf.getString("Messages.upgrade");
        } else {
            levelLore = "";
        }

        telepadGui.setItem(16,
                new ItemBuilder()
                        .setMaterial(Material.getMaterial(conf.getString("TelepadGUI.levelup.block").toUpperCase()))
                        .setName(conf.getString("TelepadGUI.levelup.name"))
                        .addLoreLine("Level: " + level)
                        .addLoreLine(mm.deserialize(levelLore,
                                Placeholder.component("cost",
                                        Component.text(conf.getDouble("TelepadGUI.levelup.cost"))))
                                .decoration(TextDecoration.ITALIC, false))
                        .whenClicked("telepad:pad_level_up")
                        .build());

    }

    private static void openDestinationGuiI(InventoryClickEvent e) {
        MiniMessage mm = MiniMessage.miniMessage();
        if (e.getInventory().getHolder() instanceof TelepadGui tg) {
            e.setCancelled(true);
            DataBasePool db = Telepads.INSTANCE.basePool;
            List<Integer> pads = DataBasePool.getTelepadsIFPermission(db, e.getWhoClicked().getUniqueId(), tg.id);
            List<ItemStack> list = new ArrayList<>();
            for (int a : pads) {
                String name = DataBasePool.getName(db, a);
                String blockID = DataBasePool.getBlockID(db, a);
                Material block;
                if (blockID == null) {
                    block = Material.BEACON;
                } else {
                    block = Material.getMaterial(blockID.toUpperCase());
                }
                ItemStack item = new ItemBuilder()
                        .setMaterial(block)
                        .setName(mm.deserialize(name).decoration(TextDecoration.ITALIC, false))
                        .whenClicked("telepads:select_telepad_destination")
                        .build();
                ItemMeta meta = item.getItemMeta();
                meta.getPersistentDataContainer().set(src, PersistentDataType.INTEGER, tg.id);
                meta.getPersistentDataContainer().set(desti, PersistentDataType.INTEGER, a);
                item.setItemMeta(meta);
                list.add(item);
            }
            e.getWhoClicked().openInventory(
                    new PagenationInventory(list).getInventory());
        }
    }

    private static void levelUpI(InventoryClickEvent e) {
        e.setCancelled(true);
        MiniMessage mm = MiniMessage.miniMessage();
        Telepads telepads = Telepads.INSTANCE;
        FileConfiguration conf = telepads.getConfig();
        DataBasePool db = Telepads.INSTANCE.basePool;
        OfflinePlayer p = (OfflinePlayer) e.getWhoClicked();

        Double cost = conf.getDouble("TelepadGUI.levelup.cost");
        if (p instanceof Player player && e.getInventory().getHolder() instanceof TelepadGui tg) {
            if (tg.level >= 2) {
                player.sendMessage(mm.deserialize(conf.getString("Messages.maxLevel")));
                return;
            }
            if (cost != 0) {
                if (Telepads.getEconomy().getBalance(p) >= cost) {
                    Telepads.getEconomy().withdrawPlayer(player, cost);
                } else {
                    player.sendMessage(mm.deserialize(conf.getString("Messages.noMoney")));
                    return;
                }
            }
            DataBasePool.setLevel2(db, tg.id);
            tg.level++;
            tg.telepadGui.setItem(16,
                    new ItemBuilder()
                            .setMaterial(Material.EMERALD)
                            .setName("Aufwerten")
                            .addLoreLine("Level: " + tg.level)
                            .whenClicked("telepad:pad_level_up")
                            .build());
            player.sendMessage(mm.deserialize(conf.getString("Messages.upgraded")));
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return telepadGui;
    }

}
