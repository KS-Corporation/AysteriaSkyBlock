package me.trent.skyblock.nms;

import me.trent.skyblock.island.Island;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import me.trent.skyblock.PluginHook;
import me.trent.skyblock.SkyBlock;
import me.trent.skyblock.Storage;

import java.util.Map;

public class NMSHandler_v1_12_R1 extends NMSHandler {

    @Override
    public void generate(String name) {
        SkyBlock.getInstance().getWorldGenerator().generateWorld("skyBlock");
    }

    @Override
    public String getVersion() {
        return "1_12_R1";
    }

    @Override
    public void calculate(org.bukkit.Chunk chunk, Island island) {
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


        for (final Map.Entry<net.minecraft.server.v1_12_R1.BlockPosition, net.minecraft.server.v1_12_R1.TileEntity> entry : craftChunk.getHandle().tileEntities.entrySet()) {
            if (island.isBlockInIsland(entry.getKey().getX(), entry.getKey().getZ())) {
                final net.minecraft.server.v1_12_R1.TileEntity tileEntity = entry.getValue();

                String blockType;
                boolean isSpawner = false;

                if (tileEntity instanceof net.minecraft.server.v1_12_R1.TileEntityMobSpawner) {
                    net.minecraft.server.v1_12_R1.TileEntityMobSpawner spawner = (net.minecraft.server.v1_12_R1.TileEntityMobSpawner)tileEntity;
                    blockType = spawner.getSpawner().getMobName().toString().toUpperCase();

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
        net.minecraft.server.v1_12_R1.World w = ((org.bukkit.craftbukkit.v1_12_R1.CraftWorld) Storage.getSkyBlockWorld()).getHandle();
        net.minecraft.server.v1_12_R1.Chunk chunk = w.getChunkAt(X >> 4, Z >> 4);
        net.minecraft.server.v1_12_R1.BlockPosition bp = new net.minecraft.server.v1_12_R1.BlockPosition(X, Y, Z);
        net.minecraft.server.v1_12_R1.IBlockData ibd = net.minecraft.server.v1_12_R1.Block.getByCombinedId(0);

        w.setTypeAndData(bp, ibd, applyPhysics ? 3 : 2);
        chunk.a(bp, ibd);
    }

    @Override
    public void sendBorder(Player p, double x, double z, double radius) {
        final WorldBorder worldBorder = new WorldBorder();
        org.bukkit.World bukkitWorld = p.getWorld();
        CraftWorld craftWorld = (CraftWorld)bukkitWorld;

        worldBorder.world = craftWorld.getHandle();

        worldBorder.setCenter(x, z);
        worldBorder.setSize(radius * 2);
        worldBorder.setWarningDistance(0);
        final EntityPlayer entityPlayer = ((CraftPlayer) p).getHandle();

        PacketPlayOutWorldBorder sizePacket = new PacketPlayOutWorldBorder(worldBorder, PacketPlayOutWorldBorder.EnumWorldBorderAction.SET_SIZE);
        PacketPlayOutWorldBorder centerPacket = new PacketPlayOutWorldBorder(worldBorder, PacketPlayOutWorldBorder.EnumWorldBorderAction.SET_CENTER);
        PacketPlayOutWorldBorder warningPacket = new PacketPlayOutWorldBorder(worldBorder, PacketPlayOutWorldBorder.EnumWorldBorderAction.SET_WARNING_BLOCKS);

        entityPlayer.playerConnection.sendPacket(sizePacket);
        entityPlayer.playerConnection.sendPacket(centerPacket);
        entityPlayer.playerConnection.sendPacket(warningPacket);
    }

    @Override
    public void sendTitle(Player p, String text, int in, int stay, int out, String type) {
        final PacketPlayOutTitle title = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.valueOf(type),
                IChatBaseComponent.ChatSerializer.a(ChatColor.translateAlternateColorCodes('&', "{\"text\":\"" + text + " \"}")), in, stay, out);
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(title);
    }
}
