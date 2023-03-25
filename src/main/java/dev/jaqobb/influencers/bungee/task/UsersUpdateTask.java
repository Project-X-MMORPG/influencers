package dev.jaqobb.influencers.bungee.task;

import dev.jaqobb.influencers.api.service.ServiceType;
import dev.jaqobb.influencers.api.user.User;
import dev.jaqobb.influencers.bungee.InfluencersBungeePlugin;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class UsersUpdateTask implements Runnable {

    private InfluencersBungeePlugin plugin;

    public UsersUpdateTask(InfluencersBungeePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        this.plugin.debug("Updating online users...");
        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
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
