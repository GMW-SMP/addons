package cc.flogi.smp.command;

import cc.flogi.smp.player.GamePlayer;
import cc.flogi.smp.player.PlayerManager;
import cc.flogi.smp.util.UtilUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * @author Caden Kriese (flogic)
 *
 * Created on 2019-08-07
 */
public class BookmarkCommand implements CommandExecutor {
    @Override public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (sender instanceof Player) {
            GamePlayer gp = PlayerManager.getInstance().getGamePlayer((Player) sender);
            gp.getBookmarks().add(((Player) sender).getLocation());
            sender.sendMessage(UtilUI.colorize("&8[&aSMP&8] &7Added location to your bookmarks."));
        }

        return false;
    }
}
