package me.trent.skyblock.island;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import me.trent.skyblock.API.IslandCreateEvent;
import me.trent.skyblock.API.IslandCreatedEvent;
import me.trent.skyblock.SkyBlock;
import me.trent.skyblock.Storage;
import me.trent.skyblock.island.permissions.Perm;
import me.trent.skyblock.island.permissions.Perms;
import me.trent.skyblock.island.permissions.Role;
import me.trent.skyblock.island.rules.Rule;
import me.trent.skyblock.island.rules.Rules;
import me.trent.skyblock.island.warps.IslandWarp;

import java.util.*;

public class IslandUtils {

    public Role getRole(UUID uuid, Island island){
        if (isOwner(uuid, island)){
            return Role.OWNER;
        }
        if (isCoOwner(uuid, island)){
            return Role.COOWNER;
        }
        if (isOfficer(uuid, island)){
            return Role.OFFICER;
        }
        if (isMember(uuid, island)){
            return Role.MEMBER;
        }
        return Role.VISITOR;
    }

    public Location getIslandSpawn() {
        String s = SkyBlock.getInstance().getConfig().getString("settings.island-spawn");
        return SkyBlock.getInstance().getUtils().deserializeLocation(s);
    }

    public void createIsland(Player p, String permission, String schematicName) {
        if (!inIsland(p.getUniqueId())) {
            if (p.hasPermission(permission)) {

                IslandCreateEvent createEvent = new IslandCreateEvent(p.getUniqueId());
                Bukkit.getPluginManager().callEvent(createEvent);

                if (!createEvent.isCancelled()) {
                    Location location = SkyBlock.getInstance().getUtils().generateIslandLocation(Storage.minLocation(), Storage.maxLocation());
                    Island island = new Island(schematicName, location.getBlockX(), location.getBlockY(), location.getBlockZ(), p.getUniqueId(), new ArrayList<>(),
                            new ArrayList<>(), new ArrayList<>(), SkyBlock.getInstance().getUtils().getSettingInt("default-protection-radius"), p.getName(), 0);
                    p.sendMessage(SkyBlock.getInstance().getUtils().getMessage("createIsland"));

                    Bukkit.getPluginManager().callEvent(new IslandCreatedEvent(p.getUniqueId(), island));

                    SkyBlock.getInstance().getUtils().getMemoryPlayer(p.getUniqueId()).setIsland(island);

                }
            } else {
                p.sendMessage(SkyBlock.getInstance().getUtils().getMessage("noIslandPermissionCreate"));
            }
        } else {
            p.sendMessage(SkyBlock.getInstance().getUtils().getMessage("alreadyIsland"));
        }
    }

    public Island getIsland(UUID uuid) {
        for (Island island : Storage.islandList) {
            if (island.getOwnerUUID().equals(uuid) || island.getMemberList().contains(uuid) || island.getCoownerList().contains(uuid) || island.getOfficerList().contains(uuid)) {
                return island;
            }
        }
        return null;
    }

    public Island getIslandFromLocation(Location location) {
        for (Island island : Storage.islandList) {
            if (island.isBlockInIsland(location.getBlockX(), location.getBlockZ())){
                return island;
            }
        }
        return null;
    }

    public Island getIslandFromName(String name){
        for (Island island : Storage.islandList){
            if (island.getName().equalsIgnoreCase(name)){
                return island;
            }
        }
        return null;
    }

    public boolean isIslandName(String name){
        for (Island island : Storage.islandList){
            if (island.getName().equalsIgnoreCase(name)){
                return true;
            }
        }
        return false;
    }


    public boolean isOwner(UUID uuid, Island island) {
        return island.getOwnerUUID().equals(uuid);
    }

    public boolean isMember(UUID uuid, Island island) {
        return island.getMemberList().contains(uuid);
    }

    public boolean isOfficer(UUID uuid, Island island) {
        return island.getOfficerList().contains(uuid);
    }

    public boolean isCoOwner(UUID uuid, Island island) {
        return island.getCoownerList().contains(uuid);
    }

    public boolean inIsland(UUID uuid) {
        for (Island island : Storage.islandList) {
            if (isOwner(uuid, island) || isCoOwner(uuid, island) || isOfficer(uuid, island) || isMember(uuid, island)){
                return true;
            }
        }
        return false;
    }

    public double getLevelWorth(String blockType, boolean isSpawner) {
        if (isSpawner) {
            return SkyBlock.getInstance().getFileManager().getWorth().fetchDouble("level-worth.spawners." + blockType);
        } else {
            return SkyBlock.getInstance().getFileManager().getWorth().fetchDouble("level-worth.blocks." + blockType);
        }
    }

