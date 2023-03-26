package dev.arttention.influencers.bungee;

import dev.arttention.influencers.bungee.basic.BungeeConfiguration;
import dev.arttention.influencers.bungee.basic.BungeeMessages;
import dev.arttention.influencers.bungee.command.YouTubeCommand;
import dev.arttention.influencers.bungee.configuration.MySQLDefaultConfig;
import dev.arttention.influencers.bungee.listener.PlayerListener;
import dev.arttention.influencers.bungee.provider.MySQLProvider;
import dev.arttention.influencers.bungee.task.UsersUpdateTask;
import dev.arttention.influencers.bungee.user.BungeeUserRepository;
import dev.arttention.influencers.bungee.util.ColorHelper;
import dev.arttention.libraries.api.configuration.ConfigurationLoader;
import dev.arttention.influencers.api.InfluencersPlugin;
import dev.arttention.influencers.api.basic.Configuration;
import dev.arttention.influencers.api.basic.Messages;
import dev.arttention.influencers.api.rank.type.YouTubeRank;
import dev.arttention.influencers.api.user.UserRepository;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import lombok.Getter;
import lombok.SneakyThrows;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

@Getter
public class InfluencersBungeePlugin extends Plugin implements InfluencersPlugin {

    private Configuration configuration;
    private Messages messages;
    private MySQLDefaultConfig mySQLConfig;
    private ConfigurationLoader configurationLoader;
    private UserRepository userRepository;

    @Override
    public void onLoad() {
        this.configuration = new BungeeConfiguration(this);
        if (this.configuration.isYouTubeEnabled()) {
            if (this.configuration.getYouTubeAPIKey() == null || this.configuration.getYouTubeAPIKey().isEmpty()) {
                this.configuration.setYouTubeEnabled(false);
                ProxyServer.getInstance().getConsole().sendMessage(ColorHelper.colorize("&cDisabling YouTube service because the API key could not be found or is empty."));
            }
            if (this.configuration.getYouTubeRanks().size() < 2) {
                this.configuration.setYouTubeEnabled(false);
                ProxyServer.getInstance().getConsole().sendMessage(ColorHelper.colorize("&cDisabling YouTube service because there are less than 2 ranks for it."));
            }
            YouTubeRank noRank = this.configuration.getYouTubeRanks().get(0);
            if (noRank.getSubscribers() > 0 || noRank.getTotalViews() > 0 || noRank.getAverageViews() > 0 || noRank.getPublishTime().toEpochMilli() > 0 || !noRank.getTitleContains().isEmpty() || !noRank.getDescriptionContains().isEmpty()) {
                this.configuration.setYouTubeEnabled(false);
                ProxyServer.getInstance().getConsole().sendMessage(ColorHelper.colorize("&cDisabling YouTube service because the first rank (no rank) is not set properly. The no rank has to have no requirements for it. Make sure the no rank is set accordingly to the information found in the default configuration."));
            }
        }
        if (this.configuration.isTwitchEnabled()) {
            if (this.configuration.getTwitchAPIKey() == null || this.configuration.getTwitchAPIKey().isEmpty()) {
                this.configuration.setTwitchEnabled(false);
                ProxyServer.getInstance().getConsole().sendMessage(ColorHelper.colorize("&cDisabling Twitch service because the API key could not be found or is empty."));
            }
            if (this.configuration.getTwitchRanks().size() < 2) {
                this.configuration.setTwitchEnabled(false);
                ProxyServer.getInstance().getConsole().sendMessage(ColorHelper.colorize("&cDisabling Twitch service because there are less than 2 ranks for it."));
            }
        }
        this.messages = new BungeeMessages(this);
        this.userRepository = new BungeeUserRepository(this);
        this.userRepository.loadAll();
        this.configurationLoader = new ConfigurationLoader();
        this.mySQLConfig = configurationLoader.getConfiguration(MySQLDefaultConfig.class);
    }

    @SneakyThrows
    @Override
    public void onEnable() {
        this.debug("Registering commands...");
        this.getProxy().getPluginManager().registerCommand(this, new YouTubeCommand(this));
        this.debug("Registering listeners...");
        this.getProxy().getPluginManager().registerListener(this, new PlayerListener(this));
        this.debug("Starting tasks...");
        this.getProxy().getScheduler().schedule(this, new UsersUpdateTask(this), this.configuration.getUsersUpdateDelay(), this.configuration.getUsersUpdatePeriod(), TimeUnit.MILLISECONDS);
        openSQLConnections();
    }

    @Override
    public void onDisable() {
        if (this.configuration.shouldSaveUsersOnPluginDisable()) {
            this.userRepository.saveAll();
        }
    }

    @Override
    public Configuration getConfiguration() {
        return this.configuration;
    }

    @Override
    public Messages getMessages() {
        return this.messages;
    }

    @Override
    public UserRepository getUserRepository() {
        return this.userRepository;
    }

    @Override
    public void debug(String message) {
        if (this.configuration.isDebugModeEnabled()) {
            this.getLogger().log(Level.INFO, message);
        }
    }

    private void openSQLConnections() {
        new MySQLProvider(mySQLConfig);
        MySQLProvider.getInstance().connect("youtubermanager");
    }


    @Override
    public void debug(String message, Exception exception) {
        if (this.configuration.isDebugModeEnabled()) {
            this.getLogger().log(Level.INFO, message, exception);
        }
    }
}
