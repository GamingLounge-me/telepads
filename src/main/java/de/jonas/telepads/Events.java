package de.jonas.telepads;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.block.Beacon;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import com.destroystokyo.paper.ParticleBuilder;
import com.destroystokyo.paper.profile.PlayerProfile;

import de.jonas.telepads.commands.GiveBuildItem;
import de.jonas.telepads.commands.GivePortableTeleportItem;
import de.jonas.telepads.gui.CustomizeGUI;
import de.jonas.telepads.gui.PublishGUI;
import de.jonas.telepads.gui.TelepadGui;
import de.jonas.telepads.particle.ParticleRunner;
import de.jonas.telepads.particle.effects.SpiralEffect;
import de.jonas.telepads.particle.spawner.BuilderParticle;
import me.gaminglounge.guiapi.Pagenation;
import me.gaminglounge.itembuilder.ItemBuilder;
import me.gaminglounge.itembuilder.ItemBuilderManager;
import me.gaminglounge.playerinputapi.UseNextChatInput;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class Events {

    public static final NamespacedKey teleID = new NamespacedKey("telepads", "telepad_id");

    public Events() {
        MiniMessage mm = MiniMessage.miniMessage();
        Telepads telepads = Telepads.INSTANCE;
        FileConfiguration conf = telepads.getConfig();
        DataBasePool db = telepads.basePool;

        ItemBuilderManager.addRightClickEvent("telepads:favorite_telepad", (e) -> {
            e.setCancelled(true);
            if (e.getCurrentItem() == null || e.getCurrentItem().getItemMeta() == null)
                return;
            int id = e.getCurrentItem().getItemMeta().getPersistentDataContainer().get(teleID,
                    PersistentDataType.INTEGER);
            UUID playerUUID = e.getWhoClicked().getUniqueId();
            if (playerUUID == null) {
                e.getWhoClicked().closeInventory();
                e.getWhoClicked().sendMessage(mm.deserialize("Unexpected error (Events.java | favI)"));
            }
            if (DataBasePool.getPlayerFavorite(db, playerUUID, id)) {
                DataBasePool.removePlayerFavorites(db, id, playerUUID);
            } else {
                DataBasePool.addPlayerFavorites(db, id, playerUUID);
            }
            if (e.getInventory().getHolder() instanceof Pagenation pgi) {
                pgi.setItems(GivePortableTeleportItem.getItems((Player) e.getWhoClicked()));
                pgi.fillPage(pgi.currentpage);
            }
        });

        ItemBuilderManager.addBothClickEvent("telepads:click_block", (e) -> {
            e.getWhoClicked().closeInventory();
            if (e.getInventory().getHolder() instanceof CustomizeGUI tg) {
                new UseNextChatInput((Player) e.getWhoClicked())
                        .sendMessage(mm.deserialize(
                                "Welcher soll dein neuer Anzeige Block sein?.<br>Schreibe \"exit\" oder \"abbrechen\" um den Vorgang abzubrechen."))
                        .setChatEvent((player, message) -> {
                            if (message.equalsIgnoreCase("exit") || message.equalsIgnoreCase("abbrechen")) {
                                player.sendMessage("Abgebrochen");
                                return;
                            }

                            Pattern ptm = Pattern.compile("[a-zA-Z_]{1,64}");
                            if (ptm.matcher(message).matches()) {
                                Material mat = Material.matchMaterial(message.toUpperCase());
                                if (mat == null || !mat.isItem()) {
                                    player.sendMessage(mm.deserialize("<red>Dieses Item wurde nicht gefunden.</red>"));
                                    return;
                                }
                                DataBasePool.setBlockID(db, tg.id, message);
                                player.sendMessage(
                                        mm.deserialize(
                                                "Dein Telepad Block wurde zu \"<green><name></green>\" geändert.",
                                                Placeholder.component("name", Component.text(message))));
                            } else {
                                player.sendMessage(mm.deserialize("<red>Dieses Item wurde nicht gefunden.</red>"));
                            }
                        })
                        .capture();
            }
        });

        ItemBuilderManager.addBothClickEvent("telepads:open_customizer_gui", (e) -> {
            if (e.getInventory().getHolder() instanceof TelepadGui tg) {
                e.setCancelled(true);
                e.getWhoClicked().openInventory(new CustomizeGUI(tg.id, tg.level, (Player) e.getWhoClicked()).getInventory());
            }
        });

        ItemBuilderManager.addBothClickEvent("telepads:open_telepad_gui", (e) -> {
            e.setCancelled(true);
            if (e.getClickedInventory().getHolder() instanceof PublishGUI pg) {
                e.getWhoClicked().openInventory(new TelepadGui((Player) e.getWhoClicked(), pg.id).getInventory());
            }
            if (e.getClickedInventory().getHolder() instanceof CustomizeGUI cg) {
                e.getWhoClicked().openInventory(new TelepadGui((Player) e.getWhoClicked(), cg.id).getInventory());
            }
        });

        ItemBuilderManager.addBothClickEvent("telepads:publish_to_everyone", (e) -> {
            if (e.getClickedInventory().getHolder() instanceof PublishGUI tg) {
                DataBasePool.setPublic(db, tg.id);
                tg.publish = !(tg.publish);
                tg.executePublishUpdate((Player) e.getWhoClicked());
            }
            e.setCancelled(true);
        });

        ItemBuilderManager.addBothClickEvent("telepads:add_permittet_player", (e) -> {
            if (e.getInventory().getHolder() instanceof PublishGUI pg) {

                e.setCancelled(true);
                e.getWhoClicked().closeInventory();
                new UseNextChatInput((Player) e.getWhoClicked())
                        .sendMessage(mm.deserialize(
                                "Schreibe den Spielernamne den du hinzufügen willst in den Chat.<br>Schreibe \"exit\" zum abzubrechen."))
                        .setChatEvent((player, message) -> {
                            if (message.equalsIgnoreCase("exit")) {
                                player.sendMessage("Abgebrochen");
                                return;
                            }
                            DataBasePool.addPlayerPermission(db, pg.id, Bukkit.getOfflinePlayer(message).getUniqueId());
                            player.sendMessage(mm.deserialize(
                                    "Der Spieler \"<green><name></green>\" wurde für dieses Telepad gesetzt.",
                                    Placeholder.component("name",
                                            Component.text(Bukkit.getOfflinePlayer(message).getName()))));
                            // player.sendMessage(mm.deserialize("<red>Ungültiger Name.</red>"));
                        })
                        .capture();
            }
        });

        ItemBuilderManager.addBothClickEvent("telepads:remove_permittet_player", (e) -> {
            ItemMeta meta = e.getCurrentItem().getItemMeta();
            SkullMeta skull = (SkullMeta) e.getCurrentItem().getItemMeta();
            e.setCancelled(true);
            DataBasePool.removePlayerPermission(db,
                    meta.getPersistentDataContainer().get(teleID, PersistentDataType.INTEGER),
                    skull.getPlayerProfile().getId());
            e.getWhoClicked().sendMessage(mm.deserialize(
                    "Der Spieler <green>\"" + skull.getPlayerProfile().getName() + "\"</green> wurde entfernt."));
            if (e.getInventory().getHolder() instanceof Pagenation pg) {
                pg.items.remove(e.getCurrentItem());
                pg.fillPage(pg.currentpage);
            }
        });

        ItemBuilderManager.addBothClickEvent("telepads:list_permittet_player", (e) -> {
            if (e.getInventory().getHolder() instanceof PublishGUI pg) {
                e.setCancelled(true);
                List<UUID> list = DataBasePool.getPermittetPlayer(db, pg.id);
                List<ItemStack> items = new ArrayList<>();
                for (UUID a : list) {
                    PlayerProfile prof = Bukkit.getOfflinePlayer(a).getPlayerProfile();
                    if (!prof.completeFromCache()) {
                        prof.complete();
                    }
                    ItemStack item = new ItemBuilder(a)
                            .setName(Component.text(prof.getName()))
                            .addLoreLine(Component.text("Klicke um zu entfernen."))
                            .addBothClickEvent("telepads:remove_permittet_player")
                            .build();
                    ItemMeta meta = item.getItemMeta();
                    meta.getPersistentDataContainer().set(teleID, PersistentDataType.INTEGER, pg.id);
                    item.setItemMeta(meta);
                    items.add(item);
                }
                e.getWhoClicked()
                        .openInventory(new Pagenation((Player) e.getWhoClicked()).setItems(items).getInventory());
            }
        });

        ItemBuilderManager.addBothClickEvent("telepads:open_publish_gui", (e) -> {
            if (e.getInventory().getHolder() instanceof TelepadGui tg) {
                e.setCancelled(true);
                e.getWhoClicked().openInventory(new PublishGUI(tg.id, tg.level, (Player) e.getWhoClicked()).getInventory());
            }
        });

        ItemBuilderManager.addBothClickEvent("telepads:select_telepad_destination", (e) -> {
            e.setCancelled(true);
            PersistentDataContainer container = e.getCurrentItem().getItemMeta().getPersistentDataContainer();
            int idsource = container.get(TelepadGui.src, PersistentDataType.INTEGER);
            int id = container.get(TelepadGui.desti, PersistentDataType.INTEGER);

            DataBasePool.setNewDestinationID(db, idsource, id);
            Component name = mm.deserialize(DataBasePool.getName(db, id));
            e.getWhoClicked().sendMessage(mm.deserialize("Du hast \"<desti>\" erfolgreich als Ziel gesetzt.",
                    Placeholder.component("desti", name)));
            Location l = DataBasePool.getlocation(db, idsource);
            l.add(0.5, 2.5, 0.5);
            l.getNearbyEntitiesByType(TextDisplay.class, 0.1).forEach(display -> {
                PersistentDataContainer persis = display.getPersistentDataContainer();
                if (!persis.has(GiveBuildItem.telepadNum))
                    return;
                if (persis.get(GiveBuildItem.telepadNum, PersistentDataType.INTEGER) == idsource)
                    display.remove();
            });
            TextDisplay t = (TextDisplay) l.getWorld().spawnEntity(l, EntityType.TEXT_DISPLAY);
            t.text(name);
            t.getPersistentDataContainer().set(GiveBuildItem.telepadNum, PersistentDataType.INTEGER, idsource);
            t.setBillboard(Display.Billboard.CENTER);
            e.getWhoClicked().closeInventory();
        });

        ItemBuilderManager.addBothClickEvent("telepads:cancelevent", (e) -> {
            e.setCancelled(true);
        });

        ItemBuilderManager.addBothClickEvent("telepads:closeinv", (e) -> {
            e.setCancelled(true);
            e.getWhoClicked().closeInventory();
        });

        ItemBuilderManager.addBothClickEvent("telepads:click_change_name", (e) -> {
            e.getWhoClicked().closeInventory();
            if (e.getInventory().getHolder() instanceof CustomizeGUI tg) {
                new UseNextChatInput((Player) e.getWhoClicked())
                        .sendMessage(mm.deserialize(conf.getString("TelepadGUI.customizer.telepadname.question")))
                        .setChatEvent((player, message) -> {
                            if (conf.getStringList("TelepadGUI.customizer.telepadname.exitWords").contains(message)) {
                                player.sendMessage(conf.getString("Messages.exitChatInput"));
                                return;
                            }

                            Pattern ptm = Pattern.compile("[a-zA-Z0-9_ #:</>]{1,32}");
                            if (ptm.matcher(PlainTextComponentSerializer.plainText().serialize(mm.deserialize(message)))
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
        });

        ItemBuilderManager.addBothClickEvent("telepads:pick_telepad_up", (e) -> {
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
                        new ItemBuilder(Material.BEACON)
                                .setName(Component.text("Telepad"))
                                .addBlockPlaceEvent("telepads:buildTelepad")
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
        });

        ItemBuilderManager.addBothClickEvent("telepad:open_initial_destination_gui", (e) -> {
            if (e.getInventory().getHolder() instanceof TelepadGui tg) {
                e.setCancelled(true);
                List<Integer> pads = DataBasePool.getTelepadsIFPermission(db, e.getWhoClicked().getUniqueId(), tg.id);
                List<ItemStack> list = new ArrayList<>();
                for (int a : pads) {
                    String nameI = DataBasePool.getName(db, a);
                    String blockID = DataBasePool.getBlockID(db, a);
                    Material block;
                    if (blockID == null) {
                        block = Material.BEACON;
                    } else {
                        block = Material.getMaterial(blockID.toUpperCase());
                    }
                    ItemStack item = new ItemBuilder(block)
                            .setName(mm.deserialize(nameI).decoration(TextDecoration.ITALIC, false))
                            .addBothClickEvent("telepads:select_telepad_destination")
                            .build();
                    ItemMeta meta = item.getItemMeta();
                    meta.getPersistentDataContainer().set(TelepadGui.src, PersistentDataType.INTEGER, tg.id);
                    meta.getPersistentDataContainer().set(TelepadGui.desti, PersistentDataType.INTEGER, a);
                    item.setItemMeta(meta);
                    list.add(item);
                }
                e.getWhoClicked().openInventory(
                        new Pagenation((Player) e.getWhoClicked()).setItems(list).getInventory());
            }
        });

        ItemBuilderManager.addBothClickEvent("telepad:pad_level_up", (e) -> {
            e.setCancelled(true);
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
                        new ItemBuilder(Material.EMERALD)
                                .setName(Component.text("Aufwerten"))
                                .addLoreLine(MiniMessage.miniMessage().deserialize("Level: " + tg.level))
                                .addBothClickEvent("telepad:pad_level_up")
                                .build());
                player.sendMessage(mm.deserialize(conf.getString("Messages.upgraded")));
            }
        });

        ItemBuilderManager.addBothClickEvent("telepads:teleport_per_portable_gui", (e) -> {
            e.setCancelled(true);
            e.getWhoClicked().closeInventory();
            if (e.getCurrentItem() == null || e.getCurrentItem().getItemMeta() == null)
                return;
            int id = e.getCurrentItem().getItemMeta().getPersistentDataContainer().get(Events.teleID,
                    PersistentDataType.INTEGER);
            Location l = DataBasePool.getlocation(db, id).add(0.5, 1, 0.5);
            e.getWhoClicked().teleport(l);
            new ParticleRunner(
                    Telepads.INSTANCE,
                    l,
                    new SpiralEffect(2,
                            1,
                            2,
                            new BuilderParticle(
                                    new ParticleBuilder(Particle.DUST)
                                            .count(1)
                                            .color(Color.PURPLE, 1f)
                                            .source((Player) e.getWhoClicked()))),
                    2,
                    10);
        });

        ItemBuilderManager.addBlockPlaceEvent("telepads:buildTelepad", (pv) -> {
            Location loc, l1, l2, l3, l4, l5, l6, l7, l8;
            loc = pv.getBlock().getLocation();
            l1 = pv.getBlock().getLocation().add(1, 0, 1);
            l2 = pv.getBlock().getLocation().add(1, 0, 0);
            l3 = pv.getBlock().getLocation().add(1, 0, -1);
            l4 = pv.getBlock().getLocation().add(0, 0, 1);
            l5 = pv.getBlock().getLocation().add(0, 0, -1);
            l6 = pv.getBlock().getLocation().add(-1, 0, -1);
            l7 = pv.getBlock().getLocation().add(-1, 0, 0);
            l8 = pv.getBlock().getLocation().add(-1, 0, 1);

            if (l1.getBlock().getType().isAir() &&
                    l2.getBlock().getType().isAir() &&
                    l3.getBlock().getType().isAir() &&
                    l4.getBlock().getType().isAir() &&
                    l5.getBlock().getType().isAir() &&
                    l6.getBlock().getType().isAir() &&
                    l7.getBlock().getType().isAir() &&
                    l8.getBlock().getType().isAir() &&
                    pv.getBlockPlaced().getState(false) instanceof Beacon b) {
                int id = DataBasePool.setNewTelepad(db, pv.getPlayer().getUniqueId(), pv.getBlock().getLocation());
                if (id == -1) {
                    pv.getPlayer().sendMessage(mm.deserialize("Messages.dbError"));
                    return;
                }
                b.getPersistentDataContainer().set(GiveBuildItem.telepadNum, PersistentDataType.INTEGER, id);
                loc.getWorld().setType(l1, Material.DARK_PRISMARINE);
                loc.getWorld().setType(l3, Material.DARK_PRISMARINE);
                loc.getWorld().setType(l6, Material.DARK_PRISMARINE);
                loc.getWorld().setType(l8, Material.DARK_PRISMARINE);
                loc.getWorld().setType(l2, Material.STRIPPED_WARPED_HYPHAE);
                GiveBuildItem.orientBlock(l2, Axis.X);
                loc.getWorld().setType(l4, Material.STRIPPED_WARPED_HYPHAE);
                GiveBuildItem.orientBlock(l4, Axis.Z);
                loc.getWorld().setType(l5, Material.STRIPPED_WARPED_HYPHAE);
                GiveBuildItem.orientBlock(l5, Axis.X);
                loc.getWorld().setType(l7, Material.STRIPPED_WARPED_HYPHAE);
                GiveBuildItem.orientBlock(l7, Axis.Z);
            } else {
                pv.setCancelled(true);
            }
        });

        ItemBuilderManager.addLeftClickEvent("telepads:teleport_per_portable_gui", (e) -> {
            e.setCancelled(true);
            if (e.getCurrentItem() == null || e.getCurrentItem().getItemMeta() == null)
                return;
            e.getWhoClicked().closeInventory();
            int id = e.getCurrentItem().getItemMeta().getPersistentDataContainer().get(Events.teleID,
                    PersistentDataType.INTEGER);
            Location l = DataBasePool.getlocation(db, id).add(0.5, 1, 0.5);
            double cost = conf.getDouble("UseTelepadCost");
            if (cost != 0) {
                if (Telepads.getEconomy().getBalance((OfflinePlayer) e.getWhoClicked()) <= cost) {
                    e.getWhoClicked().sendMessage(mm.deserialize("Messages.noMoney"));
                    return;
                }
                Telepads.getEconomy().withdrawPlayer((OfflinePlayer) e.getWhoClicked(), cost);
            }
            e.getWhoClicked().sendMessage(mm.deserialize("Messages.teleport",
                    Placeholder.component("cost", Component.text(cost))));
            e.getWhoClicked().teleport(l);
            new ParticleRunner(
                    Telepads.INSTANCE,
                    l,
                    new SpiralEffect(2,
                            1,
                            2,
                            new BuilderParticle(
                                    new ParticleBuilder(Particle.DUST)
                                            .count(1)
                                            .color(Color.PURPLE, 1f)
                                            .source((Player) e.getWhoClicked()))),
                    2,
                    10);
        });

    }
}
