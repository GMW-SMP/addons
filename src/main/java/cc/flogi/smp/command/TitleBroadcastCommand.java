package cc.flogi.smp.command;

import cc.flogi.smp.util.UtilUI;
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
                try {
                    if (string.startsWith("fadein:")) {
                        fadeIn = Integer.parseInt(string.substring("fadein:".length()));
                    } else if (string.startsWith("stay:")) {
                        stay = Integer.parseInt(string.substring("stay:".length()));
                    } else if (string.startsWith("fadeout:")) {
                        fadeOut = Integer.parseInt(string.substring("fadeout:".length()));
                    } else {
                        //Matches %nl% & {nl} case insensitive.
                        string = string.replaceFirst("[%{][Nn][Ll][%}]", "<nl>");

                        if (string.equalsIgnoreCase("<nl>")) {
                            writeToSubtitle = true;
                            continue;
                        }

                        if (string.contains("<nl>")) {
                            String titleAppend = string.substring(0, string.indexOf("<nl>")).replace("<nl>", "");
                            if (title.length() > 0)
                                titleAppend = " " + titleAppend;
                            title.append(titleAppend);

                            subTitle.append(string.substring(string.indexOf("<nl>")).replace("<nl>", ""));
                            writeToSubtitle = true;
                            continue;
                        }

                        if (!writeToSubtitle) {
                            title.append(title.toString().equals("") ? string : " " + string);
                        } else {
                            subTitle.append(" ").append(string);
                        }
                    }
                } catch (NumberFormatException ex) {
                    sender.sendMessage(UtilUI.colorize("&8[&cSMP&8] &7That's not a valid number!"));
                    return true;
                }
            }

            String finalTitle = UtilUI.colorize(title.toString()).replace("\\t", "    ");
            String finalSubtitle = UtilUI.colorize(subTitle.toString()).replace("\\t", "    ");
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
            sender.sendMessage(UtilUI.colorize("&8[&cSMP&8] &7Insufficient permissions."));
        }

        return true;
    }
}
