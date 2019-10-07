package me.trent.skyblock.events;

import me.trent.skyblock.API.IslandCreatedEvent;
import me.trent.skyblock.SkyBlock;
import me.trent.skyblock.island.Island;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.server.PluginDisableEvent;

public class IslandEvents implements Listener {

    @EventHandler
    public void rel(PluginDisableEvent e){
        if (e.getPlugin().equals(SkyBlock.getInstance())){
            Bukkit.getScheduler().cancelTasks(SkyBlock.getInstance());
        }
    }

    @EventHandler
    public void islandCreate(IslandCreatedEvent e) {
        Island island = e.getIsland();
        //when an island is created without fail
        SkyBlock.getInstance().getIslandUtils().calculateIslandLevel(island);
    }

    @EventHandler
    public void move(PlayerMoveEvent e){
        Player p = e.getPlayer();
        if (p.getLocation().getY() <= -10){
            //teleport them to their island
            SkyBlock.getInstance().safePlayers.add(p);
            //check if they have an island first, then teleport, if not teleport to is spawn
            if (SkyBlock.getInstance().getIslandUtils().getIsland(p.getUniqueId()) != null) {
                p.teleport(SkyBlock.getInstance().getIslandUtils().getIsland(p.getUniqueId()).getHome());
            }else{
                //is null, teleport to is spawn
                Location spawnLoc = SkyBlock.getInstance().getIslandUtils().getIslandSpawn();
               if (spawnLoc != null){
                   p.teleport(spawnLoc);
               }else{
                   p.sendMessage(SkyBlock.getInstance().getUtils().getMessage("noSpawn"));
                   p.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
               }
            }
            SkyBlock.getInstance().safePlayers.remove(p);
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void damage(EntityDamageEvent e){
        if (e.getEntity() instanceof Player){
            Player p = (Player)e.getEntity();
            if (SkyBlock.getInstance().safePlayers.contains(p)) {
                e.setCancelled(true);
            }
        }
    }
}