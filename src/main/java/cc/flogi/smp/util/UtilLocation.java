package cc.flogi.smp.util;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;

/**
 * @author Caden Kriese (flogic)
 *
 * Created on 2019-08-07
 */
public class UtilLocation {
    private static final BlockFace[] RADIAL = {
            BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST,
            BlockFace.NORTH_WEST, BlockFace.NORTH, BlockFace.NORTH_EAST,
            BlockFace.EAST, BlockFace.SOUTH_EAST};

    /**
     * Gets the direction of one block relative to another.
     *
     * @param locA The base location.
     * @param locB The relative location.
     * @return The block face of locB relative to locA.
     */
    public static BlockFace getRelativeDirection(Location locA, Location locB) {
        locA.setDirection(locB.toVector().subtract(locA.toVector()));
        float yaw = locA.getYaw();

        return RADIAL[Math.round(yaw / 45f) & 0x7];
    }
}
