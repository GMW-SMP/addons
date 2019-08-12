package cc.flogi.smp.command;

import cc.flogi.smp.player.GamePlayer;
import cc.flogi.smp.player.PlayerManager;
import cc.flogi.smp.util.BookGUI;
import cc.flogi.smp.util.UtilUI;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

/**
 * @author Caden Kriese (flogic)
 *
 * Created on 2019-08-07
 */
public class BookmarkCommand implements CommandExecutor {
    @Override public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            GamePlayer gp = PlayerManager.getInstance().getGamePlayer(player);

            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("list")) {
                    StringBuilder bookmarks = new StringBuilder();

                    //TODO NPE on next line?
                    gp.getBookmarks().forEach(mark -> bookmarks.append(ChatColor.RED)
                                                              .append(mark.getName())
                                                              .append(" &7&l| &a")
                                                              .append(mark.getX())
                                                              .append(", ")
                                                              .append(mark.getY())
                                                              .append(", ")
                                                              .append(mark.getZ()));

                    new BookGUI(Collections.singletonList(bookmarks.toString())).open(player);
                }
            }

            String name = StringUtils.join(args, ' ');
            gp.addBookmark((player).getLocation(), name);
            PlayerManager.getInstance().saveToFile(gp);

            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
            player.sendMessage(UtilUI.colorize("&8[&aSMP&8] &7Added location to your bookmarks."));
        }

        return false;
    }
}
