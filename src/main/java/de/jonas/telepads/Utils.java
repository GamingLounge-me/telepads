package de.jonas.telepads;

import org.bukkit.Axis;
import org.bukkit.Location;
import org.bukkit.block.data.Orientable;

public class Utils {

    public static void orientBlock(Location loc, Axis a) {
        if (loc.getBlock().getBlockData() instanceof Orientable o) {
            o.setAxis(a);
            loc.getBlock().setBlockData(o);
        }
    }

}
