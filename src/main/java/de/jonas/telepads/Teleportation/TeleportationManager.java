package de.jonas.telepads.Teleportation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.intellij.lang.annotations.Language;

import de.jonas.stuff.Stuff;
import de.jonas.telepads.Telepads;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

public class TeleportationManager {

    Telepads telepads = Telepads.INSTANCE;
    MiniMessage mm = MiniMessage.miniMessage();
    private HashMap<Entity, List<Entity>> TPAList;
    Map<Player, BukkitTask> notMoveTimer;
    String prefix = "<white>[</white><#ff0000>G<#ff1100>a<#ff2200>m<#ff3300>i<#ff4400>n<#ff5500>g<#ff6600>L<#ff7700>o<#ff8800>u<#ff9900>n<#ffaa00>g<#ffbb00>e<white>]</white> ";

    private boolean isSafeLocation(Location location) {
        Block block = location.getBlock();
        Block below = location.clone().add(0, -1, 0).getBlock();
        Block above = location.clone().add(0, 1, 0).getBlock();

        // Check if the block at the location and the block above are air, and the block
        // below is solid
        return block.getType() == Material.AIR && above.getType() == Material.AIR && (below.getType().isSolid()
                || below.getType() != Material.LAVA);
    }

    public TeleportationManager() {
        TPAList = new HashMap<Entity, List<Entity>>();
        notMoveTimer = new HashMap<Player, BukkitTask>();
    }

    // Method to add a player to the TPAList
    public void addPlayerToTPAList(Entity target, Entity executor) {
        TPAList.computeIfAbsent(target, k -> new ArrayList<>()).add(executor);
    }

    public void createTPA(Player executor, Player target) {

        String tmp = "<aqua>Der spieler <player> hat dir eine TPA gesenden.</aqua><br><green><bold><accept>Annehmen</accept></bold></green> <gray>|</gray> <dark_red><decline>Ablehnen</decline></dark_red>";

        Component msg = mm.deserialize(
                tmp,
                Placeholder.styling("accept",
                        ClickEvent.runCommand("/telepad:tpaaccept " + executor.getName())),
                Placeholder.styling("decline",
                        ClickEvent.runCommand("/telepad:tpadecline " + executor.getName())),
                Placeholder.component("player", executor.displayName()));

        target.sendMessage(msg);
        addPlayerToTPAList(target, executor);

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        ScheduledFuture<?> scheduledTask = scheduler.schedule(() -> {
            if (!TPAList.isEmpty()) {
                removePlayerFromTPAList(target, executor);
            }
        }, Telepads.INSTANCE.getConfig().getInt("TPA.AutoCancleInMinutes"), TimeUnit.MINUTES);

        // Check if the HashMap is empty and cancel the task if it is
        if (TPAList.isEmpty()) {
            scheduledTask.cancel(false);
        }
    }

    public void acceptTPA(Player executor, Player target) {

        if (TPAList.get(target) != null && TPAList.get(target).contains(executor)) {
            removePlayerFromTPAList(target, executor);
            teleportPlayer(executor, target);
        } else {
            executor.sendMessage(mm.deserialize("some error"));
        }
    }

    public void declineTPA(Player executor, Player target) {
        if (TPAList.get(target) != null && TPAList.get(target).contains(executor)) {
            removePlayerFromTPAList(target, executor);
            target.sendMessage(mm.deserialize(prefix + "<>"));
        } else {
            executor.sendMessage(mm.deserialize("some error"));
        }
    }

    // Method to remove a player from the TPAList
    public void removePlayerFromTPAList(Entity target, Entity executor) {
        List<Entity> executors = TPAList.get(target);
        if (executors != null) {
            executors.remove(executor);
            if (executors.isEmpty()) {
                TPAList.remove(target);
            }
        }
    }

    public void teleportPlayer(Player executor, Player target) {

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(telepads, () -> {
            long startTime = System.currentTimeMillis();

            if (System.currentTimeMillis() - startTime >= Telepads.INSTANCE.getConfig()
                    .getInt("TPA.DontMoveTimeInMill")) {
                removePlayerFromTPAList(executor, target);
                notMoveTimer.remove(target);
                teleport(target, executor.getLocation());
            }

            if (target.getVelocity().length() > 0.1) {
                removePlayerFromTPAList(executor, target);
                notMoveTimer.remove(target);
                target.sendMessage(
                        mm.deserialize("<gradient:#fa0000:#f73105>Your TPA got cancelt, cause you moved.</gradient>"));
                return;
            }
            // could add a title and sound, that get's send every sec
        }, 0, 20);
        notMoveTimer.put(target, task);
    }

    public void teleport(Player target, Location location) {
        if (isSafeLocation(location)) {
            target.teleport(location);
            target.sendMessage(mm.deserialize("<green>You have been teleported safely.</green>"));
        } else {
            target.sendMessage(mm.deserialize("<red>The teleportation location is not safe.</red>"));
        }
    }

}
