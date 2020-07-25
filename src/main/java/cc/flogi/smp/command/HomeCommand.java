package cc.flogi.smp.command;

import cc.flogi.smp.i18n.I18n;
import cc.flogi.smp.util.Cooldown;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.type.RespawnAnchor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * @author Caden Kriese (flogic)
 *
 * Created on 2019-05-08
 */
public class HomeCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            RespawnAnchor anchor = null;
            if (player.getPotentialBedLocation() != null && player.getPotentialBedLocation().getBlock().getType() == Material.RESPAWN_ANCHOR) {
                anchor = (RespawnAnchor) player.getPotentialBedLocation().getBlock().getBlockData();
                if (anchor.getCharges() == 0) {
                    I18n.sendError(player, "no_valid_home", true);
                    return true;
                }
            }

            if (player.getBedSpawnLocation() != null && player.getWorld() == player.getBedSpawnLocation().getWorld()) {
                RespawnAnchor finalAnchor = anchor;
                new Cooldown(player, 2, 100, () -> {
                    player.teleport(player.getBedSpawnLocation());
                    if (finalAnchor != null) {
                        finalAnchor.setCharges(finalAnchor.getCharges() - 1);
                        player.getPotentialBedLocation().getBlock().setBlockData(finalAnchor);
                    }
                },
                        I18n.getMessage(player, "teleporting_home",
                                "time", "{0}",
                                "seconds", "{1}",
                                "bar", "{2}"),
                        I18n.getMessage(player, "teleported_home"),
                        true
                ).start();
            } else {
                I18n.sendError(player, "no_valid_home", true);
            }
        } else {
            I18n.sendError(sender, "must_be_player", true);
        }

        return true;
    }
}
