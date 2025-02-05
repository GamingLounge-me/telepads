package de.jonas.telepads.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import com.destroystokyo.paper.ParticleBuilder;

import de.jonas.stuff.Stuff;
import de.jonas.stuff.interfaced.ClickEvent;
import de.jonas.stuff.utility.ItemBuilder;
import de.jonas.stuff.utility.PagenationInventory;
import de.jonas.telepads.DataBasePool;
import de.jonas.telepads.Events;
import de.jonas.telepads.Telepads;
import de.jonas.telepads.particle.ParticleRunner;
import de.jonas.telepads.particle.effects.SpiralEffect;
import de.jonas.telepads.particle.spawner.BuilderParticle;
import dev.jorel.commandapi.CommandAPICommand;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class Admin {

    private static final ClickEvent adminTeleport = Admin::adminTeleportI;

    public Admin() {
        Telepads telepads = Telepads.INSTANCE;
        FileConfiguration conf = telepads.getConfig();

        Stuff.INSTANCE.itemBuilderManager.addClickEvent(adminTeleport, "telepads:teleport_per_portable_gui");

        new CommandAPICommand("telepads:admin")
                .withPermission(conf.getString("AdminPermission"))
                .executesPlayer((player, arg) -> {
                    player.openInventory(new PagenationInventory(getItems()).getInventory());
                })
                .register();
    }

    private static void adminTeleportI(InventoryClickEvent e) {
        MiniMessage mm = MiniMessage.miniMessage();
        DataBasePool db = Telepads.INSTANCE.basePool;
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
    }

    public static List<ItemStack> getItems() {
        DataBasePool db = Telepads.INSTANCE.basePool;
        List<Integer> pads = DataBasePool.getAllTelepads(db);

        List<ItemStack> list = new ArrayList<>();
        if (pads != null) {
            for (int a : pads) {
                String name = DataBasePool.getName(db, a);
                ItemStack item = new ItemBuilder()
                        .setMaterial(Material.BEACON)
                        .setName(name)
                        .whenClicked("telepads:teleport_per_portable_gui")
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
