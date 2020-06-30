package dev.jaqobb.influencers.spigot.task;

import dev.jaqobb.influencers.api.service.ServiceType;
import dev.jaqobb.influencers.api.user.User;
import dev.jaqobb.influencers.spigot.InfluencersSpigotPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class UsersUpdateTask implements Runnable {

	private InfluencersSpigotPlugin plugin;

	public UsersUpdateTask(InfluencersSpigotPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public void run() {
		this.plugin.debug("Updating online users...");
		for (Player player : Bukkit.getOnlinePlayers()) {
			User user = this.plugin.getUserRepository().get(player.getUniqueId());
			if (user == null) {
				continue;
			}
			for (ServiceType serviceType : user.getServices().keySet()) {
				this.plugin.getUserRepository().updateService(user, serviceType);
			}
		}
	}
}
