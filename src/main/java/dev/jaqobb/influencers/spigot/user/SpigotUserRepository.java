package dev.jaqobb.influencers.spigot.user;

import dev.jaqobb.influencers.api.rank.type.YouTubeRank;
import dev.jaqobb.influencers.api.service.Service;
import dev.jaqobb.influencers.api.service.ServiceType;
import dev.jaqobb.influencers.api.service.list.TwitchService;
import dev.jaqobb.influencers.api.service.list.YouTubeService;
import dev.jaqobb.influencers.api.service.list.YouTubeService.Channel;
import dev.jaqobb.influencers.api.service.list.YouTubeService.Channel.Video;
import dev.jaqobb.influencers.api.user.User;
import dev.jaqobb.influencers.api.user.UserRepository;
import dev.jaqobb.influencers.spigot.InfluencersSpigotPlugin;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class SpigotUserRepository extends UserRepository {

	private InfluencersSpigotPlugin plugin;
	private File directory;

	public SpigotUserRepository(InfluencersSpigotPlugin plugin) {
		super(plugin);
		this.plugin = plugin;
		this.directory = new File(this.plugin.getDataFolder(), "users");
		if (!this.directory.exists() && !this.directory.mkdirs()) {
			throw new RuntimeException("Could not create users directory");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void loadAll() {
		this.plugin.debug("Loading all users...");
		for (File file : this.directory.listFiles()) {
			String fileName = file.getName();
			int dotIndex = fileName.lastIndexOf('.');
			String fileNameWithoutExtension = dotIndex == -1 ? fileName : fileName.substring(0, dotIndex);
			String fileExtension = dotIndex == -1 ? "" : fileName.substring(dotIndex + 1);
			if (!fileExtension.equals("yml")) {
				continue;
			}
			UUID uniqueId;
			try {
				uniqueId = UUID.fromString(fileNameWithoutExtension);
			} catch (IllegalArgumentException exception) {
				this.plugin.debug("Could not parse \"" + fileNameWithoutExtension + "\" user file name to UUID.");
				continue;
			}
			FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);
			String name = configuration.getString("name");
			Set<ServiceType> blacklistedServices = EnumSet.noneOf(ServiceType.class);
			if (configuration.isSet("blacklisted-services") && configuration.isConfigurationSection("blacklisted-services")) {
				for (String key : configuration.getConfigurationSection("blacklisted-services").getKeys(false)) {
					if (configuration.getConfigurationSection("blacklisted-services").getBoolean(key)) {
						blacklistedServices.add(ServiceType.LIST.value(key));
					}
				}
			}
			Map<ServiceType, Service> services = new EnumMap<>(ServiceType.class);
			if (configuration.isSet("services") && configuration.isConfigurationSection("services")) {
				for (String key : configuration.getConfigurationSection("services").getKeys(false)) {
					ConfigurationSection serviceConfiguration = configuration.getConfigurationSection("services").getConfigurationSection(key);
					ServiceType serviceType = ServiceType.LIST.value(key);
					if (serviceType == ServiceType.YOUTUBE) {
						Instant lastCacheTime = Instant.ofEpochMilli(serviceConfiguration.getLong("last-cache-time"));
						String channelId = serviceConfiguration.getString("channel-id");
						String channelVerificationKey = serviceConfiguration.getString("channel-verification-key");
						boolean channelVerified = serviceConfiguration.getBoolean("channel-verified");
						Channel channel = null;
						YouTubeRank currentRank = null;
						if (channelVerified) {
							ConfigurationSection channelConfiguration = serviceConfiguration.getConfigurationSection("channel");
							String channelName = channelConfiguration.getString("name");
							long channelSubscribers = channelConfiguration.getLong("subscribers");
							long channelVideos = channelConfiguration.getLong("videos");
							long channelTotalViews = channelConfiguration.getLong("total-views");
							List<Video> channelLastVideos = new ArrayList<>(5);
							for (Object lastVideo : channelConfiguration.getList("last-videos")) {
								Map<String, Object> data = (Map<String, Object>) lastVideo;
								String id = (String) data.get("id");
								String title = (String) data.get("title");
								String description = (String) data.get("description");
								Instant publishTime = Instant.ofEpochMilli((long) data.get("publish-time"));
								channelLastVideos.add(new Video(id, title, description, publishTime));
							}
							channel = new Channel(channelName, channelSubscribers, channelVideos, channelTotalViews, channelLastVideos);
							currentRank = this.plugin.getConfiguration().getYouTubeRank(serviceConfiguration.getString("current-rank"));
						}
						services.put(ServiceType.YOUTUBE, new YouTubeService(lastCacheTime, name, channelId, channelVerificationKey, channelVerified, channel, currentRank));
					} else {
						// TODO: Add loading Twitch service.
					}
				}
			}
			Instant createdAt;
			if (configuration.isSet("created-at")) {
				createdAt = Instant.ofEpochMilli(configuration.getLong("created-at"));
			} else {
				createdAt = Instant.now();
			}
			this.add(new User(uniqueId, name, blacklistedServices, services, createdAt));
		}
	}

	@Override
	public boolean save(User user) {
		if (user.getCreatedAt().plusMillis(this.plugin.getConfiguration().getUserSafeToSaveDelay()).compareTo(Instant.now()) > 0) {
			return false;
		}
		File file = new File(this.directory, user.getUniqueId() + ".yml");
		if (file.exists() && !file.delete()) {
			this.plugin.debug("Could not delete \"" + user.getUniqueId() + ".yml\" user file.");
			return true;
		}
		try {
			if (!file.createNewFile()) {
				this.plugin.debug("Could not create \"" + user.getUniqueId() + ".yml\" user file.");
				return true;
			}
		} catch (IOException exception) {
			this.plugin.debug("Could not create \"" + user.getUniqueId() + ".yml\" user file.", exception);
			return true;
		}
		try {
			YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
			configuration.set("name", user.getName());
			configuration.createSection("blacklisted-services");
			for (ServiceType serviceType : ServiceType.values()) {
				configuration.getConfigurationSection("blacklisted-services").set(serviceType.getId(), user.hasServiceBlacklisted(serviceType));
			}
			configuration.createSection("services");
			for (Entry<ServiceType, Service> entry : user.getServices().entrySet()) {
				ServiceType serviceType = entry.getKey();
				ConfigurationSection serviceConfiguration = configuration.getConfigurationSection("services").createSection(serviceType.getId());
				if (serviceType == ServiceType.YOUTUBE) {
					YouTubeService service = (YouTubeService) entry.getValue();
					serviceConfiguration.set("last-cache-time", service.getLastCacheTime().toEpochMilli());
					serviceConfiguration.set("channel-id", service.getChannelId());
					serviceConfiguration.set("channel-verification-key", service.getChannelVerificationKey());
					serviceConfiguration.set("channel-verified", service.isChannelVerified());
					if (service.isChannelVerified()) {
						Channel channel = service.getChannel();
						ConfigurationSection channelConfiguration = serviceConfiguration.createSection("channel");
						channelConfiguration.set("name", channel.getName());
						channelConfiguration.set("subscribers", channel.getSubscribers());
						channelConfiguration.set("videos", channel.getVideos());
						channelConfiguration.set("total-views", channel.getTotalViews());
						List<Map<String, Object>> lastVideos = new ArrayList<>(channel.getLastVideos().size());
						for (Video lastVideo : channel.getLastVideos()) {
							Map<String, Object> data = new HashMap<>(4, 1.0F);
							data.put("id", lastVideo.getId());
							data.put("title", lastVideo.getTitle());
							data.put("description", lastVideo.getDescription());
							data.put("publish-time", lastVideo.getPublishTime().toEpochMilli());
							lastVideos.add(data);
						}
						channelConfiguration.set("last-videos", lastVideos);
						serviceConfiguration.set("current-rank", service.getCurrentRank().getId());
					}
				} else {
					// TODO: Add saving Twitch service.
				}
			}
			configuration.save(file);
		} catch (IOException exception) {
			this.plugin.debug("Could not save \"" + user.getUniqueId() + ".yml\" user file.", exception);
		}
		return true;
	}

	@Override
	public void updateService(User user, ServiceType serviceType) {
		if (user.hasServiceBlacklisted(serviceType)) {
			return;
		}
		if (serviceType == ServiceType.YOUTUBE && this.plugin.getConfiguration().isYouTubeEnabled()) {
			YouTubeService service = (YouTubeService) user.getService(ServiceType.YOUTUBE);
			if (!service.isChannelVerified()) {
				return;
			}
			if (service.getLastCacheTime().plusMillis(this.plugin.getConfiguration().getYouTubeCacheTime().toEpochMilli()).compareTo(Instant.now()) >= 0) {
				return;
			}
			YouTubeRank oldRank = service.getCurrentRank();
			String result = service.update(this.plugin);
			if (result == null) {
				YouTubeRank newRank = service.getCurrentRank();
				if ((oldRank == null || !oldRank.equals(newRank)) && newRank != null) {
					this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
						for (String newRankCommand : newRank.getCommands()) {
							Bukkit.dispatchCommand(Bukkit.getConsoleSender(), newRankCommand.replace("{player}", user.getName()));
						}
					});
				}
			}
			service.updateLastCacheTime();
		} else if (this.plugin.getConfiguration().isTwitchEnabled()) {
			TwitchService service = (TwitchService) user.getService(ServiceType.TWITCH);
			if (service.getLastCacheTime().plusMillis(this.plugin.getConfiguration().getTwitchCacheTime().toEpochMilli()).compareTo(Instant.now()) >= 0) {
				return;
			}
			String result = service.update(this.plugin);
			if (result == null) {
				// TODO: Add updating Twitch service.
			}
			service.updateLastCacheTime();
		}
	}
}
