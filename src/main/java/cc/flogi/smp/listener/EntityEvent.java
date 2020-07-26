package cc.flogi.smp.listener;

import cc.flogi.smp.SMP;
import cc.flogi.smp.util.UtilThreading;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

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

    @EventHandler
    public void onCreeperExplode(EntityExplodeEvent event) {
        if (event.getEntity() instanceof Creeper)
            event.setYield(1);

        List<Block> blockList = event.blockList().stream().filter(block -> (block.getLocation().getY() - event.getLocation().getY() < 2)).collect(Collectors.toList());
        for (Block block : blockList) {
            //Generate vector in random direction.
            Random rand = new Random();
            double x = (rand.nextDouble() - rand.nextDouble()) / 1.5;
            double y = rand.nextDouble();
            double z = (rand.nextDouble() - rand.nextDouble()) / 1.5;

            FallingBlock fall = block.getWorld().spawnFallingBlock(block.getLocation(), block.getBlockData());
            fall.setDropItem(false);
            fall.setHurtEntities(true);
            fall.setVelocity(new Vector(x, y, z));
            fall.setMetadata("debris", new FixedMetadataValue(SMP.get(), true));
        }
    }

    @EventHandler
    public void onBlockChange(EntityChangeBlockEvent event) {
        if (event.getEntityType() == EntityType.FALLING_BLOCK) {
            if (event.getEntity().hasMetadata("debris") && event.getEntity().getMetadata("debris").get(0).asBoolean()) {
                event.setCancelled(true);
            }
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
}
