package CatchGlow.catchGlow;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class GameManager {
    private final CatchGlow plugin;
    private final PlayerManager playerManager;
    private final ConfigManager configManager;
    private final ItemManager itemManager;
    private boolean isRunning = false;
    private boolean isSeekerReleased = false; // 술래 해방 여부
    private BukkitRunnable gameTask;
    private BukkitRunnable jailTask;

    public GameManager(CatchGlow plugin, PlayerManager playerManager, ConfigManager configManager, ItemManager itemManager) {
        this.plugin = plugin;
        this.playerManager = playerManager;
        this.configManager = configManager;
        this.itemManager = itemManager;
    }

    public void startGame() {
        if (isRunning) return;
        
        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        if (onlinePlayers.size() < 2) {
            Bukkit.broadcastMessage(ChatColor.RED + "게임을 시작하려면 최소 2명의 플레이어가 필요합니다.");
            return;
        }

        isRunning = true;
        isSeekerReleased = false; // 초기화
        playerManager.clear();

        // Assign roles
        Random random = new Random();
        Player seeker = onlinePlayers.get(random.nextInt(onlinePlayers.size()));
        playerManager.addSeeker(seeker);
        
        for (Player p : onlinePlayers) {
            if (!p.getUniqueId().equals(seeker.getUniqueId())) {
                playerManager.addRunner(p);
            }
            // 게임 시작 시 배고픔/체력 회복
            p.setHealth(20);
            p.setFoodLevel(20);
        }
        // 술래에게도 스테이크 지급 (혹시 모르니)
        seeker.setHealth(20);
        seeker.setFoodLevel(20);


        // Teleport and give items
        Location mapSpawn = configManager.getLocation("map-spawn");
        Location jail = configManager.getLocation("jail");

        if (mapSpawn != null) {
            for (UUID uuid : playerManager.getRunners()) {
                Player runner = Bukkit.getPlayer(uuid);
                if (runner != null) {
                    runner.teleport(mapSpawn);
                    itemManager.giveRunnerItems(runner);
                    // 스테이크 지급 (인벤토리 클리어 후 지급)
                    runner.getInventory().addItem(new ItemStack(Material.COOKED_BEEF, 64));
                    runner.sendMessage(ChatColor.GREEN + "당신은 도망자입니다! 도망치세요!");
                }
            }
        }

        if (jail != null) {
            seeker.teleport(jail);
            itemManager.giveSeekerItems(seeker);
            // 술래에게도 스테이크 지급
            seeker.getInventory().addItem(new ItemStack(Material.COOKED_BEEF, 64));
            // 저항 효과 부여 (무적이지만 밀쳐짐)
            seeker.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 255));
            seeker.sendMessage(ChatColor.RED + "당신은 술래입니다! 잠시 후 풀려납니다.");
        }

        Bukkit.broadcastMessage(configManager.getMessage("game-start"));

        // Jail timer
        jailTask = new BukkitRunnable() {
            int timeLeft = configManager.getJailTime();

            @Override
            public void run() {
                if (!isRunning) {
                    this.cancel();
                    return;
                }
                
                if (timeLeft <= 0) {
                    isSeekerReleased = true; // 술래 해방 상태 변경
                    if (mapSpawn != null) {
                        seeker.teleport(mapSpawn);
                        Bukkit.broadcastMessage(ChatColor.RED + "술래가 풀려났습니다! 도망자들의 무적이 해제됩니다!");
                        seeker.playSound(seeker.getLocation(), org.bukkit.Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 1f);
                        seeker.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0));
                    }
                    this.cancel();
                } else {
                    if (timeLeft <= 5 || timeLeft % 10 == 0) {
                        seeker.sendMessage(ChatColor.YELLOW + "술래 해방까지 " + timeLeft + "초 남았습니다.");
                    }
                    
                    // 술래 대기 중 액션바 표시
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.RED + "술래가 풀려나기 전입니다... " + timeLeft + "초"));
                    }
                    
                    timeLeft--;
                }
            }
        };
        jailTask.runTaskTimer(plugin, 0L, 20L);

        // Game timer
        gameTask = new BukkitRunnable() {
            int timeLeft = configManager.getPlayTime();

            @Override
            public void run() {
                if (!isRunning) {
                    this.cancel();
                    return;
                }

                if (playerManager.getRunners().isEmpty()) {
                    stopGame(true); // Seeker wins
                    this.cancel();
                    return;
                }

                if (timeLeft <= 0) {
                    stopGame(false); // Runners win
                    this.cancel();
                }
                
                // 60초 남았을 때 이벤트 (1분)
                if (timeLeft == 60) {
                    Bukkit.broadcastMessage(ChatColor.RED + "게임 종료 1분 전! 모든 플레이어가 발광 상태가 되며, 술래가 강력해집니다!");
                    
                    // 술래에게 신속 2, 점프 강화 2, 힘 3 부여
                    for (UUID uuid : playerManager.getSeekers()) {
                        Player s = Bukkit.getPlayer(uuid);
                        if (s != null) {
                            s.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1)); // Speed 2
                            s.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, Integer.MAX_VALUE, 1)); // Jump Boost 2
                            s.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 2)); // Strength 3
                        }
                    }
                    
                    // 모든 플레이어에게 발광 부여
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 0));
                        p.playSound(p.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
                    }
                }
                
                // Action bar timer (술래가 풀려난 후에만 표시)
                if (isSeekerReleased) {
                    String timeString = String.format("%02d:%02d", timeLeft / 60, timeLeft % 60);
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.YELLOW + "남은 시간: " + timeString));
                    }
                    timeLeft--; // 술래가 풀려난 후에만 시간 감소
                }
            }
        };
        gameTask.runTaskTimer(plugin, 0L, 20L);
    }

    public void stopGame(boolean seekerWin) {
        isRunning = false;
        if (gameTask != null && !gameTask.isCancelled()) gameTask.cancel();
        if (jailTask != null && !jailTask.isCancelled()) jailTask.cancel();

        if (seekerWin) {
            Bukkit.broadcastMessage(configManager.getMessage("seeker-win"));
        } else {
            Bukkit.broadcastMessage(configManager.getMessage("runner-win"));
        }

        Location lobby = configManager.getLocation("lobby");
        if (lobby != null) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.teleport(lobby);
                p.getInventory().clear();
                p.getActivePotionEffects().forEach(effect -> p.removePotionEffect(effect.getType()));
                p.setGameMode(org.bukkit.GameMode.SURVIVAL); // 게임 모드 초기화
                p.setHealth(20);
                p.setFoodLevel(20);
            }
        }
        playerManager.clear();
    }
    
    public void forceStop() {
        isRunning = false;
        if (gameTask != null && !gameTask.isCancelled()) gameTask.cancel();
        if (jailTask != null && !jailTask.isCancelled()) jailTask.cancel();
        
        Bukkit.broadcastMessage(ChatColor.RED + "게임이 강제 종료되었습니다.");
        
        Location lobby = configManager.getLocation("lobby");
        if (lobby != null) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.teleport(lobby);
                p.getInventory().clear();
                p.getActivePotionEffects().forEach(effect -> p.removePotionEffect(effect.getType()));
                p.setGameMode(org.bukkit.GameMode.SURVIVAL);
                p.setHealth(20);
                p.setFoodLevel(20);
            }
        }
        playerManager.clear();
    }

    public boolean isRunning() {
        return isRunning;
    }

    public boolean isSeekerReleased() {
        return isSeekerReleased;
    }
}
