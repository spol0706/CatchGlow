package CatchGlow.catchGlow;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands implements CommandExecutor {
    private final GameManager gameManager;
    private final ConfigManager configManager;

    public Commands(GameManager gameManager, ConfigManager configManager) {
        this.gameManager = gameManager;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("플레이어만 사용할 수 있습니다.");
            return true;
        }

        Player player = (Player) sender;

        if (label.equalsIgnoreCase("시작")) {
            if (!player.hasPermission("catchglow.admin")) {
                player.sendMessage("권한이 없습니다.");
                return true;
            }
            gameManager.startGame();
            return true;
        }

        if (label.equalsIgnoreCase("중지")) {
            if (!player.hasPermission("catchglow.admin")) {
                player.sendMessage("권한이 없습니다.");
                return true;
            }
            gameManager.forceStop();
            return true;
        }
        
        if (label.equalsIgnoreCase("cgreload")) {
            if (!player.hasPermission("catchglow.admin")) {
                player.sendMessage("권한이 없습니다.");
                return true;
            }
            configManager.loadConfig();
            player.sendMessage("설정을 리로드했습니다.");
            return true;
        }

        return false;
    }
}
