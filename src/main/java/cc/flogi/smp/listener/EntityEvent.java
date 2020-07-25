package cc.flogi.smp.listener;

import cc.flogi.smp.util.UtilThreading;
import org.bukkit.Material;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

/**
 * @author Caden Kriese (flogic)
 *
 * Created on 07/25/2020
 */
public class EntityEvent implements Listener {
    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        //Big buff for Wither
        if (event.getEntityType() == EntityType.WITHER) {
            event.setDroppedExp(3000);
        } else if (event.getEntityType() == EntityType.ENDER_DRAGON) {
            EnderDragon dragon = (EnderDragon) event.getEntity();
            handleDragonDrops(dragon);
        }
    }

    private void handleDragonDrops(EnderDragon dragon) {
        if (dragon.getDeathAnimationTicks() != 0) {
            UtilThreading.syncDelayed(() -> {
                dragon.getWorld().dropItem(dragon.getLocation(), new ItemStack(Material.ELYTRA));
                if (dragon.getDragonBattle() != null && dragon.getDragonBattle().hasBeenPreviouslyKilled()) {
                    ((ExperienceOrb) dragon.getWorld().spawnEntity(dragon.getLocation(), EntityType.EXPERIENCE_ORB)).setExperience(1900);
                    for (int i = 0; i < 10; i++) {
                        ((ExperienceOrb) dragon.getWorld().spawnEntity(dragon.getLocation(), EntityType.EXPERIENCE_ORB)).setExperience(960);
                    }
                }
            }, dragon.getDeathAnimationTicks());
        } else {
            //Wait three seconds and check again
            UtilThreading.syncDelayed(() -> handleDragonDrops(dragon), 60);
        }
    }

//    @EventHandler
//    public void onPhaseChange(EnderDragonChangePhaseEvent event) {
//        EnderDragon dragon = event.getEntity();
//        if (event.getNewPhase() == EnderDragon.Phase.DYING) {
//            Bukkit.broadcastMessage("Delay - " + dragon.getDeathAnimationTicks());
//            UtilThreading.syncDelayed(() -> {
//                Bukkit.broadcastMessage("DROPPING SPECIAL ITEMS");
//                dragon.getWorld().dropItem(dragon.getLocation(), new ItemStack(Material.ELYTRA));
//                ((ExperienceOrb) dragon.getWorld().spawnEntity(dragon.getLocation(), EntityType.EXPERIENCE_ORB)).setExperience(1900);
//                for (int i = 0; i < 10; i++) {
//                    ((ExperienceOrb) dragon.getWorld().spawnEntity(dragon.getLocation(), EntityType.EXPERIENCE_ORB)).setExperience(960);
//                }
//            }, dragon.getDeathAnimationTicks());
//        }
//    }
}
