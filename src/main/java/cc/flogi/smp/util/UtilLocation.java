package cc.flogi.smp.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Caden Kriese (flogic)
 *
 * Created on 2019-08-07
 */
public class UtilLocation {
    public static Map<String, Object> convertMap(Map<?, ?> location) {
        Map<String, Object> convertedLocation = new HashMap<>();

        if (!location.isEmpty()) {
            for (Map.Entry<?, ?> entry : location.entrySet()) {
                if (entry.getKey() instanceof String) {
                    convertedLocation.put((String) entry.getKey(), entry.getValue());
                }
            }
        }

        return convertedLocation;
    }
}
