package dev.arttention.influencers.bungee.user;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.arttention.influencers.bungee.provider.MySQLProvider;
import dev.arttention.libraries.api.mysql.MySQL;
import dev.arttention.influencers.api.rank.type.YouTubeRank;
import dev.arttention.influencers.api.service.Service;
import dev.arttention.influencers.api.service.ServiceType;
import dev.arttention.influencers.api.service.list.TwitchService;
import dev.arttention.influencers.api.service.list.YouTubeService;
import dev.arttention.influencers.api.service.list.YouTubeService.Channel;
import dev.arttention.influencers.api.service.list.YouTubeService.Channel.Video;
import dev.arttention.influencers.api.user.User;
import dev.arttention.influencers.api.user.UserRepository;
import dev.arttention.influencers.bungee.InfluencersBungeePlugin;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class BungeeUserRepository extends UserRepository {

    private InfluencersBungeePlugin plugin;
    private File directory;

    public BungeeUserRepository(InfluencersBungeePlugin plugin) {
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
            try {
                Configuration configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
                String name = configuration.getString("name");
                Set<ServiceType> blacklistedServices = EnumSet.noneOf(ServiceType.class);
                for (String key : configuration.getSection("blacklisted-services").getKeys()) {
                    if (configuration.getSection("blacklisted-services").getBoolean(key)) {
                        blacklistedServices.add(ServiceType.LIST.value(key));
                    }
                }
                Map<ServiceType, Service> services = new EnumMap<>(ServiceType.class);
                for (String key : configuration.getSection("services").getKeys()) {
                    Configuration serviceConfiguration = configuration.getSection("services").getSection(key);
                    ServiceType serviceType = ServiceType.LIST.value(key);
                    if (serviceType == ServiceType.YOUTUBE) {
                        Instant lastCacheTime = Instant.ofEpochMilli(serviceConfiguration.getLong("last-cache-time"));
                        String channelId = serviceConfiguration.getString("channel-id");
                        String channelVerificationKey = serviceConfiguration.getString("channel-verification-key");
                        boolean channelVerified = serviceConfiguration.getBoolean("channel-verified");
                        Channel channel = null;
                        YouTubeRank currentRank = null;
                        if (channelVerified) {
                            Configuration channelConfiguration = serviceConfiguration.getSection("channel");
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
                Instant createdAt;
                if (configuration.contains("created-at")) {
                    createdAt = Instant.ofEpochMilli(configuration.getLong("created-at"));
                } else {
                    createdAt = Instant.now();
                }
                this.add(new User(uniqueId, name, blacklistedServices, services, createdAt));
            } catch (IOException exception) {
                this.plugin.debug("Could not load \"" + fileName + "\" user file.", exception);
            }
        }
    }

    @Override
    public boolean save(User user) {

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", user.getName());

        // Save blacklisted services
        JsonObject blacklistedServicesObject = new JsonObject();
        for (ServiceType serviceType : ServiceType.values()) {
            blacklistedServicesObject.addProperty(serviceType.getId(), user.hasServiceBlacklisted(serviceType));
        }
        jsonObject.add("blacklisted-services", blacklistedServicesObject);

        // Save services
        JsonObject servicesObject = new JsonObject();
        for (Entry<ServiceType, Service> entry : user.getServices().entrySet()) {
            ServiceType serviceType = entry.getKey();
            Service service = entry.getValue();
            JsonObject serviceObject = new JsonObject();

            if (serviceType == ServiceType.YOUTUBE) {
                YouTubeService youTubeService = (YouTubeService) service;
                serviceObject.addProperty("last-cache-time", youTubeService.getLastCacheTime().toEpochMilli());
                serviceObject.addProperty("channel-id", youTubeService.getChannelId());
                serviceObject.addProperty("channel-verification-key", youTubeService.getChannelVerificationKey());
                serviceObject.addProperty("channel-verified", youTubeService.isChannelVerified());

                if (youTubeService.isChannelVerified()) {
                    Channel channel = youTubeService.getChannel();
                    JsonObject channelObject = new JsonObject();
                    channelObject.addProperty("name", channel.getName());
                    channelObject.addProperty("subscribers", channel.getSubscribers());
                    channelObject.addProperty("videos", channel.getVideos());
                    channelObject.addProperty("total-views", channel.getTotalViews());

                    JsonArray lastVideosArray = new JsonArray();
                    for (Video lastVideo : channel.getLastVideos()) {
                        JsonObject lastVideoObject = new JsonObject();
                        lastVideoObject.addProperty("id", lastVideo.getId());
                        lastVideoObject.addProperty("title", lastVideo.getTitle());
                        lastVideoObject.addProperty("description", lastVideo.getDescription());
                        lastVideoObject.addProperty("publish-time", lastVideo.getPublishTime().toEpochMilli());
                        lastVideosArray.add(lastVideoObject);
                    }
                    channelObject.add("last-videos", lastVideosArray);
                    serviceObject.add("channel", channelObject);
                    serviceObject.addProperty("current-rank", youTubeService.getCurrentRank().getId());
                }
            }
            servicesObject.add(serviceType.getId(), serviceObject);
        }
        jsonObject.add("services", servicesObject);
        MySQL mySQL = MySQLProvider.getInstance().getConnection("youtubermanager");
        mySQL.executeUpdate("INSERT INTO `youtubers`(`unique_id`, `name`, services) VALUES (?,?,?)", user.getUniqueId().toString(), user.getName(), jsonObject.toString());
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
                    for (String newRankCommand : newRank.getCommands()) {
                        ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), newRankCommand.replace("{player}", user.getName()));
                    }
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
