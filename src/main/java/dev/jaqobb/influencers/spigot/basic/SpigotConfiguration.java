package dev.jaqobb.influencers.spigot.basic;

import dev.jaqobb.influencers.api.basic.Configuration;
import dev.jaqobb.influencers.api.rank.type.TwitchRank;
import dev.jaqobb.influencers.api.rank.type.YouTubeRank;
import dev.jaqobb.influencers.spigot.InfluencersSpigotPlugin;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class SpigotConfiguration implements Configuration {

	private InfluencersSpigotPlugin plugin;
	private File file;
	private FileConfiguration fileConfiguration;
	private boolean debugModeEnabled;
	private boolean metricsEnabled;
	private ZoneOffset timeZone;
	private String timeFormat;
	private boolean saveUsersOnPluginDisable;
	private long userSafeToSaveDelay;
	private long usersUpdateDelay;
	private long usersUpdatePeriod;
	private boolean youTubeEnabled;
	private String youTubeAPIKey;
	private Instant youTubeCacheTime;
	private List<YouTubeRank> youTubeRanks;
	private boolean twitchEnabled;
	private String twitchAPIKey;
	private Instant twitchCacheTime;
	private List<TwitchRank> twitchRanks;

	public SpigotConfiguration(InfluencersSpigotPlugin plugin) {
		this.plugin = plugin;
		this.file = new File(this.plugin.getDataFolder(), "configuration.yml");
		if (!this.file.exists()) {
			this.plugin.saveResource("configuration.yml", false);
		}
		this.reload();
	}

	@Override
	public boolean isDebugModeEnabled() {
		return this.debugModeEnabled;
	}

	@Override
	public void setDebugModeEnabled(boolean enabled) {
		this.debugModeEnabled = enabled;
	}

	@Override
	public boolean areMetricsEnabled() {
		return this.metricsEnabled;
	}

	@Override
	public void setMetricsEnabled(boolean enabled) {
		this.metricsEnabled = enabled;
	}

	@Override
	public ZoneOffset getTimeZone() {
		return this.timeZone;
	}

	@Override
	public void setTimeZone(ZoneOffset zone) {
		this.timeZone = zone;
	}

	@Override
	public String getTimeFormat() {
		return this.timeFormat;
	}

	@Override
	public void setTimeFormat(String format) {
		this.timeFormat = format;
	}

	@Override
	public boolean shouldSaveUsersOnPluginDisable() {
		return this.saveUsersOnPluginDisable;
	}

	@Override
	public void setShouldSaveUsersOnPluginDisable(boolean should) {
		this.saveUsersOnPluginDisable = should;
	}

	@Override
	public long getUserSafeToSaveDelay() {
		return this.userSafeToSaveDelay;
	}

	@Override
	public void setUserSaveToSaveDelay(long delay) {
		this.userSafeToSaveDelay = delay;
	}

	@Override
	public long getUsersUpdateDelay() {
		return this.usersUpdateDelay;
	}

	@Override
	public void setUsersUpdateDelay(long delay) {
		this.usersUpdateDelay = delay;
	}

	@Override
	public long getUsersUpdatePeriod() {
		return this.usersUpdateDelay;
	}

	@Override
	public void setUsersUpdatePeriod(long period) {
		this.usersUpdatePeriod = period;
	}

	@Override
	public boolean isYouTubeEnabled() {
		return this.youTubeEnabled;
	}

	@Override
	public void setYouTubeEnabled(boolean enabled) {
		this.youTubeEnabled = enabled;
	}

	@Override
	public String getYouTubeAPIKey() {
		return this.youTubeAPIKey;
	}

	@Override
	public void setYouTubeAPIKey(String apiKey) {
		this.youTubeAPIKey = apiKey;
	}

	@Override
	public Instant getYouTubeCacheTime() {
		return this.youTubeCacheTime;
	}

	@Override
	public void setYouTubeCacheTime(Instant time) {
		this.youTubeCacheTime = time;
	}

	@Override
	public List<YouTubeRank> getYouTubeRanks() {
		return Collections.unmodifiableList(this.youTubeRanks);
	}

	@Override
	public void setYouTubeRanks(List<YouTubeRank> ranks) {
		this.youTubeRanks = ranks;
	}

	@Override
	public boolean isTwitchEnabled() {
		return this.twitchEnabled;
	}

	@Override
	public void setTwitchEnabled(boolean enabled) {
		this.twitchEnabled = enabled;
	}

	@Override
	public String getTwitchAPIKey() {
		return this.twitchAPIKey;
	}

	@Override
	public void setTwitchAPIKey(String apiKey) {
		this.twitchAPIKey = apiKey;
	}

	@Override
	public Instant getTwitchCacheTime() {
		return this.twitchCacheTime;
	}

	@Override
	public void setTwitchCacheTime(Instant time) {
		this.twitchCacheTime = time;
	}

	@Override
	public List<TwitchRank> getTwitchRanks() {
		return Collections.unmodifiableList(this.twitchRanks);
	}

	@Override
	public void setTwitchRanks(List<TwitchRank> ranks) {
		this.twitchRanks = ranks;
	}

	@Override
	public void load() {
		try {
			this.debugModeEnabled = this.fileConfiguration.getBoolean("general.debug-mode-enabled");
			this.metricsEnabled = this.fileConfiguration.getBoolean("general.metrics-enabled");
			this.timeZone = ZoneOffset.of(this.fileConfiguration.getString("general.time-zone"));
			this.timeFormat = this.fileConfiguration.getString("general.time-format");
			this.saveUsersOnPluginDisable = this.fileConfiguration.getBoolean("general.save-users-on-plugin-disable");
			this.userSafeToSaveDelay = this.fileConfiguration.getLong("general.user-safe-to-save-delay");
			this.usersUpdateDelay = this.fileConfiguration.getLong("general.users-update.delay");
			this.usersUpdatePeriod = this.fileConfiguration.getLong("general.users-update.period");
			this.youTubeEnabled = this.fileConfiguration.getBoolean("youtube.enabled");
			this.youTubeAPIKey = this.fileConfiguration.getString("youtube.api-key");
			this.youTubeCacheTime = Instant.ofEpochMilli(this.fileConfiguration.getLong("youtube.cache-time"));
			this.youTubeRanks = this.fileConfiguration.getConfigurationSection("youtube.ranks").getKeys(false).stream()
				.map(id -> {
					ConfigurationSection section = this.fileConfiguration.getConfigurationSection("youtube.ranks." + id);
					String name = section.getString("name");
					long subscribers = section.getLong("subscribers");
					long totalViews = section.getLong("total-views");
					long averageViews = section.getLong("average-views");
					ConfigurationSection lastVideosConditionsSection = section.getConfigurationSection("last-videos-conditions");
					Instant publishTime = Instant.ofEpochMilli(lastVideosConditionsSection.getLong("publish-time"));
					List<String> titleContains = lastVideosConditionsSection.getStringList("title-contains");
					List<String> descriptionContains = lastVideosConditionsSection.getStringList("description-contains");
					List<String> commands = section.getStringList("commands");
					return new YouTubeRank(id, name, subscribers, totalViews, averageViews, publishTime, titleContains, descriptionContains, commands);
				})
				.collect(Collectors.toList());
			this.twitchEnabled = this.fileConfiguration.getBoolean("twitch.enabled");
			this.twitchAPIKey = this.fileConfiguration.getString("twitch.api-key");
			this.twitchCacheTime = Instant.ofEpochMilli(this.fileConfiguration.getLong("twitch.cache-time"));
			// TODO: Add loading Twitch ranks.
		} catch (Exception exception) {
			this.plugin.getLogger().log(Level.WARNING, "Could not load configuration.", exception);
		}
	}

	@Override
	public void save() {
		try {
			this.fileConfiguration.set("general.debug-mode-enabled", this.debugModeEnabled);
			this.fileConfiguration.set("general.metrics-enabled", this.metricsEnabled);
			this.fileConfiguration.set("general.time-zone", this.timeZone.getId());
			this.fileConfiguration.set("general.time-format", this.timeFormat);
			this.fileConfiguration.set("general.save-users-on-plugin-disable", this.saveUsersOnPluginDisable);
			this.fileConfiguration.set("general.user-safe-to-save-delay", this.userSafeToSaveDelay);
			this.fileConfiguration.set("general.users-update.delay", this.usersUpdateDelay);
			this.fileConfiguration.set("general.users-update.period", this.usersUpdatePeriod);
			this.fileConfiguration.set("youtube.enabled", this.youTubeEnabled);
			this.fileConfiguration.set("youtube.api-key", this.youTubeAPIKey);
			this.fileConfiguration.set("youtube.cache-time", this.youTubeCacheTime.toEpochMilli());
			for (YouTubeRank rank : this.youTubeRanks) {
				ConfigurationSection section = this.fileConfiguration.getConfigurationSection("youtube.ranks." + rank.getId());
				section.set("name", rank.getName());
				section.set("subscribers", rank.getSubscribers());
				section.set("total-views", rank.getTotalViews());
				section.set("average-views", rank.getAverageViews());
				ConfigurationSection lastVideosConditionsSection = section.getConfigurationSection("last-videos-conditions");
				lastVideosConditionsSection.set("publish-time", rank.getPublishTime().toEpochMilli());
				lastVideosConditionsSection.set("title-contains", rank.getTitleContains());
				lastVideosConditionsSection.set("description-contains", rank.getDescriptionContains());
				section.set("commands", rank.getCommands());
			}
			this.fileConfiguration.set("twitch.enabled", this.twitchEnabled);
			this.fileConfiguration.set("twitch.api-key", this.twitchAPIKey);
			this.fileConfiguration.set("twitch.cache-time", this.twitchCacheTime.toEpochMilli());
			// TODO: Add saving Twitch ranks.
			this.fileConfiguration.save(this.file);
		} catch (Exception exception) {
			this.plugin.getLogger().log(Level.WARNING, "Could not save configuration.", exception);
		}
	}

	@Override
	public void reload() {
		this.fileConfiguration = YamlConfiguration.loadConfiguration(this.file);
		InputStream configurationStream = this.plugin.getResource("configuration.yml");
		if (configurationStream != null) {
			this.fileConfiguration.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(configurationStream, StandardCharsets.UTF_8)));
		}
		this.load();
	}
}
