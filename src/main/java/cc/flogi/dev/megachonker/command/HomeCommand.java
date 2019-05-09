package cc.flogi.dev.megachonker.command;

import cc.flogi.dev.megachonker.util.Cooldown;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Caden Kriese (flogic)
 *
 * Created on 2019-05-08
 */
public class HomeCommand implements CommandExecutor {
    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (player.getBedSpawnLocation() != null)
                new Cooldown(player, 100, () -> player.teleport(player.getBedSpawnLocation()),
                        "Teleporting home in", "&aTeleportation complete.", true).start();
        }

        return false;
    }
}
