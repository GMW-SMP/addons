package cc.flogi.smp.command;

import cc.flogi.smp.i18n.I18n;
import cc.flogi.smp.player.GamePlayer;
import cc.flogi.smp.player.PlayerManager;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * @author Caden Kriese (flogic)
 *
 * Created on 2019-05-11
 */
public class SetColorCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            GamePlayer gamePlayer = PlayerManager.getInstance().getGamePlayer(player);

            if (args.length > 0) {
                try {
                    ChatColor color = ChatColor.of(args[0].toUpperCase());
                    gamePlayer.setNameColor(color);
                    PlayerManager.getInstance().saveToFile(gamePlayer);

                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
                    I18n.sendMessage(player, "name_color_set", true,
                            "c", color.toString(),
                            "cname", color.getName());

                    return true;
                } catch (IllegalArgumentException ex) {
                    ex.printStackTrace();

                    I18n.sendError(player, "invalid_color", true);
                    return true;
                }
            }

            I18n.sendError(player, "invalid_color", true);
        } else
            I18n.sendError(sender, "must_be_player", true);

        return true;
    }
}
