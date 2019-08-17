package cc.flogi.smp.player.data;

import com.google.gson.annotations.Expose;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.Location;

/**
 * @author Caden Kriese (flogic)
 *
 * Created on 2019-08-11
 */
@Data @AllArgsConstructor public class Bookmark {
    @Expose(deserialize = false) String world;
    private String name;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;

    public Location getLocation() {
        return new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
    }
}
