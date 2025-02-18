package de.jonas.telepads.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import com.destroystokyo.paper.ParticleBuilder;

import de.jonas.telepads.DataBasePool;
import de.jonas.telepads.Events;
import de.jonas.telepads.Telepads;
import de.jonas.telepads.particle.ParticleRunner;
import de.jonas.telepads.particle.effects.SpiralEffect;
import de.jonas.telepads.particle.spawner.BuilderParticle;
import dev.jorel.commandapi.CommandAPICommand;
import me.gaminglounge.guiapi.Pagenation;
import me.gaminglounge.itembuilder.ItemBuilder;
import me.gaminglounge.itembuilder.ItemBuilderManager;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class Admin {

    public Admin() {
        Telepads telepads = Telepads.INSTANCE;
        FileConfiguration conf = telepads.getConfig();

        ItemBuilderManager.addBothClickEvent("telepads:teleport_per_portable_gui", (e) -> {
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
        });

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
