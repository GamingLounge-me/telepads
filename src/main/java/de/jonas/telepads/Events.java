package de.jonas.telepads;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import com.destroystokyo.paper.profile.PlayerProfile;

import de.jonas.stuff.utility.UseNextChatInput;
import de.jonas.telepads.commands.GiveBuildItem;
import de.jonas.telepads.commands.GivePortableTeleportItem;
import de.jonas.telepads.gui.CustomizeGUI;
import de.jonas.telepads.gui.PublishGUI;
import de.jonas.telepads.gui.TelepadGui;
import me.gaminglounge.guiapi.Pagenation;
import me.gaminglounge.itembuilder.ItemBuilder;
import me.gaminglounge.itembuilder.ItemBuilderManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

public class Events {

    public static final NamespacedKey teleID = new NamespacedKey("telepads", "telepad_id");

    public Events() {
        ItemBuilderManager.addRightClickEvent("telepads:favorite_telepad", (e) -> {
            e.setCancelled(true);
            MiniMessage mm = MiniMessage.miniMessage();
            DataBasePool db = Telepads.INSTANCE.basePool;
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
            MiniMessage mm = MiniMessage.miniMessage();
            Telepads telepads = Telepads.INSTANCE;
            DataBasePool db = telepads.basePool;
            e.getWhoClicked().closeInventory();
            if (e.getInventory().getHolder() instanceof CustomizeGUI tg) {
                new UseNextChatInput((Player) e.getWhoClicked())
                        .sendMessage(
                                "Welcher soll dein neuer Anzeige Block sein?.<br>Schreibe \"exit\" oder \"abbrechen\" um den Vorgang abzubrechen.")
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
                e.getWhoClicked().openInventory(new CustomizeGUI(tg.id, tg.level).getInventory());
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
            DataBasePool db = Telepads.INSTANCE.basePool;
            if (e.getClickedInventory().getHolder() instanceof PublishGUI tg) {
                DataBasePool.setPublic(db, tg.id);
                tg.publish = !(tg.publish);
                tg.executePublishUpdate();
            }
            e.setCancelled(true);
        });

        ItemBuilderManager.addBothClickEvent("telepads:add_permittet_player", (e) -> {
            if (e.getInventory().getHolder() instanceof PublishGUI pg) {
                DataBasePool db = Telepads.INSTANCE.basePool;
                MiniMessage mm = MiniMessage.miniMessage();
                e.setCancelled(true);
                e.getWhoClicked().closeInventory();
                new UseNextChatInput((Player) e.getWhoClicked())
                        .sendMessage(
                                "Schreibe den Spielernamne den du hinzufügen willst in den Chat.<br>Schreibe \"exit\" zum abzubrechen.")
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
            MiniMessage mm = MiniMessage.miniMessage();
            DataBasePool db = Telepads.INSTANCE.basePool;
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
                DataBasePool db = Telepads.INSTANCE.basePool;
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
                e.getWhoClicked().openInventory(new PublishGUI(tg.id, tg.level).getInventory());
            }
        });

        ItemBuilderManager.addBothClickEvent("telepads:select_telepad_destination", (e) -> {
            DataBasePool db = Telepads.INSTANCE.basePool;
            MiniMessage mm = MiniMessage.miniMessage();
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
    }
}
