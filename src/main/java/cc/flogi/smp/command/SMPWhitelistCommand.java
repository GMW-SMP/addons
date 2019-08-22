package cc.flogi.smp.command;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * @author Caden Kriese (flogic)
 *
 * Created on 8/21/19
 */
public class SMPWhitelistCommand implements CommandExecutor {
    @Override public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length > 0) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
            player.setWhitelisted(true);
            sender.sendMessage("[SMP] '" + player.getName() + "' added to the whitelist.");
        }

        return false;
    }
}
