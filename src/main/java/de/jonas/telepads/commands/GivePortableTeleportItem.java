package de.jonas.telepads.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import de.jonas.telepads.DataBasePool;
import de.jonas.telepads.Events;
import de.jonas.telepads.Telepads;
import dev.jorel.commandapi.CommandAPICommand;
import me.gaminglounge.guiapi.Pagenation;
import me.gaminglounge.itembuilder.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class GivePortableTeleportItem {

    public GivePortableTeleportItem() {
        MiniMessage mm = MiniMessage.miniMessage();
        Telepads telepads = Telepads.INSTANCE;
        FileConfiguration conf = telepads.getConfig();

        new CommandAPICommand("telepads:openTeleportGUI")
                .withPermission(conf.getString("PortableTelepadCommand.permission"))
                .withAliases(conf.getStringList("PortableTelepadCommand.aliases").toArray(num -> new String[num]))
                .executesPlayer((player, arg) -> {
                    player.openInventory(new Pagenation(player).setItems(getItems(player)).getInventory());
                })
                .register();
    }

    public static List<ItemStack> getItems(Player player) {
        MiniMessage mm = MiniMessage.miniMessage();
        Telepads telepads = Telepads.INSTANCE;
        FileConfiguration conf = telepads.getConfig();
        DataBasePool db = Telepads.INSTANCE.basePool;
        List<Integer> pads = DataBasePool.getAllTelepadsIFPermissionAndLevel2PadFavorites(db, player.getUniqueId());
        List<Integer> padss = DataBasePool.getAllTelepadsIFPermissionAndLevel2PadNotFavorites(db, player.getUniqueId());
        if (padss != null) {
            for (Integer a : padss) {
                pads.add(a);
            }
        }

        List<ItemStack> list = new ArrayList<>();
        if (pads != null) {
            for (int a : pads) {
                String name = DataBasePool.getName(db, a);
                String blockID = DataBasePool.getBlockID(db, a);
                boolean isFavorite = DataBasePool.getPlayerFavorite(db, player.getUniqueId(), a);
                Component fav;
                if (isFavorite) {
                    fav = mm.deserialize(conf.getString("PortableTelepad.FavoriteMarker"))
                            .decoration(TextDecoration.ITALIC, false);
                } else {
                    fav = Component.text("");
                }
                Material block;
                if (blockID == null) {
                    block = Material.BEACON;
                } else {
                    block = Material.getMaterial(blockID.toUpperCase());
                }

                ItemStack item = new ItemBuilder(block)
                        .setName(mm.deserialize(name).decoration(TextDecoration.ITALIC, false))
                        .addleftClickEvent("telepads:teleport_per_portable_gui")
                        .addRightClickEvent("telepads:favorite_telepad")
                        .addLoreLine(mm.deserialize(conf.getString("PortableTelepad.LeftClickTELEPRT"))
                                .decoration(TextDecoration.ITALIC, false))
                        .addLoreLine(mm.deserialize(conf.getString("PortableTelepad.RightClickFAVOTITE"))
                                .decoration(TextDecoration.ITALIC, false))
                        .addLoreLine(fav)

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
