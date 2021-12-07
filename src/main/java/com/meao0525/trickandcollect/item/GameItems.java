package com.meao0525.trickandcollect.item;

import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public enum GameItems {
    STONE(Material.STONE, 32),
    DIRT(Material.DIRT, 64),
    OAK_LOG(Material.OAK_LOG, 16),
    IRON_INGOT(Material.IRON_INGOT, 10),
    COOKED_CHICKEN(Material.COOKED_CHICKEN, 16),
    SALMON(Material.SALMON, 4),
    BREAD(Material.BREAD, 4),
    APPLE(Material.APPLE, 10),
    FLETCHING_TABLE(Material.FLETCHING_TABLE, 1),
    BEE_NEST(Material.BEE_NEST, 1),
    WHITE_WOOL(Material.WHITE_WOOL, 16),
    WATER_BUCKET(Material.WATER_BUCKET, 1),
    GOLD_INGOT(Material.GOLD_INGOT, 1),
    BOW(Material.BOW, 1),
    GLASS_PANE(Material.GLASS_PANE, 64),
    SUGAR_CANE(Material.SUGAR_CANE, 16),
    RED_CARPET(Material.RED_CARPET, 9),
    BRICK_STAIRS(Material.BRICK_STAIRS, 10);




    private final Material material;
    private final int amount;

    //こんすとらくたー
    private GameItems(Material material, int amount) {
        this.material = material;
        this.amount = amount;
    }

    //ItemStack化するよ
    public ItemStack toItemStack() {
        ItemStack item = new ItemStack(material, amount);
        //同じの取得してもスタックしないためのNBT追加
        ItemMeta meta = item.getItemMeta();
        meta.setCustomModelData(1);
        item.setItemMeta(meta);

        return item;
    }

    //ゲッター
    public Material getMaterial() {
        return material;
    }

    public int getAmount() {
        return amount;
    }
}
