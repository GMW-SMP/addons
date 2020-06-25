package cc.flogi.smp.command;

import cc.flogi.smp.i18n.I18n;
import cc.flogi.smp.util.Cooldown;
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

            //TODO Make teleporting to a respawn anchor remove a charge.
            if (player.getBedSpawnLocation() != null) {
                new Cooldown(player, 2, 100, () -> player.teleport(player.getBedSpawnLocation()),
                        I18n.getMessage(player, "teleporting_home",
                                "time", "{0}",
                                "seconds", "{1}",
                                "bar", "{2}"),
                        I18n.getMessage(player, "teleported_home"),
                        true).start();
            } else {
                I18n.sendError(player, "no_valid_home", true);
            }
        } else {
            I18n.sendError(sender, "must_be_player", true);
        }

        return false;
    }
}
