package dev.jaqobb.influencers.bungee.listener;

import dev.jaqobb.influencers.api.service.ServiceType;
import dev.jaqobb.influencers.api.user.User;
import dev.jaqobb.influencers.bungee.InfluencersBungeePlugin;

import java.time.Instant;
import java.util.EnumMap;
import java.util.EnumSet;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PlayerListener implements Listener {

    private InfluencersBungeePlugin plugin;

    public PlayerListener(InfluencersBungeePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();
        User user = this.plugin.getUserRepository().get(player.getUniqueId());
        if (user != null) {
            user.setName(player.getName());
        } else {
            this.plugin.getUserRepository().add(new User(player.getUniqueId(), player.getName(), EnumSet.noneOf(ServiceType.class), new EnumMap<>(ServiceType.class), Instant.now()));
        }
    }

    @EventHandler
    public void onPlayerKick(PlayerDisconnectEvent event) {
        this.savePlayer(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(ServerKickEvent event) {
        this.savePlayer(event.getPlayer());
    }

    private void savePlayer(ProxiedPlayer player) {
        User user = this.plugin.getUserRepository().get(player.getUniqueId());
        if (user == null) {
            return;
        }
        if (!this.plugin.getUserRepository().save(user)) {
            this.plugin.getUserRepository().remove(user);
        }
    }
}
