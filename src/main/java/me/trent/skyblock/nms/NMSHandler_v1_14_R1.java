package me.trent.skyblock.nms;

import me.trent.skyblock.island.Island;
import me.trent.worldapi.WorldAPI_1_13_R2;
import net.minecraft.server.v1_14_R1.*;
import net.minecraft.server.v1_14_R1.WorldBorder;
import org.bukkit.*;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_14_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import me.trent.skyblock.PluginHook;
import me.trent.skyblock.SkyBlock;
import me.trent.skyblock.Storage;

import java.util.Map;

public class NMSHandler_v1_14_R1 extends NMSHandler {

    @Override
    public void generate(String name) {
        WorldAPI_1_13_R2.generate(name);
    }

    @Override
    public String getVersion() {
        return "1_14_R1";
    }

    public void calculate(Chunk chunk, Island island) {

        final CraftChunk craftChunk = (CraftChunk) chunk;

        final int minX = chunk.getX() << 4;
        final int minZ = chunk.getZ() << 4;
        final int maxX = minX | 15;
        final int maxY = chunk.getWorld().getMaxHeight();
        final int maxZ = minZ | 15;
        try {
            new BukkitRunnable() {
                public void run() {
                    for (int x = minX; x <= maxX; ++x) {
                        for (int y = 0; y <= maxY; ++y) {
                            for (int z = minZ; z <= maxZ; ++z) {
                                try {
                                    org.bukkit.block.Block block = chunk.getWorld().getBlockAt(x, y, z);
                                    if (block != null && !block.getType().equals(org.bukkit.Material.AIR)) {
                                        if (!ReflectionManager.tileEntities.contains(block.getType())) {
                                            String type = block.getType().name().toUpperCase();
                                            if (SkyBlock.getInstance().getIslandUtils().hasWorth(type, false)) {
                                                island.addBlockCount(type, false, 1);
                                                // System.out.print("\n\n\n "+type+":"+island.getBlockCount(new FakeItem("DIAMOND_BLOCK", false))+"   \n\n\n");
                                            }
                                        }
                                    }
                                } catch (IllegalArgumentException e) {
                                    e.printStackTrace();
                                    return;
                                }
                            }
                        }
                    }
                }
            }.runTaskAsynchronously(SkyBlock.getInstance());
        }catch(IllegalStateException e){ }

        for (final Map.Entry<BlockPosition, net.minecraft.server.v1_14_R1.TileEntity> entry : craftChunk.getHandle().tileEntities.entrySet()) {
            if (island.isBlockInIsland(entry.getKey().getX(), entry.getKey().getZ())) {
                final net.minecraft.server.v1_14_R1.TileEntity tileEntity = entry.getValue();

                String blockType;
                boolean isSpawner = false;

                if (tileEntity instanceof net.minecraft.server.v1_14_R1.TileEntityMobSpawner) {
                    net.minecraft.server.v1_14_R1.TileEntityMobSpawner spawner = (net.minecraft.server.v1_14_R1.TileEntityMobSpawner)tileEntity;
                    blockType = spawner.getSpawner().getMobName().toString().toUpperCase();
                    blockType = blockType.replace("MINECRAFT:", "");
                    int amount = PluginHook.getSpawnerCount(new Location(Bukkit.getWorld(spawner.getWorld().worldData.getName()), spawner.getPosition().getX(), spawner.getPosition().getY(), spawner.getPosition().getZ()));
                    island.addBlockCount(blockType, true, amount);
                    continue;
                } else {
                    blockType = tileEntity.getBlock().getMaterial().toString().toUpperCase();
                }
                if (SkyBlock.getInstance().getIslandUtils().hasWorth(blockType, false)){
                    island.addBlockCount(blockType, false, 1);
                }
            }
        }
    }

    @Override
    public void removeBlockSuperFast(int X, int Y, int Z, boolean applyPhysics) {
        Storage.getSkyBlockWorld().getBlockAt(X, Y, Z).setType(Material.AIR, applyPhysics);
    }

    @Override
    public void sendBorder(Player p, double x, double z, double radius) {
        final WorldBorder worldBorder = new WorldBorder();
        org.bukkit.World bukkitWorld = p.getWorld();
        org.bukkit.craftbukkit.v1_14_R1.CraftWorld craftWorld = (CraftWorld) bukkitWorld;

        worldBorder.world = craftWorld.getHandle();
        worldBorder.setCenter(x, z);
        worldBorder.setSize(radius * 2);
        worldBorder.setWarningDistance(0);
        final EntityPlayer entityPlayer = ((CraftPlayer) p).getHandle();
        //System.out.print("\n\n "+worldBorder.getCenterX()+","+worldBorder.getCenterZ()+"\n\n");
        //System.out.print("\n\n "+entityPlayer.getName()+","+entityPlayer.playerConnection.toString()+"\n\n");
        //entityPlayer is good, same with WorldBorder, has to be with the packet or the Enum

        entityPlayer.playerConnection.sendPacket(new PacketPlayOutWorldBorder(worldBorder, PacketPlayOutWorldBorder.EnumWorldBorderAction.SET_SIZE));
        entityPlayer.playerConnection.sendPacket(new PacketPlayOutWorldBorder(worldBorder, PacketPlayOutWorldBorder.EnumWorldBorderAction.SET_CENTER));
        entityPlayer.playerConnection.sendPacket(new PacketPlayOutWorldBorder(worldBorder, PacketPlayOutWorldBorder.EnumWorldBorderAction.SET_WARNING_BLOCKS));
    }

    @Override
    public void sendTitle(Player p, String text, int in, int stay, int out, String type) {
        final PacketPlayOutTitle title = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.valueOf(type),
                IChatBaseComponent.ChatSerializer.a(ChatColor.translateAlternateColorCodes('&', "{\"text\":\"" + text + " \"}")), in, stay, out);
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(title);
    }
}
