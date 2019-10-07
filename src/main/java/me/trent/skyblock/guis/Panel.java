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

import java.util.List;

import static me.trent.skyblock.island.upgrades.UpgradesUI.createFadedItem;

public class Panel implements Listener {

    public static void openPanel(Player p) {
        Inventory i = Bukkit.createInventory(null, SkyBlock.getInstance().getFileManager().getGuis().fetchInt("panel.rows") * 9, SkyBlock.getInstance().getUtils().color(SkyBlock.getInstance().getFileManager().getGuis().fetchString("panel.name")));

        for (String s : SkyBlock.getInstance().getFileManager().getGuis().fetchString("panel.faded-slots").split(",")){
            int slot = Integer.parseInt(s);
            i.setItem(slot -1, createFadedItem());
        }

        int m = SkyBlock.getInstance().getFileManager().getGuis().getConfig().getConfigurationSection("panel.items").getKeys(false).size();
        for (int a = 1; a <= m; a++) {
            String materialID = SkyBlock.getInstance().getFileManager().getGuis().fetchString("panel.items." + a + ".item-id");
            int amount = SkyBlock.getInstance().getFileManager().getGuis().fetchInt("panel.items." + a + ".amount");
            int slot = SkyBlock.getInstance().getFileManager().getGuis().fetchInt("panel.items." + a + ".slot");
            String name = SkyBlock.getInstance().getUtils().color(SkyBlock.getInstance().getFileManager().getGuis().fetchString("panel.items." + a + ".item-name"));
            List<String> lore = SkyBlock.getInstance().getUtils().color(SkyBlock.getInstance().getFileManager().getGuis().fetchStringList("panel.items." + a + ".item-lore"));

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
            if (iName.equalsIgnoreCase(SkyBlock.getInstance().getUtils().color(SkyBlock.getInstance().getFileManager().getGuis().fetchString("panel.name")))) {
                e.setCancelled(true);
                if (e.getCurrentItem() != null && !e.getCurrentItem().getType().equals(Material.AIR)) {
                    ItemStack clicked = e.getCurrentItem();
                    if (clicked.hasItemMeta()) {
                        ItemMeta meta = clicked.getItemMeta();
                        String name = meta.getDisplayName();

                        int m = SkyBlock.getInstance().getFileManager().getGuis().getConfig().getConfigurationSection("panel.items").getKeys(false).size();
                        for (int a = 1; a <= m; a++) {
                            String name2 = SkyBlock.getInstance().getUtils().color(SkyBlock.getInstance().getFileManager().getGuis().fetchString("panel.items." + a + ".item-name"));

                            if (name.equalsIgnoreCase(name2)) {
                                //run the commands in the config
                                List<String> commands = SkyBlock.getInstance().getFileManager().getGuis().fetchStringList("panel.items." + a + ".commands");
                                for (String s : commands) {
                                    Bukkit.dispatchCommand(p, s);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}