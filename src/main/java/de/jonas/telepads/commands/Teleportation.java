package de.jonas.telepads.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import de.jonas.telepads.Telepads;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.PlayerArgument;
import dev.jorel.commandapi.arguments.SafeSuggestions;

public class Teleportation {

    public Teleportation() {
        new CommandAPICommand("telepad:tpa")
                .withAliases("tpa")
                .withArguments(new PlayerArgument("player").replaceSafeSuggestions(
                        SafeSuggestions.suggest(info -> Bukkit.getOnlinePlayers()
                                .toArray(new Player[0]))))
                .withPermission("telepads.tpa.tpa")
                .executesPlayer((executor, args) -> {
                    Telepads.INSTANCE.teleportationManager.createTPA(executor.getPlayer(),
                            (Player) args.get("player"));
                })
                .register();

        new CommandAPICommand("telepad:tpaaccept")
                .withAliases("tpaaccept", "tpac")
                .withArguments(new PlayerArgument("player").replaceSafeSuggestions(
                        SafeSuggestions.suggest(info -> Bukkit.getOnlinePlayers()
                                .toArray(new Player[0]))))
                .withPermission("telepads.tpa.accept")
                .executesPlayer((executor, args) -> {
                    Telepads.INSTANCE.teleportationManager.acceptTPA(executor.getPlayer(),
                            (Player) args.get("player"));
                })
                .register();

        new CommandAPICommand("telepad:tpadecline")
                .withAliases("tpadecline", "tpad")
                .withArguments(new PlayerArgument("player").replaceSafeSuggestions(
                        SafeSuggestions.suggest(info -> Bukkit.getOnlinePlayers()
                                .toArray(new Player[0]))))
                .withPermission("telepads.tpa.decline")
                .executesPlayer((executor, args) -> {
                    Telepads.INSTANCE.teleportationManager.declineTPA(executor.getPlayer(),
                            (Player) args.get("player"));
                })
                .register();

    }
}
