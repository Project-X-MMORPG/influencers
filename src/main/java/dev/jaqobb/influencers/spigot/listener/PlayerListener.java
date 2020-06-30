package dev.jaqobb.influencers.spigot.listener;

import dev.jaqobb.influencers.api.service.ServiceType;
import dev.jaqobb.influencers.api.user.User;
import dev.jaqobb.influencers.spigot.InfluencersSpigotPlugin;
import java.time.Instant;
import java.util.EnumMap;
import java.util.EnumSet;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

	private InfluencersSpigotPlugin plugin;

	public PlayerListener(InfluencersSpigotPlugin plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		User user = this.plugin.getUserRepository().get(player.getUniqueId());
		if (user != null) {
			user.setName(player.getName());
		} else {
			this.plugin.getUserRepository().add(new User(player.getUniqueId(), player.getName(), EnumSet.noneOf(ServiceType.class), new EnumMap<>(ServiceType.class), Instant.now()));
		}
	}

	@EventHandler
	public void onPlayerKick(PlayerKickEvent event) {
		this.savePlayer(event.getPlayer());
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		this.savePlayer(event.getPlayer());
	}

	private void savePlayer(Player player) {
		User user = this.plugin.getUserRepository().get(player.getUniqueId());
		if (user == null) {
			return;
		}
		if (!this.plugin.getUserRepository().save(user)) {
			this.plugin.getUserRepository().remove(user);
		}
	}
}
