package com.meao0525.trickorcollect.item;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public enum GameItems {
    STONE(Material.STONE, 32, 0),
    DIRT(Material.DIRT, 64, 1),
    OAK_LOG(Material.OAK_LOG, 16, 2),
    IRON_INGOT(Material.IRON_INGOT, 10, 3),
    COOKED_CHICKEN(Material.COOKED_CHICKEN, 16, 4),
    SALMON(Material.SALMON, 4, 5),
    BREAD(Material.BREAD, 4, 6),
    APPLE(Material.APPLE, 10, 7),
    FLETCHING_TABLE(Material.FLETCHING_TABLE, 1, 8),
    BEE_NEST(Material.BEE_NEST, 1, 9),
    WHITE_WOOL(Material.WHITE_WOOL, 16, 10),
    WATER_BUCKET(Material.WATER_BUCKET, 1, 11),
    GOLD_INGOT(Material.GOLD_INGOT, 1, 12),
    BOW(Material.BOW, 1, 13),
    GLASS_PANE(Material.GLASS_PANE, 64, 14),
    SUGAR_CANE(Material.SUGAR_CANE, 16, 15),
    RED_CARPET(Material.RED_CARPET, 9, 16),
    BRICK_STAIRS(Material.BRICK_STAIRS, 10, 17);




    private final Material material;
    private final int amount;
    private final int index;

    //こんすとらくたー
    private GameItems(Material material, int amount, int index) {
        this.material = material;
        this.amount = amount;
        this.index = index;
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

    public int getIndex() {
        return index;
    }
}
