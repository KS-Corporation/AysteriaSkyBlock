package me.trent.skyblock.guis;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import me.trent.skyblock.SkyBlock;
import me.trent.skyblock.island.MemoryPlayer;

import java.util.List;

import static me.trent.skyblock.island.upgrades.UpgradesUI.createFadedItem;

public class Islands implements Listener {

    public static void openIslands(Player p) {
        Inventory i = Bukkit.createInventory(null, SkyBlock.getInstance().getFileManager().getGuis().fetchInt("islands.rows") * 9, SkyBlock.getInstance().getUtils().color(SkyBlock.getInstance().getFileManager().getGuis().fetchString("islands.name")));

        for (String s : SkyBlock.getInstance().getFileManager().getGuis().fetchString("islands.faded-slots").split(",")){
            int slot = Integer.parseInt(s);
            i.setItem(slot -1, createFadedItem());
        }

        int m = SkyBlock.getInstance().getFileManager().getGuis().getConfig().getConfigurationSection("islands.items").getKeys(false).size();
        for (int a = 1; a <= m; a++) {
            String materialID = SkyBlock.getInstance().getFileManager().getGuis().fetchString("islands.items." + a + ".item-id");
            int amount = SkyBlock.getInstance().getFileManager().getGuis().fetchInt("islands.items." + a + ".amount");
            int slot = SkyBlock.getInstance().getFileManager().getGuis().fetchInt("islands.items." + a + ".slot");
            String name = SkyBlock.getInstance().getUtils().color(SkyBlock.getInstance().getFileManager().getGuis().fetchString("islands.items." + a + ".item-name"));
            List<String> lore = SkyBlock.getInstance().getUtils().color(SkyBlock.getInstance().getFileManager().getGuis().fetchStringList("islands.items." + a + ".item-lore"));

            i.setItem(slot - 1, SkyBlock.getInstance().getUtils().createItem(materialID, 0, name, lore, amount));
        }
        p.openInventory(i);
    }

    @EventHandler
    public void click(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        Inventory i = e.getClickedInventory();
        if (i != null) {
            String iName = e.getView().getTitle();
            if (iName == null) return;
            if (iName.equalsIgnoreCase(SkyBlock.getInstance().getUtils().color(SkyBlock.getInstance().getFileManager().getGuis().fetchString("islands.name")))) {
                e.setCancelled(true);
                if (e.getCurrentItem() != null && !e.getCurrentItem().getType().equals(Material.AIR)) {
                    ItemStack clicked = e.getCurrentItem();
                    if (clicked.hasItemMeta()) {
                        ItemMeta meta = clicked.getItemMeta();
                        String name = meta.getDisplayName();

                        int m = SkyBlock.getInstance().getFileManager().getGuis().getConfig().getConfigurationSection("islands.items").getKeys(false).size();
                        for (int a = 1; a <= m; a++) {
                            String name2 = SkyBlock.getInstance().getUtils().color(SkyBlock.getInstance().getFileManager().getGuis().fetchString("islands.items." + a + ".item-name"));
                            String schematic = SkyBlock.getInstance().getFileManager().getGuis().fetchString("islands.items." + a + ".schematic");
                            String perm = SkyBlock.getInstance().getFileManager().getGuis().fetchString("islands.items." + a + ".permission");

                            if (name.equalsIgnoreCase(name2)) {
                                //create island
                                //check their current resets
                                MemoryPlayer memoryPlayer = SkyBlock.getInstance().getUtils().getMemoryPlayer(p.getUniqueId());
                                if (memoryPlayer != null){
                                    int resetMax = SkyBlock.getInstance().getUtils().getSettingInt("default-reset-limit");
                                    if (memoryPlayer.getResets() >= resetMax){
                                        if (memoryPlayer.getPermissionValue("skyblock.resets") <= memoryPlayer.getResets()){
                                            p.sendMessage(SkyBlock.getInstance().getUtils().getMessage("island-reset-max"));
                                            return;
                                        }
                                        p.sendMessage(SkyBlock.getInstance().getUtils().getMessage("island-reset-max"));
                                        return;
                                    }
                                }
                                SkyBlock.getInstance().getIslandUtils().createIsland(p, perm, schematic);
                            }
                        }
                    }
                }
            }
        }
    }
}