package de.jonas.telepads.commands;

import org.bukkit.Axis;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.data.Orientable;
import org.bukkit.configuration.file.FileConfiguration;

import de.jonas.telepads.TelepadCrafting;
import de.jonas.telepads.Telepads;
import dev.jorel.commandapi.CommandAPICommand;

public class GiveBuildItem {
    Telepads telepads = Telepads.INSTANCE;
    FileConfiguration conf = telepads.getConfig();

    public static final NamespacedKey telepadNum = new NamespacedKey("telepads", "identification_number");

    public GiveBuildItem() {

        new CommandAPICommand("telepads:giveBuildItem")
                .withPermission(conf.getString("GiveBuildItem.permission"))
                .withAliases(conf.getStringList("GiveBuildItem.aliases").toArray(num -> new String[num]))
                .executesPlayer((player, arg) -> {
                    player.getInventory().addItem(TelepadCrafting.telepad);
                })
                .register();
    }

    public static void orientBlock(Location loc, Axis a) {
        if (loc.getBlock().getBlockData() instanceof Orientable o) {
            o.setAxis(a);
            loc.getBlock().setBlockData(o);
        }
    }

}
