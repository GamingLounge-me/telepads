package de.jonas.telepads.commands;

import org.bukkit.Axis;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Beacon;
import org.bukkit.block.data.Orientable;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.persistence.PersistentDataType;
import de.jonas.stuff.Stuff;
import de.jonas.stuff.commandapi.CommandAPICommand;
import de.jonas.stuff.interfaced.PlaceEvent;
import de.jonas.stuff.utility.ItemBuilder;
import de.jonas.telepads.DataBasePool;
import de.jonas.telepads.Telepads;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class GiveBuildItem { 

    MiniMessage mm = MiniMessage.miniMessage();
    Telepads telepads = Telepads.INSTANCE;
    DataBasePool db = telepads.basePool;

    public static final NamespacedKey telepadNum = new NamespacedKey("telepads", "identification_number");
    public final PlaceEvent buildTelepad = this::buildTelepadI;

    private void buildTelepadI(BlockPlaceEvent pv) {
        Location loc,l1,l2,l3,l4,l5,l6,l7,l8;
        loc = pv.getBlock().getLocation();
        l1 = pv.getBlock().getLocation().add(1, 0, 1);
        l2 = pv.getBlock().getLocation().add(1, 0, 0);
        l3 = pv.getBlock().getLocation().add(1, 0, -1);
        l4 = pv.getBlock().getLocation().add(0, 0, 1);
        l5 = pv.getBlock().getLocation().add(0, 0, -1);
        l6 = pv.getBlock().getLocation().add(-1, 0, -1);
        l7 = pv.getBlock().getLocation().add(-1, 0, 0);
        l8 = pv.getBlock().getLocation().add(-1, 0, 1);

        if (l1.getBlock().getType().isAir() &&
            l2.getBlock().getType().isAir() &&
            l3.getBlock().getType().isAir() &&
            l4.getBlock().getType().isAir() &&
            l5.getBlock().getType().isAir() &&
            l6.getBlock().getType().isAir() &&
            l7.getBlock().getType().isAir() &&
            l8.getBlock().getType().isAir() &&
            pv.getBlockPlaced().getState(false) instanceof Beacon b
            ) {
                int id = DataBasePool.setNewTelepad(db, pv.getPlayer().getUniqueId(),  pv.getBlock().getLocation());
                if (id == -1) {
                    pv.getPlayer().sendMessage(mm.deserialize("<red>Es gab einen Fehler beim erstellen des Telepads in der Datenbank, bitte kontactiere einen Administrator!</red>"));
                    return;
                }
                b.getPersistentDataContainer().set(telepadNum, PersistentDataType.INTEGER, id);
                loc.getWorld().setType(l1, Material.DARK_PRISMARINE);
                loc.getWorld().setType(l3, Material.DARK_PRISMARINE);
                loc.getWorld().setType(l6, Material.DARK_PRISMARINE);
                loc.getWorld().setType(l8, Material.DARK_PRISMARINE);
                loc.getWorld().setType(l2, Material.STRIPPED_WARPED_HYPHAE);
                orientBlock(l2, Axis.X);
                loc.getWorld().setType(l4, Material.STRIPPED_WARPED_HYPHAE);
                orientBlock(l4, Axis.Z);
                loc.getWorld().setType(l5, Material.STRIPPED_WARPED_HYPHAE);
                orientBlock(l5, Axis.X);
                loc.getWorld().setType(l7, Material.STRIPPED_WARPED_HYPHAE);
                orientBlock(l7, Axis.Z);
                pv.getPlayer().sendMessage(mm.deserialize("<green>Dein Telepad wurde Erstellt.</green>"));
        } else {
            pv.setCancelled(true);
        }
    }

    public GiveBuildItem() {

        Stuff.INSTANCE.itemBuilderManager.addPlaceEvent(buildTelepad, "telepads:buildTelepad");

        new CommandAPICommand("telepad:giveBuildItem")
        .withPermission("telepads.givebuilditem")
        .executesPlayer((player, arg) -> {
            player.getInventory().addItem(new ItemBuilder()
                .setMaterial(Material.BEACON)
                .setName("Telepad")
                .whenPlaced("telepads:buildTelepad")
                .build()
            );
        })
        .register();
    }

    public static void orientBlock(Location loc, Axis a) {
        if(loc.getBlock().getBlockData() instanceof Orientable o) {
            o.setAxis(a);
            loc.getBlock().setBlockData(o);
        }
    }

}
