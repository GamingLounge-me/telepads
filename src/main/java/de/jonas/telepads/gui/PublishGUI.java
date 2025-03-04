package de.jonas.telepads.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import de.jonas.telepads.DataBasePool;
import de.jonas.telepads.Telepads;
import me.gaminglounge.itembuilder.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class PublishGUI implements InventoryHolder {
    Telepads telepads = Telepads.INSTANCE;
    FileConfiguration conf = telepads.getConfig();

    Inventory inv;
    public int id, level;
    public boolean publish;
    String pl;

    public PublishGUI(int id, int level) {
        DataBasePool db = Telepads.INSTANCE.basePool;
        this.id = id;
        this.level = level;
        this.publish = DataBasePool.getPublic(db, id);
        if (level == 1) {
            pl = conf.getString("Messages.telepadLevelRequired");
        } else if (publish) {
            pl = conf.getString("CommonPage.public");
        } else {
            pl = conf.getString("CommonPage.private");
        }

        inv = Bukkit.createInventory(this, (9 * 5), Component.text(""));

        int[] place = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 14, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28,
                29, 30, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44 };
        for (int a : place) {
            inv.setItem(a,
                    new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                            .setName(Component.empty())
                            .addBothClickEvent("telepads:cancelevent")
                            .build());
        }

        inv.setItem(11,
                new ItemBuilder(Material.ENDER_EYE)
                        .setName(MiniMessage.miniMessage().deserialize(conf.getString("TelepadGUI.publicity.name")))
                        .addLoreLine(Component.text(pl))
                        .addBothClickEvent("telepads:publish_to_everyone")
                        .build());

        inv.setItem(13,
                new ItemBuilder(Material.PLAYER_HEAD)
                        .setName(MiniMessage.miniMessage().deserialize(conf.getString("TelepadGUI.publicity.add.name")))
                        .addBothClickEvent("telepads:add_permittet_player")
                        .build());

        inv.setItem(15,
                new ItemBuilder(Material.PAPER)
                        .setName(
                                MiniMessage.miniMessage().deserialize(conf.getString("TelepadGUI.publicity.list.name")))
                        .addBothClickEvent("telepads:list_permittet_player")
                        .build());

        inv.setItem(31,
                new ItemBuilder(Material.BARRIER)
                        .setName(MiniMessage.miniMessage().deserialize(conf.getString("CommonPage.back")))
                        .addBothClickEvent("telepads:open_telepad_gui")
                        .build());

    }

    public void executePublishUpdate() {
        if (publish) {
            pl = conf.getString("CommonPage.public");
        } else {
            pl = conf.getString("CommonPage.private");
        }
        inv.setItem(11,
                new ItemBuilder(Material.ENDER_EYE)
                        .setName(MiniMessage.miniMessage().deserialize(conf.getString("TelepadGUI.publicity.name")))
                        .addLoreLine(Component.text(pl))
                        .addBothClickEvent("telepads:publish_to_everyone")
                        .build());
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inv;
    }

}
