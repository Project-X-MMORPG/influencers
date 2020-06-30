package dev.jaqobb.influencers.spigot;

import dev.jaqobb.influencers.api.InfluencersPlugin;
import dev.jaqobb.influencers.api.basic.Configuration;
import dev.jaqobb.influencers.api.basic.Messages;
import dev.jaqobb.influencers.api.rank.type.YouTubeRank;
import dev.jaqobb.influencers.api.user.UserRepository;
import dev.jaqobb.influencers.spigot.basic.SpigotConfiguration;
import dev.jaqobb.influencers.spigot.basic.SpigotMessages;
import dev.jaqobb.influencers.spigot.command.InfluencersCommand;
import dev.jaqobb.influencers.spigot.command.YouTubeCommand;
import dev.jaqobb.influencers.spigot.listener.PlayerListener;
import dev.jaqobb.influencers.spigot.metrics.Metrics;
import dev.jaqobb.influencers.spigot.task.UsersUpdateTask;
import dev.jaqobb.influencers.spigot.user.SpigotUserRepository;
import dev.jaqobb.influencers.spigot.util.ColorHelper;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class InfluencersSpigotPlugin extends JavaPlugin implements InfluencersPlugin {

	private Configuration configuration;
	private Messages messages;
	private UserRepository userRepository;
	private Metrics metrics;

	@Override
	public void onLoad() {
		this.configuration = new SpigotConfiguration(this);
		if (this.configuration.isYouTubeEnabled()) {
			if (this.configuration.getYouTubeAPIKey() == null || this.configuration.getYouTubeAPIKey().isEmpty()) {
				this.configuration.setYouTubeEnabled(false);
				Bukkit.getConsoleSender().sendMessage(ColorHelper.colorize("&cDisabling YouTube service because the API key could not be found or is empty."));
			}
			if (this.configuration.getYouTubeRanks().size() < 2) {
				this.configuration.setYouTubeEnabled(false);
				Bukkit.getConsoleSender().sendMessage(ColorHelper.colorize("&cDisabling YouTube service because there are less than 2 ranks for it."));
			}
			YouTubeRank noRank = this.configuration.getYouTubeRanks().get(0);
			if (noRank.getSubscribers() > 0 || noRank.getTotalViews() > 0 || noRank.getAverageViews() > 0 || noRank.getPublishTime().toEpochMilli() > 0 || !noRank.getTitleContains().isEmpty() || !noRank.getDescriptionContains().isEmpty()) {
				this.configuration.setYouTubeEnabled(false);
				Bukkit.getConsoleSender().sendMessage(ColorHelper.colorize("&cDisabling YouTube service because the first rank (no rank) is not set properly. The no rank has to have no requirements for it. Make sure the no rank is set accordingly to the information found in the default configuration."));
			}
		}
		if (this.configuration.isTwitchEnabled()) {
			if (this.configuration.getTwitchAPIKey() == null || this.configuration.getTwitchAPIKey().isEmpty()) {
				this.configuration.setTwitchEnabled(false);
				Bukkit.getConsoleSender().sendMessage(ColorHelper.colorize("&cDisabling Twitch service because the API key could not be found or is empty."));
			}
			if (this.configuration.getTwitchRanks().size() < 2) {
				this.configuration.setTwitchEnabled(false);
				Bukkit.getConsoleSender().sendMessage(ColorHelper.colorize("&cDisabling Twitch service because there are less than 2 ranks for it."));
			}
		}
		this.messages = new SpigotMessages(this);
		this.userRepository = new SpigotUserRepository(this);
		this.userRepository.loadAll();
	}

	@Override
	public void onEnable() {
		this.debug("Registering commands...");
		this.getCommand("influencers").setExecutor(new InfluencersCommand(this));
		this.getCommand("youtube").setExecutor(new YouTubeCommand(this));
		this.getCommand("youtube").setTabCompleter(new YouTubeCommand(this));
		this.debug("Registering listeners...");
		this.getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
		this.debug("Starting tasks...");
		this.getServer().getScheduler().runTaskTimerAsynchronously(this, new UsersUpdateTask(this), this.configuration.getUsersUpdateDelay(), this.configuration.getUsersUpdatePeriod());
		if (this.configuration.areMetricsEnabled()) {
			this.debug("Starting metrics...");
			this.metrics = new Metrics(this);
		}
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

	@Override
	public void debug(String message, Exception exception) {
		if (this.configuration.isDebugModeEnabled()) {
			this.getLogger().log(Level.INFO, message, exception);
		}
	}

	public Metrics getMetrics() {
		return this.metrics;
	}
}
