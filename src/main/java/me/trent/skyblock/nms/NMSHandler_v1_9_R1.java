package me.trent.skyblock.nms;

import me.trent.skyblock.island.Island;
import net.minecraft.server.v1_9_R1.*;
import net.minecraft.server.v1_9_R1.WorldBorder;
import org.bukkit.*;
import org.bukkit.Chunk;
import org.bukkit.craftbukkit.v1_9_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_9_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import me.trent.skyblock.PluginHook;
import me.trent.skyblock.SkyBlock;
import me.trent.skyblock.Storage;

import java.util.Map;

public class NMSHandler_v1_9_R1 extends NMSHandler {

    @Override
    public void generate(String name) {
        SkyBlock.getInstance().getWorldGenerator().generateWorld("skyBlock");
    }

    @Override
    public String getVersion() {
        return "1_9_R1";
    }

    public void calculate(Chunk chunk, Island island) {

        final CraftChunk craftChunk = (CraftChunk) chunk;

        final int minX = chunk.getX() << 4;
        final int minZ = chunk.getZ() << 4;
        final int maxX = minX | 15;
        final int maxY = chunk.getWorld().getMaxHeight();
        final int maxZ = minZ | 15;

        new BukkitRunnable(){
            @Override
            public void run() {
                for (int x = minX; x <= maxX; ++x) {
                    for (int y = 0; y <= maxY; ++y) {
                        for (int z = minZ; z <= maxZ; ++z) {
                            org.bukkit.block.Block block = chunk.getBlock(x, y, z);
                            if (block != null && !block.getType().equals(org.bukkit.Material.AIR)) {
                                if (!ReflectionManager.tileEntities.contains(block.getType())) {
                                    String type = block.getType().name().toUpperCase();
                                    if (SkyBlock.getInstance().getIslandUtils().hasWorth(type, false)){
                                        island.addBlockCount(type, false, 1);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }.runTaskAsynchronously(SkyBlock.getInstance());

        for (final Map.Entry<BlockPosition, TileEntity> entry : craftChunk.getHandle().tileEntities.entrySet()) {
            if (island.isBlockInIsland(entry.getKey().getX(), entry.getKey().getZ())) {
                final TileEntity tileEntity = entry.getValue();

                String blockType;
                boolean isSpawner = false;

                if (tileEntity instanceof net.minecraft.server.v1_9_R1.TileEntityMobSpawner) {
                    net.minecraft.server.v1_9_R1.TileEntityMobSpawner spawner = (net.minecraft.server.v1_9_R1.TileEntityMobSpawner)tileEntity;
                    blockType = spawner.getSpawner().getMobName().toUpperCase();

                    int amount = PluginHook.getSpawnerCount(new Location(Bukkit.getWorld(spawner.getWorld().worldData.getName()), spawner.getPosition().getX(), spawner.getPosition().getY(), spawner.getPosition().getZ()));
                    island.addBlockCount(blockType, true, amount);
                    continue;
                } else {
                    blockType = tileEntity.getBlock().getName().toUpperCase();
                }
                if (SkyBlock.getInstance().getIslandUtils().hasWorth(blockType, false)){
                    island.addBlockCount(blockType, false, 1);
                }
            }
        }
    }

    @Override
    public void removeBlockSuperFast(int X, int Y, int Z, boolean applyPhysics) {
        Storage.getSkyBlockWorld().getBlockAt(X, Y, Z).setType(org.bukkit.Material.AIR, applyPhysics);
    }

    @Override
    public void sendBorder(Player p, double x, double z, double radius) {
        final WorldBorder worldBorder = new WorldBorder();
        org.bukkit.World bukkitWorld = p.getWorld();
        org.bukkit.craftbukkit.v1_9_R1.CraftWorld craftWorld = (CraftWorld) bukkitWorld;

        worldBorder.world = craftWorld.getHandle();
        worldBorder.setCenter(x, z);
        worldBorder.setSize(radius * 2);
        worldBorder.setWarningDistance(0);
        final EntityPlayer entityPlayer = ((CraftPlayer) p).getHandle();
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
