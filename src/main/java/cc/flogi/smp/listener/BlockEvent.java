package cc.flogi.smp.listener;

import cc.flogi.smp.i18n.I18n;
import cc.flogi.smp.util.UtilUI;
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
        if (Arrays.stream(event.getLines()).anyMatch(line -> line.contains("\u00A7"))) {
            I18n.sendMessage(event.getPlayer(), "sign_colorized", true, true);
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
