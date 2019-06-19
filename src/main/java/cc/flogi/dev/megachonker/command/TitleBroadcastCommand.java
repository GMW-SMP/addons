package cc.flogi.dev.megachonker.command;

import cc.flogi.dev.megachonker.util.UtilUI;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * @author Caden Kriese (flogic)
 *
 * Created on 2019-05-17
 */
@SuppressWarnings("NullableProblems") public class TitleBroadcastCommand implements CommandExecutor {
    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.isOp()) {
            StringBuilder title = new StringBuilder();
            StringBuilder subTitle = new StringBuilder();
            int fadeIn = 10;
            int stay = 20;
            int fadeOut = 10;
            boolean writeToSubtitle = false;
            for (String string : args) {
                if (string.startsWith("fadein:")) {
                    fadeIn = Integer.parseInt(string.substring("fadein:".length()));
                } else if (string.startsWith("stay:")) {
                    stay = Integer.parseInt(string.substring("stay:".length()));
                } else if (string.startsWith("fadeout:")) {
                    fadeOut = Integer.parseInt(string.substring("fadeout:".length()));
                } else {
                    string = string.replaceFirst("[%{][Nn][Ll][%}]", "<nl>");
                    String[] split = string.split("(?i)<nl>");

                    if (split.length > 1) {
                        title.append(title.toString().equals("") ? split[0] : " " + split[0]);
                        subTitle = new StringBuilder(split[1]);
                        if (split[1].equals("")) subTitle = new StringBuilder(" ");
                        writeToSubtitle = true;
                        continue;
                    }

                    if (!writeToSubtitle) {
                        title.append(title.toString().equals("") ? string : " " + string);
                    } else {
                        subTitle.append(" ").append(string);
                    }
                }
            }

            String finalTitle = UtilUI.colorize(title.toString());
            String finalSubtitle = UtilUI.colorize(subTitle.toString());
            int finalFadeIn = fadeIn;
            int finalFadeOut = fadeOut;
            int finalStay = stay;

            Bukkit.getOnlinePlayers().forEach(player -> player.sendTitle(
                                                              finalTitle,
                                                              finalSubtitle,
                                                              finalFadeIn,
                                                              finalStay,
                                                              finalFadeOut));
        } else {
            sender.sendMessage(UtilUI.colorize("&8[&cMegachonker&8] &7Insufficient permissions."));
        }

        return true;
    }
}
