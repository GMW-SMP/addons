package cc.flogi.smp.player.data;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Caden Kriese (flogic)
 *
 * Created on 2019-08-11
 */
@Data @AllArgsConstructor public class Bookmark {
    private String name;
    private double x;
    private double y;
    private double z;
    private double pitch;
    private double yaw;
}
