package cc.flogi.smp.util;

import cc.flogi.smp.SMP;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;

/**
 * @author Caden Kriese (flogic)
 *
 * Created on 07/19/2020
 */
public class UtilProtocol {
    public static void broadcastPacketToAllExcept(Player player, PacketContainer packet) {
        Bukkit.getOnlinePlayers().stream().filter(p -> p != player).forEach(p -> {
            try {
                SMP.get().getProtocolManager().sendServerPacket(player, packet);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        });
    }
}
