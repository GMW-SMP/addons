package cc.flogi.dev.megachonker.listener;

import cc.flogi.dev.megachonker.util.UtilUI;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

/**
 * @author Caden Kriese (flogic)
 *
 * Created on 2019-05-17
 */
public class BlockEvent implements Listener {
    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        //Colorize sign lines.
        for (int i = 0; i < event.getLines().length; i++) {
            event.setLine(i, UtilUI.colorize(event.getLine(i)));
        }

        //Notify player. '\u00A7' is a section sign, similar to an ampersand. MC Uses it for color codes.
        Player player = event.getPlayer();
        if (Arrays.stream(event.getLines()).anyMatch(line -> line.contains("\u00A7"))) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
            player.sendMessage(UtilUI.colorize("&8[&aSMP&8] &7Colorized sign text."));
        }
    }

    @EventHandler
    public void onAnvilEvent(PrepareAnvilEvent event) {
        if (event.getResult() != null && event.getInventory().getRenameText() != null) {
            String colorizedText = UtilUI.colorize(event.getInventory().getRenameText());

            if (colorizedText.contains("\u00A7") && event.getResult().hasItemMeta()) {
                ItemMeta meta = event.getResult().getItemMeta();
                meta.setDisplayName(colorizedText);
                event.getResult().setItemMeta(meta);
            }
        }
    }
}
