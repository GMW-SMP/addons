package cc.flogi.smp.listener;

import cc.flogi.smp.SMP;
import cc.flogi.smp.i18n.I18n;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ExpBottleEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.Collections;

/**
 * @author Caden Kriese
 *
 * Manages events in relation to the custom experience storage system.
 *
 * Created on 02/11/2020.
 */
public class ExperienceEvent implements Listener {
    @EventHandler
    public void onPrepareItemCraft(PrepareItemCraftEvent event) {
        if (event.getRecipe() == null)
            return;

        ItemStack product = event.getRecipe().getResult();

        if (product.getType() == Material.GLASS_BOTTLE) {
            Player player = (Player) event.getInventory().getViewers().get(0);
            ItemMeta meta = product.getItemMeta();
            float amount = Math.min(player.getExp(), 5f);
            meta.setLore(Collections.singletonList(I18n.getMessage(player, "add_xp_prompt",
                    "amount", amount + " lvls")));
        }
    }

    @EventHandler
    public void onInventoryInteract(InventoryClickEvent event) {
        if (event.getInventory().getType() == InventoryType.PLAYER || event.getInventory().getType() == InventoryType.ENDER_CHEST) {
            if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.GLASS_BOTTLE) {
                if (event.getClick() == ClickType.SHIFT_LEFT) {
                    ItemStack item = event.getCurrentItem();
                    Player player = (Player) event.getWhoClicked();
                    float amount = Math.min(player.getExp(), 5f);
                    item.setType(Material.EXPERIENCE_BOTTLE);
                    item.setLore(Collections.singletonList(I18n.getMessage(player, "xp_amount",
                            "amount", amount + " lvls")));

                    item.getItemMeta().getPersistentDataContainer().set(new NamespacedKey(SMP.get(), "xp_amount"),
                            PersistentDataType.FLOAT, amount);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        //TODO handle other shooters.
        if (event.getEntity().getShooter() instanceof Player) {
            Player player = (Player) event.getEntity().getShooter();
            if (event.getEntity() instanceof ThrownExpBottle) {
                ItemStack stack = player.getInventory().getItemInMainHand();
                ThrownExpBottle bottle = (ThrownExpBottle) event.getEntity();
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack item = event.getItem();
            if (item != null && item.getType() == Material.EXPERIENCE_BOTTLE && item.hasItemMeta()) {
                Float amount = item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(SMP.get(), "xp_amount"), PersistentDataType.FLOAT);
                if (amount != null) {

                }
            }
        }
    }

    @EventHandler
    public void onExpBottle(ExpBottleEvent event) {
        event.getEntity().getItem().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(SMP.get(), "xp_amount"), PersistentDataType.FLOAT);
    }

    @EventHandler
    public void onInventoryEvent(InventoryOpenEvent event) {
        if (event.getInventory().getType() == InventoryType.PLAYER || event.getInventory().getType() == InventoryType.ENDER_CHEST)
            updateBottles((Player) event.getPlayer(), event.getInventory().getContents());
    }

    private void updateBottles(Player player, ItemStack... items) {
        float amount = Math.min(player.getExp(), 5f);
        Arrays.stream(items).forEach(item -> {
            if (item.getType() == Material.GLASS_BOTTLE) {
                ItemMeta meta = item.getItemMeta();
                meta.setLore(Collections.singletonList(I18n.getMessage(player, "add_xp_prompt",
                        "amount", Float.toString(amount))));
            }
        });
    }
}
