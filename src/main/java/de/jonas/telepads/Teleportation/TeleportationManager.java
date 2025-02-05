package de.jonas.telepads.Teleportation;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.Instant;
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

import de.jonas.telepads.Telepads;
import me.gaminglounge.configapi.Language;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.Title.Times;

public class TeleportationManager {

    Telepads telepads = Telepads.INSTANCE;
    MiniMessage mm = MiniMessage.miniMessage();
    private HashMap<Entity, List<Entity>> tpaList;
    Map<Player, BukkitTask> notMoveTimer;

    private boolean isSafeLocation(Location location) {
        Block block = location.getBlock();
        Block below = location.clone().add(0, -1, 0).getBlock();
        Block above = location.clone().add(0, 1, 0).getBlock();

        // Check if the block at the location and the block above are air, and the block
        // below is solid

        Material mat;
        mat = block.getType();
        boolean safeBlock = (mat == Material.AIR ||
                mat == Material.WATER);

        mat = above.getType();
        boolean safeAbove = (mat == Material.AIR);

        mat = below.getType();
        boolean safeBelow = (mat.isSolid() ||
                mat != Material.LAVA ||
                mat != Material.MAGMA_BLOCK);

        return safeAbove && safeBelow && safeBlock;
    }

    public TeleportationManager() {
        tpaList = new HashMap<>();
        notMoveTimer = new HashMap<>();
    }

    // Method to add a player to the tpaList
    public void addPlayerToTpaList(Entity target, Entity executor) {
        tpaList.computeIfAbsent(target, k -> new ArrayList<>()).add(executor);
    }

    public void createTPA(Player executor, Player target) {

        if (executor == target) {
            executor.sendMessage(mm.deserialize(Language.getValue(telepads, executor, "error.player.is.sender", true)));
            return;
        }

        if (tpaList.containsKey(target) && tpaList.get(target).contains(executor)) {
            executor.sendMessage(mm.deserialize(Language.getValue(telepads, executor, "error.player.hasTPA", true)));
            return;
        }

        Component msg = mm.deserialize(
                Language.getValue(telepads, target, "tpa.send.recipient"),
                Placeholder.styling("accept",
                        ClickEvent.runCommand("/telepad:tpaaccept " + executor.getName())),
                Placeholder.styling("decline",
                        ClickEvent.runCommand("/telepad:tpadecline " + executor.getName())),
                Placeholder.component("player", executor.displayName()));

        target.sendMessage(msg);
        executor.sendMessage(mm.deserialize(Language.getValue(telepads, executor, "tpa.send.sender", true),
                Placeholder.component("player", target.displayName())));
        addPlayerToTpaList(target, executor);

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        ScheduledFuture<?> scheduledTask = scheduler.schedule(() -> {
            if (!tpaList.isEmpty()) {
                removePlayerFromTpaList(target, executor);
                executor.sendMessage(mm.deserialize(Language.getValue(telepads, target, "error.tpa.expired", true)));
            }
        }, Telepads.INSTANCE.getConfig().getLong("TPA.AutoCancleInSeconds"), TimeUnit.SECONDS);

        // Check if the HashMap is empty and cancel the task if it is
        if (tpaList.isEmpty()) {
            scheduledTask.cancel(false);
        }
    }

    public void acceptTPA(Player executor, Player target) {
        if (tpaList.containsKey(executor) && tpaList.get(executor).contains(target)) {
            teleportPlayer(executor, target);
            executor.sendMessage(mm.deserialize(Language.getValue(telepads, executor, "tpa.acceped", true),
                    Placeholder.component("player", target.displayName())));
        }
    }

    public void declineTPA(Player executor, Player target) {
        if (tpaList.containsKey(executor) && tpaList.get(executor).contains(target)) {
            removePlayerFromTpaList(executor, target);
            target.sendMessage(mm.deserialize(Language.getValue(telepads, target, "tpadeclined.sender", true),
                    Placeholder.component("player", executor.displayName())));
            executor.sendMessage(mm.deserialize(Language.getValue(telepads, executor, "tpadeclined.reciever", true),
                    Placeholder.component("player", target.displayName())));
        } else {
            executor.sendMessage(mm.deserialize(Language.getValue(telepads, executor, "error.noTPA", true),
                    Placeholder.component("player", target.displayName())));
        }
    }

    // Method to remove a player from the tpaList
    public void removePlayerFromTpaList(Entity target, Entity executor) {
        List<Entity> executors = tpaList.get(target);
        if (executors != null) {
            executors.remove(executor);
            if (executors.isEmpty()) {
                tpaList.remove(target);
            }
        }
    }

    public void teleportPlayer(Player executor, Player target) {
        Long teleportTime = System.currentTimeMillis()
                + Telepads.INSTANCE.getConfig().getLong("TPA.DontMoveTimeInSeconds") * 1_000;

        Location startLocation = target.getLocation();

        removePlayerFromTpaList(executor, target);

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(telepads, () -> {

            if (System.currentTimeMillis() >= teleportTime) {
                notMoveTimer.get(target).cancel();
                notMoveTimer.remove(target);
                teleport(target, executor.getLocation());
                return;
            }

            if (startLocation.distance(target.getLocation()) > Telepads.INSTANCE.getConfig()
                    .getInt("Teleport.MoveTolerance")) {
                notMoveTimer.get(target).cancel();
                notMoveTimer.remove(target);
                target.sendMessage(
                        mm.deserialize(Language.getValue(telepads, target, "tpa.cancle.move", true)));
                return;
            }

            target.showTitle(Title.title(
                    mm.deserialize(Language.getValue(telepads, target, "tpa.countdown"),
                            Placeholder.component("countdown",
                                    Component.text(Instant.ofEpochMilli(teleportTime - System.currentTimeMillis())
                                            .getEpochSecond() + 1))),
                    Component.text(""),
                    Times.times(Duration.ofMillis(125), Duration.ofMillis(450), Duration.ofMillis(125))));

        }, 0, 20);
        notMoveTimer.put(target, task);
    }

    public void teleport(Player target, Location location) {
        if (isSafeLocation(location)) {
            target.teleport(location);
            target.sendMessage(mm.deserialize(Language.getValue(telepads, target, "teleport.succefully", true)));
        } else {
            target.sendMessage(mm.deserialize(Language.getValue(telepads, target, "teleport.unsafe", true)));
        }
    }

}
