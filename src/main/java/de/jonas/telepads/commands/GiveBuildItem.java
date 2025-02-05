package de.jonas.telepads.commands;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import de.jonas.telepads.Events;
import de.jonas.telepads.Telepads;
import dev.jorel.commandapi.CommandAPICommand;
import me.gaminglounge.itembuilder.ItemBuilder;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class GiveBuildItem {
    private static Telepads telepads = Telepads.INSTANCE;
    private static FileConfiguration conf = telepads.getConfig();
    private static MiniMessage mm = MiniMessage.miniMessage();

    public GiveBuildItem() {

        new CommandAPICommand("telepads:giveBuildItem")
                .withPermission(conf.getString("GiveBuildItem.permission"))
                .withAliases(conf.getStringList("GiveBuildItem.aliases").toArray(num -> new String[num]))
                .executesPlayer((player, arg) -> {
                    player.getInventory().addItem(new ItemBuilder(Material.BEACON)
                            .setName(mm.deserialize("Telepad"))
                            .addBlockPlaceEvent(Events.BUILD_TELEPAD)
                            .build());
                })
                .register();
    }
}
