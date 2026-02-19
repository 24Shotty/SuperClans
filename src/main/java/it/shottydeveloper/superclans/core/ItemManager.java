package it.shottydeveloper.superclans.core;

import it.shottydeveloper.superclans.SuperClans;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class ItemManager {

    private final SuperClans plugin;

    public ItemManager(SuperClans plugin) {
        this.plugin = plugin;
    }

    public ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(plugin.getMessagesConfig().color(name));

            if (lore.length > 0) {
                List<String> coloredLore = Arrays.stream(lore)
                        .map(line -> plugin.getMessagesConfig().color(line))
                        .toList();
                meta.setLore(coloredLore);
            }

            item.setItemMeta(meta);
        }

        return item;
    }
}