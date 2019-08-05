package cc.flogi.smp.command;

import cc.flogi.smp.SMP;
import cc.flogi.smp.player.GamePlayer;
import cc.flogi.smp.player.PlayerManager;
import cc.flogi.smp.util.UtilUI;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang3.EnumUtils;
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
    @Override public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            GamePlayer gamePlayer = PlayerManager.getInstance().getGamePlayer(player);

            if (args.length > 0) {
                if (EnumUtils.isValidEnum(ChatColor.class, args[0].toUpperCase())) {
                    ChatColor color = ChatColor.valueOf(args[0].toUpperCase());
                    gamePlayer.setNameColor(color);

                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
                    sender.sendMessage(UtilUI.colorize("&8[&aSMP&8] &7Name color set to '" + color + color.name() + "&7'"));

                    SMP.getInstance().getConfig().set("players." + player.getUniqueId().toString() + ".name-color", color.getName());
                    SMP.getInstance().saveConfig();

                    return true;
                }
            }

            sender.sendMessage(UtilUI.colorize("&8[&cSMP&8] &7Invalid color, here's a list of colors to choose from:"));
            for (ChatColor value : ChatColor.values()) {
                if (!value.name().equals("MAGIC"))
                    sender.sendMessage(value + "- " + value.getName());
            }
        }

        return true;
    }
}
