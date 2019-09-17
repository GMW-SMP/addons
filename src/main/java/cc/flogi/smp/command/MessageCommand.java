package cc.flogi.smp.command;

import cc.flogi.smp.i18n.I18n;
import cc.flogi.smp.player.GamePlayer;
import cc.flogi.smp.player.PlayerManager;
import cc.flogi.smp.util.UtilUI;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.logging.Level;

/**
 * @author Caden Kriese (flogic)
 *
 * Created on 2019-08-17
 */
public class MessageCommand implements CommandExecutor {
    @Override public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage(UtilUI.colorize("&8[&cSMP&8] &7Please enter a player and a message."));
            return false;
        }

        if (sender instanceof Player) {
            Player player = (Player) sender;
            GamePlayer gamePlayer = PlayerManager.getInstance().getGamePlayer(player);
            ChatColor playerColor = gamePlayer.getNameColor();
            Player target = Bukkit.getPlayer(args[0]);
            String message = StringUtils.join(Arrays.copyOfRange(args, 1, args.length), ' ');

            if (player == target) {
                player.sendMessage(UtilUI.colorize("&8[&cSMP&8] &7You cannot message yourself."));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                return true;
            }

            if (label.equalsIgnoreCase("reply") || label.equalsIgnoreCase("r")) {
                if (gamePlayer.getLastMessaged() != null) {
                    target = Bukkit.getPlayer(gamePlayer.getLastMessaged());
                    message = StringUtils.join(args, ' ');
                    if (target == null)
                        gamePlayer.setLastMessaged(null);
                } else {
                    player.sendMessage(UtilUI.colorize("&8[&cSMP&8] &7You haven't messaged anyone recently."));
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                    return true;
                }
            }

            if (target != null) {
                GamePlayer targetPlayer = PlayerManager.getInstance().getGamePlayer(target);
                ChatColor targetColor = targetPlayer.getNameColor();

                if (message.length() > 0) {
                    String[] vars = new String[]{
                            "sc", playerColor.toString(),
                            "rc", targetColor.toString(),
                            "s", player.getName(),
                            "r", target.getName(),
                            "msg", message
                    };

                    I18n.logMessage("pm_send", Level.INFO, vars);
                    I18n.sendMessage(player, "pm_send", false, vars);
                    I18n.sendMessage(target, "pm_receive", false, vars);
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                    target.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                    gamePlayer.setLastMessaged(target.getUniqueId());
                    targetPlayer.setLastMessaged(player.getUniqueId());
                } else {
                    player.sendMessage(UtilUI.colorize("&8[&cSMP&8] &7That message is too short."));
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                }
            } else {
                player.sendMessage(UtilUI.colorize("&8[&cSMP&8] &7Invalid player."));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
            }
        } else {
            sender.sendMessage(UtilUI.colorize("&8[&cSMP&8] &7Only players can message."));
        }

        return false;
    }
}
