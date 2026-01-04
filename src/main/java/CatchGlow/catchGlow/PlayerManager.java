package CatchGlow.catchGlow;

import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerManager {
    private final Set<UUID> seekers = new HashSet<>();
    private final Set<UUID> runners = new HashSet<>();

    public void addSeeker(Player player) {
        seekers.add(player.getUniqueId());
        runners.remove(player.getUniqueId());
    }

    public void addRunner(Player player) {
        runners.add(player.getUniqueId());
        seekers.remove(player.getUniqueId());
    }

    public boolean isSeeker(Player player) {
        return seekers.contains(player.getUniqueId());
    }

    public boolean isRunner(Player player) {
        return runners.contains(player.getUniqueId());
    }

    public Set<UUID> getSeekers() {
        return new HashSet<>(seekers);
    }

    public Set<UUID> getRunners() {
        return new HashSet<>(runners);
    }

    public void clear() {
        seekers.clear();
        runners.clear();
    }

    public void removeRunner(Player player) {
        runners.remove(player.getUniqueId());
    }
}
