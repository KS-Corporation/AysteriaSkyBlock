package me.trent.skyblock.events;

import com.vexsoftware.votifier.model.VotifierEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import me.trent.skyblock.SkyBlock;
import me.trent.skyblock.island.MemoryPlayer;

public class VoteEvents implements Listener {

    @EventHandler
    public void voteHandler(VotifierEvent e){
        String username = e.getVote().getUsername();
        Player p = Bukkit.getPlayerExact(username);
        if (p != null && p.isOnline()){
            MemoryPlayer memoryPlayer = SkyBlock.getInstance().getUtils().getMemoryPlayer(p.getUniqueId());
            memoryPlayer.setVotes(memoryPlayer.getVotes() + 1);
        }
    }
}