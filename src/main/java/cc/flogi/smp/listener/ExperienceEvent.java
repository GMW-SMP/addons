package cc.flogi.smp.listener;

import cc.flogi.smp.SMP;
import cc.flogi.smp.i18n.I18n;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ExpBottleEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.inventory.Inventory;
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
            int amount = Math.min(player.getTotalExperience(), 50);
            meta.setLore(Collections.singletonList(I18n.getMessage(player, "add_xp_prompt",
                    "amount", amount + "xp")));
            product.setItemMeta(meta);
            event.getInventory().setResult(product);
        }
    }

    @EventHandler
    public void onInventoryInteract(InventoryClickEvent event) {
        if (event.getClickedInventory() == null)
            return;

        ItemStack item = event.getCurrentItem();

        if (item != null && item.getType() == Material.GLASS_BOTTLE) {
            if (event.getClick() == ClickType.SHIFT_RIGHT) {
                event.setCancelled(true);
                Player player = (Player) event.getWhoClicked();
                if (item.getAmount() > 1)
                    item.setAmount(item.getAmount() - 1);
                else
                    player.getInventory().remove(item);

                ItemStack bottle = new ItemStack(Material.EXPERIENCE_BOTTLE);
                ItemMeta meta = bottle.getItemMeta();
                int amount = Math.min(player.getTotalExperience(), 50);
//                player.setTotalExperience(player.getTotalExperience() - amount);
                player.setTotalExperience(0);
                player.setLevel(0);
                player.setExp(0);
                player.giveExp(player.getTotalExperience() - amount);
                meta.setLore(Collections.singletonList(I18n.getMessage(player, "xp_amount",
                        "amount", amount + "xp")));
                meta.getPersistentDataContainer().set(new NamespacedKey(SMP.get(), "xp_amount"),
                        PersistentDataType.INTEGER, amount);
                bottle.setItemMeta(meta);
                player.getInventory().addItem(bottle);
            }
        }
    }

//    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
//    public void onProjectileLaunch(ProjectileLaunchEvent event) {
//        //TODO handle other shooters.
//        if (event.getEntity().getShooter() instanceof Player) {
//            Player player = (Player) event.getEntity().getShooter();
//            if (event.getEntity() instanceof ThrownExpBottle) {
//                ItemStack stack = player.getInventory().getItemInMainHand();
//                NamespacedKey key = new NamespacedKey(SMP.get(), "xp_amount");
//                ItemMeta meta = stack.getItemMeta();
//                float amount = meta.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
//                ThrownExpBottle bottle = (ThrownExpBottle) event.getEntity();
//                bottle.getPersistentDataContainer().set(key, PersistentDataType.FLOAT, amount);
//            }
//        } else if (event.getEntity().getShooter() instanceof Dispenser) {
//            Dispenser dispenser = (Dispenser) event.getEntity().getShooter();
//        }
//    }
//
//    @EventHandler
//    public void onInteract(PlayerInteractEvent event) {
//        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
//            ItemStack item = event.getItem();
//            if (item != null && item.getType() == Material.EXPERIENCE_BOTTLE && item.hasItemMeta()) {
//                Float amount = item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(SMP.get(), "xp_amount"), PersistentDataType.FLOAT);
//                if (amount != null) {
//
//                }
//            }
//        }
//    }

    @SuppressWarnings("ConstantConditions") @EventHandler
    public void onExpBottle(ExpBottleEvent event) {
        NamespacedKey key = new NamespacedKey(SMP.get(), "xp_amount");
        ItemMeta meta = event.getEntity().getItem().getItemMeta();
        if (meta == null)
            return;

        event.setExperience(meta.getPersistentDataContainer().get(key, PersistentDataType.INTEGER));
    }

    @EventHandler
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            int amount = Math.min(player.getTotalExperience(), 50);
            ItemStack item = event.getItem().getItemStack();
            ItemMeta meta = item.getItemMeta();
            meta.setLore(Collections.singletonList(I18n.getMessage(player, "add_xp_prompt",
                    "amount", amount + "xp")));
            item.setItemMeta(meta);
            event.getItem().remove();
            event.setCancelled(true);
            ((Player) event.getEntity()).getInventory().addItem(item);
        }
    }

    //FIXME may create bugs in inventories with multiple viewers.
    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        updateBottles((Player) event.getPlayer(), event.getInventory());
    }

    @EventHandler
    public void onPlayerExpChange(PlayerExpChangeEvent event) {
        updateBottles(event.getPlayer(), event.getPlayer().getInventory());
    }

    private void updateBottles(Player player, Inventory inventory) {
        int amount = Math.min(player.getTotalExperience(), 50);
        ItemStack[] items = inventory.getContents();

        inventory.setContents(Arrays.stream(items).peek(item -> {
            if (item != null && item.getType() == Material.GLASS_BOTTLE) {
                ItemMeta meta = item.getItemMeta();
                meta.setLore(Collections.singletonList(I18n.getMessage(player, "add_xp_prompt",
                        "amount", amount + "xp")));
                item.setItemMeta(meta);
            }
        }).toArray(ItemStack[]::new));
    }
}
