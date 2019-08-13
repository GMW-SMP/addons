package cc.flogi.smp.util;

import cc.flogi.smp.SMP;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * @author Caden Kriese (flogic)
 *
 * Created on 2019-08-11
 */
public class BookGUI {
    private ItemStack book;

    public BookGUI(List<BaseComponent[]> pages) {
        book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.spigot().setPages(pages);

        book.setItemMeta(meta);
    }

//    public BookGUI(List<String> pages) {
//        book = new ItemStack(Material.WRITTEN_BOOK);
//        BookMeta meta = (BookMeta) book.getItemMeta();
//        meta.setPages(pages.stream().map(UtilUI::colorize).collect(Collectors.toList()));
//
//        book.setItemMeta(meta);
//    }

    public void open(Player player) {
        ProtocolManager protocol = SMP.getInstance().getProtocolManager();

        PacketContainer bookPacket = protocol.createPacket(PacketType.Play.Server.OPEN_BOOK);

        bookPacket.getItemModifier().write(0, book);
        //Signing =  false;
        bookPacket.getBooleans().write(0, false);

        try {
            protocol.sendServerPacket(player, bookPacket);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