    public double getMoneyWorth(String blockType, boolean isSpawner) {
        if (isSpawner) {
            return SkyBlock.getInstance().getFileManager().getWorth().fetchDouble("money-worth.spawners." + blockType);
        } else {
            return SkyBlock.getInstance().getFileManager().getWorth().fetchDouble("money-worth.blocks." + blockType);
        }
    }

    public boolean hasWorth(String blockType, boolean isSpawner){
        double level = getLevelWorth(blockType, isSpawner);
        double money = getMoneyWorth(blockType, isSpawner);
        return level > 0 || money > 0;
    }

    public Island getIslandFromPlacement(int place) {
        for (Island island : Storage.islandList) {
            if (island.getTopPlace() == place) {
                return island;
            }
        }
        return null;
    }

    public List<IslandWarp> getIslandWarps(Island island){
        return island.getIslandWarps();
    }

    public IslandWarp getIslandWarp(Island island, String name){
        for (IslandWarp islandWarp : island.getIslandWarps()){
            if (islandWarp.getName().equalsIgnoreCase(name)){
                return islandWarp;
            }
        }
        return null;
    }

    public void resetQuestData(UUID uuid){
        MemoryPlayer memoryPlayer = SkyBlock.getInstance().getUtils().getMemoryPlayer(uuid);
        if (memoryPlayer != null) {
            memoryPlayer.setCompletedQuests(new ArrayList<>());
        }
    }


    public void calculateIslandTop() {

        List<Double> levels = new ArrayList<>();

        for (Island island : Storage.islandList) {

            island.setLevel(0);
            island.setBlockWorth(0);
            island.setSpawnerWorth(0);


            Iterator it = island.getBlocks().entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();

                FakeItem fakeItem = (FakeItem)pair.getKey();
                int amount = (int)pair.getValue();
                if (amount == 0) continue;
                double val;

                // System.out.print("\n "+fakeItem.getType()+","+amount+"\n");

                if (fakeItem.isSpawner()) {
                    val = getMoneyWorth(fakeItem.getType(), true) * amount;
                    island.addSpawnerWorth(val);
                    val = getLevelWorth(fakeItem.getType(), true) * amount;
                    island.addLevel(val);
                } else {
                    val = getMoneyWorth(fakeItem.getType(), false) * amount;
                    island.addBlockWorth(val);
                    val = getLevelWorth(fakeItem.getType(), false) * amount;
                    island.addLevel(val);
                }
               // it.remove(); // avoids a ConcurrentModificationException
            }

            double level = island.getLevel();
            levels.add(level);
        }

        levels.sort(Comparator.reverseOrder());

        int current = 1;
        for (double d : levels) {
            for (Island island : Storage.islandList) {
                if (d == island.getLevel()) {
                    // map.put(current, island);
                    island.setTopPlace(current);
                    current++;
                }
            }
        }
        levels.clear();
    }

    public void calculateIslandLevel(Island island) {
        List<FakeChunk> fakeChunkList = island.getFakeChunks();

        island.clearBlockCount();

        for (FakeChunk chunk : fakeChunkList) {
            SkyBlock.getInstance().getReflectionManager().nmsHandler.calculate(island.getLocation().getWorld().getChunkAt(chunk.getX(), chunk.getZ()), island);
        }
        fakeChunkList.clear();
    }

    public ArrayList<Perms> buildDefaultPerms(Role role){
        FileConfiguration f = SkyBlock.getInstance().getFileManager().getPermissions().getConfig();
        ArrayList<Perms> perms = new ArrayList<>();
        for (Role r : Role.values()){
            if (role.equals(r)){
                for (Perm perm : Perm.values()){
                    perms.add(new Perms(perm, f.getBoolean("default-permissions."+r.name().toUpperCase()+"."+perm.name().toUpperCase())));
                }
            }
        }
        return perms;
    }

    public ArrayList<Rules> buildDefaultRules(){
        FileConfiguration f = SkyBlock.getInstance().getFileManager().getRules().getConfig();
        ArrayList<Rules> rules = new ArrayList<>();
        for (Rule r : Rule.values()){
            // perms.add(new Perms(perm, f.getBoolean("default-permissions."+r.name().toUpperCase()+"."+perm.name().toUpperCase())));
            rules.add(new Rules(r, f.getBoolean("default-rules."+r.name())));
        }
        return rules;
    }

}