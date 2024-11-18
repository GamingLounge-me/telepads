package de.jonas.telepads.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import com.destroystokyo.paper.ParticleBuilder;

import de.jonas.stuff.Stuff;
import de.jonas.stuff.interfaced.LeftClickEvent;
import de.jonas.stuff.utility.ItemBuilder;
import de.jonas.stuff.utility.PagenationInventory;
import de.jonas.telepads.DataBasePool;
import de.jonas.telepads.Events;
import de.jonas.telepads.Telepads;
import de.jonas.telepads.particle.ParticleRunner;
import de.jonas.telepads.particle.effects.SpiralEffect;
import de.jonas.telepads.particle.spawner.BuilderParticle;
import dev.jorel.commandapi.CommandAPICommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class GivePortableTeleportItem {

    private static final LeftClickEvent teleport = GivePortableTeleportItem::teleportI;

    public GivePortableTeleportItem() {

        Stuff.INSTANCE.itemBuilderManager.addleftClickEvent(teleport, "telepads:teleport_per_portable_gui");

        new CommandAPICommand("telepad:openTeleportGUI")
        .withAliases("pad")
        .withPermission("telepads.giveportbleitem")
        .executesPlayer((player, arg) -> {
            player.openInventory(new PagenationInventory(getItems(player)).getInventory());
        })
        .register();
    }

    private static void teleportI(InventoryClickEvent e) {
        MiniMessage mm = MiniMessage.miniMessage();
        DataBasePool db = Telepads.INSTANCE.basePool;
        e.setCancelled(true);
        e.getWhoClicked().closeInventory();
        if (Telepads.getEconomy().getBalance((OfflinePlayer) e.getWhoClicked()) <= 2d) {
            e.getWhoClicked().sendMessage("<red>Du hast nicht genügen Coins um dich zu Teleportieren.</red>");
            return;
        }
        if (e.getCurrentItem() == null || e.getCurrentItem().getItemMeta() == null) return;
        int id = e.getCurrentItem().getItemMeta().getPersistentDataContainer().get(Events.teleID, PersistentDataType.INTEGER);
        Location l = DataBasePool.getlocation(db, id).add(0.5,1,0.5);
        Telepads.getEconomy().withdrawPlayer((OfflinePlayer) e.getWhoClicked(), 2d);
        e.getWhoClicked().sendMessage(mm.deserialize("Dir wurden <green>2 Coins</green> zum Teleport abgezogen."));
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
                                        .source((Player) e.getWhoClicked()))
                    ),
                    2,
                    10);
    }

    public static List<ItemStack> getItems(Player player) {
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
                String fav;
                if (isFavorite) {
                    fav = "Favorite";
                } else {fav="";}
                Material block;
                if (blockID == null) {
                    block = Material.BEACON;
                } else {block = Material.getMaterial(blockID.toUpperCase());}

                ItemStack item = new ItemBuilder()
                    .setMaterial(block)
                    .setName(Component.text(name))
                    .whenLeftClicked("telepads:teleport_per_portable_gui")
                    .whenRightClicked("telepads:favorite_telepad")
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
