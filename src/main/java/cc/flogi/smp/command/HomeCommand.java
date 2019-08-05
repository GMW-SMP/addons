package cc.flogi.smp.command;

import cc.flogi.smp.util.Cooldown;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Caden Kriese (flogic)
 *
 * Created on 2019-05-08
 */
@SuppressWarnings("NullableProblems") public class HomeCommand implements CommandExecutor {
    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (player.getBedSpawnLocation() != null)
                new Cooldown(player, 1, 100, () -> player.teleport(player.getBedSpawnLocation()),
                        "&6Teleporting home... {2} &7(&e{0}s&7)", "&aTeleportation complete.", true).start();
        }

        return true;
    }
}