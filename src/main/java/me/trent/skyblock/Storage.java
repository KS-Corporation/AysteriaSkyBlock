package me.trent.skyblock;

import me.trent.skyblock.commands.CommandBase;
import me.trent.skyblock.island.Island;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import me.trent.skyblock.island.MemoryPlayer;
import me.trent.skyblock.island.quests.Quest;

import java.util.*;

public class Storage {

    public static Set<CommandBase> commandSet = new HashSet<CommandBase>();

    public static List<UUID> scoreboard_toggled = new ArrayList<>();

    public static List<String> permissionToCache = Arrays.asList("skyblock.block.hopper", "skyblock.block.spawner", "skyblock.members", "skyblock.protection", "skyblock.resets");

    public static List<String> scoreboard_worlds = new ArrayList<>();

    public static HashMap<UUID, List<Quest>> completedQuestMessageQueue = new HashMap<>();

    public static int currentTop = 1;

    public static List<Island> islandList = new ArrayList<>();
    public static List<MemoryPlayer> memoryPlayerList = new ArrayList<>();

    private static World skyBlockWorld = Bukkit.getWorld(SkyBlock.getInstance().getConfig().getString("settings.world-name"));

    public static World getSkyBlockWorld() {
        return skyBlockWorld;
    }

    public static double defaultY = 100;

    public static Location minLocation(){
        return new Location(getSkyBlockWorld(), SkyBlock.getInstance().getUtils().getSettingInt("min-x"), defaultY, SkyBlock.getInstance().getUtils().getSettingInt("min-z"));
    }
    public static Location maxLocation(){
        return new Location(getSkyBlockWorld(), SkyBlock.getInstance().getUtils().getSettingInt("max-x"), defaultY, SkyBlock.getInstance().getUtils().getSettingInt("max-z"));
    }

}