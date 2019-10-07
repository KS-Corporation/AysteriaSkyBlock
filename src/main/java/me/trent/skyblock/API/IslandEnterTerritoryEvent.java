package me.trent.skyblock.API;

import me.trent.skyblock.island.Island;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * @author Trent @ Aysteria Development
 * <p>
 * Event when a player enters an Island's zone
 */

public class IslandEnterTerritoryEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private Island island;
    private Player player;

    public IslandEnterTerritoryEvent(Player player, Island island) {
        this.player = player;
        this.island = island;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public Player getPlayer() {
        return player;
    }

    public Island getIsland() {
        return island;
    }
}