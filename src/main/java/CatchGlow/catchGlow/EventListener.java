package CatchGlow.catchGlow;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EventListener implements Listener {
    private final GameManager gameManager;
    private final PlayerManager playerManager;
    private final ConfigManager configManager;
    private final ItemManager itemManager;
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public EventListener(GameManager gameManager, PlayerManager playerManager, ConfigManager configManager, ItemManager itemManager) {
        this.gameManager = gameManager;
        this.playerManager = playerManager;
        this.configManager = configManager;
        this.itemManager = itemManager;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!gameManager.isRunning()) return;

        Player player = event.getPlayer();
        
        // 도망자 아이템 사용 (위치 변환기 - 레드스톤 중계기)
        if (playerManager.isRunner(player)) {
            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (event.getItem() != null && event.getItem().getType() == Material.REPEATER) {
                    event.setCancelled(true); // 설치 방지
                    
                    List<Player> allPlayers = new ArrayList<>();
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (p.getGameMode() == GameMode.SURVIVAL) {
                            allPlayers.add(p);
                        }
                    }
                    
                    if (allPlayers.size() < 2) {
                        player.sendMessage(ChatColor.RED + "위치를 섞을 플레이어가 부족합니다.");
                        return;
                    }
                    
                    // 현재 위치들 저장
                    List<Location> locations = new ArrayList<>();
                    for (Player p : allPlayers) {
                        locations.add(p.getLocation());
                    }
                    
                    // 위치 섞기 (자기 위치가 안 걸리도록 셔플)
                    // 간단하게 Collections.shuffle을 쓰면 자기 위치가 걸릴 수도 있음.
                    // 완전 순열(Derangement)까지는 아니더라도 그냥 섞어서 이동시킴.
                    Collections.shuffle(locations);
                    
                    for (int i = 0; i < allPlayers.size(); i++) {
                        Player p = allPlayers.get(i);
                        p.teleport(locations.get(i));
                        p.sendMessage(ChatColor.LIGHT_PURPLE + "누군가에 의해 위치가 뒤섞였습니다!");
                        p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
                    }
                    
                    Bukkit.broadcastMessage(ChatColor.GOLD + ">>> " + player.getName() + "님이 대혼란 위치 변환기를 사용했습니다! <<<");
                    
                    event.getItem().setAmount(event.getItem().getAmount() - 1);
                }
            }
            return;
        }

        // 술래 아이템 사용
        if (!playerManager.isSeeker(player)) return;

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getItem() == null) return;
            Material itemType = event.getItem().getType();
            
            if (itemType == Material.COMPASS || itemType == Material.ENDER_EYE || itemType == Material.IRON_INGOT) {
                // 쿨다운 체크 제거 (1회용 소모품으로 변경)
                boolean used = false;
                int glowDuration = configManager.getGlowDuration() * 20;

                if (itemType == Material.COMPASS) {
                    // All runners glow
                    for (UUID uuid : playerManager.getRunners()) {
                        Player runner = player.getServer().getPlayer(uuid);
                        if (runner != null) {
                            runner.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, glowDuration, 0));
                        }
                    }
                    player.sendMessage(ChatColor.GREEN + "모든 도망자의 위치가 드러났습니다!");
                    used = true;
                } else if (itemType == Material.ENDER_EYE) {
                    // Nearby runners glow
                    boolean found = false;
                    for (UUID uuid : playerManager.getRunners()) {
                        Player runner = player.getServer().getPlayer(uuid);
                        if (runner != null && runner.getWorld().equals(player.getWorld()) && runner.getLocation().distance(player.getLocation()) <= 20) {
                            runner.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, glowDuration, 0));
                            found = true;
                        }
                    }
                    if (found) {
                        player.sendMessage(ChatColor.GREEN + "주변 도망자의 위치가 드러났습니다!");
                    } else {
                        player.sendMessage(ChatColor.RED + "주변에 도망자가 없습니다.");
                    }
                    used = true;
                } else if (itemType == Material.IRON_INGOT) {
                    // Nearest runner glows
                    Player nearest = null;
                    double minDistance = Double.MAX_VALUE;
                    
                    for (UUID uuid : playerManager.getRunners()) {
                        Player runner = player.getServer().getPlayer(uuid);
                        if (runner != null && runner.getWorld().equals(player.getWorld())) {
                            double dist = runner.getLocation().distance(player.getLocation());
                            if (dist < minDistance) {
                                    minDistance = dist;
                                    nearest = runner;
                            }
                        }
                    }
                    
                    if (nearest != null) {
                        nearest.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, glowDuration, 0));
                        player.sendMessage(ChatColor.GREEN + "가장 가까운 도망자(" + nearest.getName() + ")의 위치가 드러났습니다!");
                    } else {
                        player.sendMessage(ChatColor.RED + "도망자를 찾을 수 없습니다.");
                    }
                    used = true;
                }

                if (used) {
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
                    // 아이템 소모 (1개 줄이기)
                    event.getItem().setAmount(event.getItem().getAmount() - 1);
                }
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!gameManager.isRunning()) return;

        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player attacker = (Player) event.getDamager();
            Player victim = (Player) event.getEntity();

            if (playerManager.isSeeker(attacker) && playerManager.isRunner(victim)) {
                // 술래가 풀리기 전에는 데미지 무효
                if (!gameManager.isSeekerReleased()) {
                    event.setCancelled(true);
                    attacker.sendMessage(ChatColor.RED + "아직 공격할 수 없습니다!");
                    return;
                }
                // 3방 컷 (약 7데미지)
                event.setDamage(7.0); 
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!gameManager.isRunning()) return;
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        
        // 도망자 무적 (술래 해방 전까지)
        if (playerManager.isRunner(player)) {
            if (!gameManager.isSeekerReleased()) {
                event.setCancelled(true);
            }
        }
        // 술래는 저항 효과로 데미지를 안 받지만, 낙사는 처리해야 함
        else if (playerManager.isSeeker(player)) {
             if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
                 event.setCancelled(false); // 낙사는 허용
             }
             // 나머지는 저항 포션이 막아줌
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!gameManager.isRunning()) return;
        
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            // 술래만 배고픔이 닳지 않음
            if (playerManager.isSeeker(player)) {
                event.setCancelled(true);
            } else {
                // 도망자는 배고픔 닳음 (이벤트 허용)
                event.setCancelled(false);
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (!gameManager.isRunning()) return;

        Player player = event.getEntity();
        
        if (playerManager.isSeeker(player)) {
            event.getDrops().clear();
            event.setDeathMessage(ChatColor.RED + "술래가 추락했습니다! 30초 후 부활합니다.");
        } else if (playerManager.isRunner(player)) {
            playerManager.removeRunner(player);
            event.setDeathMessage(ChatColor.RED + player.getName() + "님이 잡혔습니다! 남은 도망자: " + playerManager.getRunners().size() + "명");
            
            // 도망자는 즉시 관전 모드로 전환 (리스폰 화면 스킵)
            player.setGameMode(GameMode.SPECTATOR);
            event.getDrops().clear();
        }
    }
    
    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        if (!gameManager.isRunning()) return;
        
        Player player = event.getPlayer();
        if (playerManager.isSeeker(player)) {
            // Respawn at jail or current location temporarily
            Location jail = configManager.getLocation("jail");
            if (jail != null) {
                event.setRespawnLocation(jail);
            }
            
            player.setGameMode(GameMode.SPECTATOR);
            player.sendMessage(ChatColor.YELLOW + "30초 후에 전장에 복귀합니다...");

            new BukkitRunnable() {
                int timeLeft = 30;

                @Override
                public void run() {
                    if (!gameManager.isRunning() || !player.isOnline()) {
                        this.cancel();
                        return;
                    }

                    if (timeLeft <= 0) {
                        Location mapSpawn = configManager.getLocation("map-spawn");
                        if (mapSpawn != null) {
                            player.teleport(mapSpawn);
                        }
                        player.setGameMode(GameMode.SURVIVAL);
                        itemManager.giveSeekerItems(player);
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0));
                        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 255)); // 부활 시 저항 다시 부여
                        player.sendMessage(ChatColor.RED + "다시 사냥을 시작합니다!");
                        this.cancel();
                    } else {
                        if (timeLeft <= 5 || timeLeft % 10 == 0) {
                            player.sendMessage(ChatColor.YELLOW + "부활까지 " + timeLeft + "초...");
                        }
                        timeLeft--;
                    }
                }
            }.runTaskTimer(JavaPlugin.getProvidingPlugin(CatchGlow.class), 0L, 20L);
        }
    }
}
