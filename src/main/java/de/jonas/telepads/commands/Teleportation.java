package de.jonas.telepads.commands;

import org.bukkit.entity.Player;

import de.jonas.telepads.Telepads;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.PlayerArgument;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class Teleportation {
    Telepads telepads = Telepads.INSTANCE;
    MiniMessage mm =MiniMessage.miniMessage();
    String prefix = "<white>[</white><#ff0000>G<#ff1100>a<#ff2200>m<#ff3300>i<#ff4400>n<#ff5500>g<#ff6600>L<#ff7700>o<#ff8800>u<#ff9900>n<#ffaa00>g<#ffbb00>e<white>]</white> ";

    public Teleportation(){
        new CommandAPICommand("tpa")
            .withArguments(new PlayerArgument("player")
            .withPermission("telepads.tpa")
                .executesPlayer((executor,args)->{
                    Telepads.INSTANCE.teleportationManager.createTPA(executor.getPlayer(), (Player) args.get("player"));
            }))
            .register();
        new CommandAPICommand("tpaaccept")
            .withArguments(new PlayerArgument("player")
            .withPermission("telepads.tpa.accept")
                .executesPlayer((executor,args)->{
                    Telepads.INSTANCE.teleportationManager.acceptTPA(executor.getPlayer(), (Player) args.get("player"));
            }))
        .register();
        new CommandAPICommand("tpadecline")
            .withArguments(new PlayerArgument("player")
            .withPermission("telepads.tpa.accept")
                .executesPlayer((executor,args)->{
                    Telepads.INSTANCE.teleportationManager.declineTPA(executor.getPlayer(), (Player) args.get("player"));
            }))
    .register();
    }
}
