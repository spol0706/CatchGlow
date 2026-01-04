package CatchGlow.catchGlow;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class ItemManager {

    public void giveSeekerItems(Player player) {
        player.getInventory().clear();

        // Sword
        ItemStack sword = new ItemStack(Material.IRON_SWORD);
        ItemMeta swordMeta = sword.getItemMeta();
        swordMeta.setDisplayName("§c처형의 검");
        swordMeta.setLore(Arrays.asList("§7도망자를 3방에 처치할 수 있습니다."));
        swordMeta.addEnchant(Enchantment.SHARPNESS, 3, true); // Sharpness 3
        swordMeta.setUnbreakable(true);
        sword.setItemMeta(swordMeta);
        player.getInventory().addItem(sword);

        // Radar Compass
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta compassMeta = compass.getItemMeta();
        compassMeta.setDisplayName("§e레이더 나침반");
        compassMeta.setLore(Arrays.asList("§7우클릭 시 모든 도망자가 3초간 발광합니다.", "§7쿨다운: 10초"));
        compassMeta.setUnbreakable(true);
        compass.setItemMeta(compassMeta);
        player.getInventory().addItem(compass);

        // Tracking Eye
        ItemStack eye = new ItemStack(Material.ENDER_EYE);
        ItemMeta eyeMeta = eye.getItemMeta();
        eyeMeta.setDisplayName("§b추적의 눈");
        eyeMeta.setLore(Arrays.asList("§7우클릭 시 반경 20블록 내 도망자가 발광합니다.", "§7쿨다운: 10초"));
        eyeMeta.setUnbreakable(true);
        eye.setItemMeta(eyeMeta);
        player.getInventory().addItem(eye);

        // Detector
        ItemStack detector = new ItemStack(Material.IRON_INGOT);
        ItemMeta detectorMeta = detector.getItemMeta();
        detectorMeta.setDisplayName("§a탐지기");
        detectorMeta.setLore(Arrays.asList("§7우클릭 시 가장 가까운 도망자 1명이 발광합니다.", "§7쿨다운: 10초"));
        detectorMeta.setUnbreakable(true);
        detector.setItemMeta(detectorMeta);
        player.getInventory().addItem(detector);
    }

    public void giveRunnerItems(Player player) {
        player.getInventory().clear();

        // Knockback Stick
        ItemStack stick = new ItemStack(Material.STICK);
        ItemMeta stickMeta = stick.getItemMeta();
        stickMeta.setDisplayName("§f밀치기 막대");
        stickMeta.addEnchant(Enchantment.KNOCKBACK, 1, true);
        stickMeta.setUnbreakable(true);
        stick.setItemMeta(stickMeta);
        player.getInventory().addItem(stick);
    }
}
