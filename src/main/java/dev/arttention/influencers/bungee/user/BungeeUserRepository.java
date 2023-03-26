package dev.arttention.influencers.bungee.user;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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
import dev.arttention.influencers.bungee.provider.MySQLProvider;
import dev.arttention.libraries.api.mysql.MySQL;
import net.md_5.bungee.api.ProxyServer;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.SQLException;
import java.time.Instant;
import java.util.*;
import java.util.Map.Entry;

public class BungeeUserRepository extends UserRepository {

    private final InfluencersBungeePlugin plugin;

    public BungeeUserRepository(InfluencersBungeePlugin plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    public void loadAll() {
        this.plugin.debug("Loading all users...");

        MySQL mySQL = MySQLProvider.getInstance().getConnection("youtubermanager");
        mySQL.executeQuery("SELECT * FROM `youtubers` ", resultSet -> {
            try {
                while (resultSet.next()) {
                    String jsonString = resultSet.getString("services");
                    JSONObject jsonObject = new JSONObject(jsonString);
                    String name = jsonObject.getString("name");
                    JSONObject blacklistedServices = jsonObject.getJSONObject("blacklisted-services");
                    boolean youtubeBlacklisted = blacklistedServices.getBoolean("youtube");
                    boolean twitchBlacklisted = blacklistedServices.getBoolean("twitch");
                    Set<ServiceType> blacklistedServiceTypes = EnumSet.noneOf(ServiceType.class);
                    if (youtubeBlacklisted) {
                        blacklistedServiceTypes.add(ServiceType.YOUTUBE);
                    }
                    if (twitchBlacklisted) {
                        // TODO: Add Twitch service type to ServiceType enum.
                    }
                    JSONObject services = jsonObject.getJSONObject("services");
                    Map<ServiceType, Service> serviceMap = new EnumMap<>(ServiceType.class);
                    if (services.has("youtube")) {
                        JSONObject youtubeJson = services.getJSONObject("youtube");
                        Instant lastCacheTime = Instant.ofEpochMilli(youtubeJson.getLong("last-cache-time"));
                        String channelId = youtubeJson.getString("channel-id");
                        String channelVerificationKey = youtubeJson.getString("channel-verification-key");
                        boolean channelVerified = youtubeJson.getBoolean("channel-verified");
                        Channel channel = null;
                        YouTubeRank currentRank = null;
                        if (channelVerified) {
                            JSONObject channelJson = youtubeJson.getJSONObject("channel");
                            String channelName = channelJson.getString("name");
                            long channelSubscribers = channelJson.getLong("subscribers");
                            long channelVideos = channelJson.getLong("videos");
                            long channelTotalViews = channelJson.getLong("total-views");
                            List<Video> channelLastVideos = new ArrayList<>(5);
                            JSONArray lastVideosJsonArray = channelJson.getJSONArray("last-videos");
                            for (int i = 0; i < lastVideosJsonArray.length(); i++) {
                                JSONObject videoJson = lastVideosJsonArray.getJSONObject(i);
                                String id = videoJson.getString("id");
                                String title = videoJson.getString("title");
                                String description = videoJson.getString("description");
                                Instant publishTime = Instant.ofEpochMilli(videoJson.getLong("publish-time"));
                                channelLastVideos.add(new Video(id, title, description, publishTime));
                            }
                            channel = new Channel(channelName, channelSubscribers, channelVideos, channelTotalViews, channelLastVideos);
                            currentRank = this.plugin.getConfiguration().getYouTubeRank(youtubeJson.getString("current-rank"));
                        }
                        serviceMap.put(ServiceType.YOUTUBE, new YouTubeService(lastCacheTime, name, channelId, channelVerificationKey, channelVerified, channel, currentRank));
                    } else {
                        // TODO: Add loading Twitch service.
                    }
                    this.add(new User(UUID.randomUUID(), name, blacklistedServiceTypes, serviceMap, Instant.now()));
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

        });
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
