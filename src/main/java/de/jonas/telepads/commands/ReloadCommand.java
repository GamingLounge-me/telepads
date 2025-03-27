package de.jonas.telepads.commands;

import org.bukkit.configuration.file.FileConfiguration;

import de.jonas.telepads.Telepads;
import dev.jorel.commandapi.CommandAPICommand;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class ReloadCommand {

    public ReloadCommand() {
        Telepads telepads = Telepads.INSTANCE;
        FileConfiguration conf = telepads.getConfig();
        MiniMessage mm = MiniMessage.miniMessage();

        new CommandAPICommand("telepads:reload")
                .withPermission("telepads.commands.reload")
                .executes(((commandSender, commandArguments) -> {
                    telepads.reloadConfig();
                    commandSender.sendMessage(mm.deserialize("[Telepads] Config successfully reloaded"));
                }))
                .register();
    }

}
