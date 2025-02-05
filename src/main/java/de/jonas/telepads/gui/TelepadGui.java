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
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

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

    static ClickEvent changeName = TelepadGui::changeNameI;
    static ClickEvent pickUp = TelepadGui::pickUpI;
    static ClickEvent openDestinationGui = TelepadGui::openDestinationGuiI;
    static ClickEvent levelUp = TelepadGui::levelUpI;

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

        Stuff.INSTANCE.itemBuilderManager.addClickEvent(changeName, "telepads:click_change_name");
        Stuff.INSTANCE.itemBuilderManager.addClickEvent(pickUp, "telepads:pick_telepad_up");
        Stuff.INSTANCE.itemBuilderManager.addClickEvent(openDestinationGui, "telepad:open_initial_destination_gui");
        Stuff.INSTANCE.itemBuilderManager.addClickEvent(levelUp, "telepad:pad_level_up");

        int[] place = { 0, 1, 2, 3, 5, 6, 7, 8, 9, 11, 13, 15, 17, 18, 19, 20, 21, 23, 24, 25, 26 };
        for (int a : place) {
            telepadGui.setItem(a,
                    new ItemBuilder()
                            .setMaterial(Material.GRAY_STAINED_GLASS_PANE)
                            .setName("")
                            .whenClicked("telepads:cancelevent")
                            .build());
        }

        telepadGui.setItem(4,
                new ItemBuilder()
                        .setMaterial(Material.getMaterial(conf.getString("TelepadGUI.customizer.block").toUpperCase()))
                        .setName(conf.getString("TelepadGUI.customizer.name"))
                        .addLoreLine(conf.getString("TelepadGUI.customizer.lore"))
                        .whenClicked("telepads:open_customizer_gui")
                        .build());

        telepadGui.setItem(14,
                new ItemBuilder()
                        .setMaterial(Material.getMaterial(conf.getString("TelepadGUI.destination.block").toUpperCase()))
                        .setName(conf.getString("TelepadGUI.destination.name"))
                        .setLore(lore)
                        .whenClicked("telepad:open_initial_destination_gui")
                        .build());

        telepadGui.setItem(10,
                new ItemBuilder()
                        .setMaterial(Material.getMaterial(conf.getString("TelepadGUI.pickup.block").toUpperCase()))
                        .setName(conf.getString("TelepadGUI.pickup.name"))
                        .whenClicked("telepads:pick_telepad_up")
                        .build());

        telepadGui.setItem(12,
                new ItemBuilder()
                        .setMaterial(Material.getMaterial(conf.getString("TelepadGUI.publicity.block").toUpperCase()))
                        .setName(conf.getString("TelepadGUI.publicity.name"))
                        .whenClicked("telepads:open_publish_gui")
                        .build());

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

    private static void changeNameI(InventoryClickEvent e) {
        MiniMessage mm = MiniMessage.miniMessage();
        Telepads telepads = Telepads.INSTANCE;
        FileConfiguration conf = telepads.getConfig();
        DataBasePool db = telepads.basePool;
        e.getWhoClicked().closeInventory();
        if (e.getInventory().getHolder() instanceof CustomizeGUI tg) {
            new UseNextChatInput((Player) e.getWhoClicked())
                    .sendMessage(conf.getString("TelepadGUI.customizer.telepadname.question"))
                    .setChatEvent((player, message) -> {
                        if (conf.getStringList("TelepadGUI.customizer.telepadname.exitWords").contains(message)) {
                            player.sendMessage(conf.getString("Messages.exitChatInput"));
                            return;
                        }

                        Pattern ptm = Pattern.compile("[a-zA-Z0-9_ #:</>]{3,128}");
                        if (ptm.matcher(LegacyComponentSerializer.legacySection().serialize(mm.deserialize(message)))
                                .matches()) {
                            DataBasePool.setName(db, tg.id, message);
                            player.sendMessage(mm.deserialize(conf.getString("Messages.renameTelepad"),
                                    Placeholder.component("name", mm.deserialize(message))));
                        } else {
                            player.sendMessage(mm.deserialize(conf.getString("Messages.regex")));
                        }
                    })
                    .capture();
        }
    }

    private static void pickUpI(InventoryClickEvent e) {
        MiniMessage mm = MiniMessage.miniMessage();
        Telepads telepads = Telepads.INSTANCE;
        FileConfiguration conf = telepads.getConfig();
        DataBasePool db = telepads.basePool;
        TelepadGui gui = (TelepadGui) e.getInventory().getHolder(false);
        UUID owner = DataBasePool.getOwner(db, gui.id);
        Location location = DataBasePool.getlocation(db, gui.id);
        if (e.getWhoClicked().getUniqueId().equals(owner)
                || e.getWhoClicked().hasPermission(conf.getString("AdminPermission"))) {
            if (e.getWhoClicked().getInventory().firstEmpty() == -1) {
                e.getWhoClicked().sendMessage(mm.deserialize(conf.getString("Messages.invFull")));
                return;
            }
            e.getWhoClicked().closeInventory();
            DataBasePool.removeTelepadsDestinationWithDestination(db, gui.id);
            DataBasePool.removeTelepad(db, gui.id);
            location.clone().add(0.5, 2.5, 0.5).getNearbyEntitiesByType(TextDisplay.class, 0.1).forEach(display -> {
                PersistentDataContainer persis = display.getPersistentDataContainer();
                if (!persis.has(GiveBuildItem.telepadNum))
                    return;
                if (persis.get(GiveBuildItem.telepadNum, PersistentDataType.INTEGER) == gui.id)
                    display.remove();
            });
            location.getWorld().setType(location, Material.AIR);
            location.getWorld().setType(location.clone().add(1, 0, 1), Material.AIR);
            location.getWorld().setType(location.clone().add(1, 0, 0), Material.AIR);
            location.getWorld().setType(location.clone().add(1, 0, -1), Material.AIR);
            location.getWorld().setType(location.clone().add(0, 0, 1), Material.AIR);
            location.getWorld().setType(location.clone().add(0, 0, -1), Material.AIR);
            location.getWorld().setType(location.clone().add(-1, 0, -1), Material.AIR);
            location.getWorld().setType(location.clone().add(-1, 0, 0), Material.AIR);
            location.getWorld().setType(location.clone().add(-1, 0, 1), Material.AIR);
            e.getWhoClicked().getInventory().addItem(
                    new ItemBuilder()
                            .setMaterial(Material.BEACON)
                            .setName("Telepad")
                            .whenPlaced("telepads:buildTelepad")
                            .build());
            Double cost = conf.getDouble("TelepadGUI.levelup.cost");
            if (gui.level >= 2 && cost != 0) {
                Telepads.getEconomy().depositPlayer((OfflinePlayer) e.getWhoClicked(), cost);
                e.getWhoClicked().sendMessage(mm.deserialize(conf.getString("Messages.pickupRegainMoney"),
                        Placeholder.component("cost", Component.text(cost))));
            }
            e.getWhoClicked().sendMessage(mm.deserialize(conf.getString("Messages.pickup")));
        } else {
            e.getWhoClicked().sendMessage(mm.deserialize(conf.getString("Messages.noPerms")));
        }
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
