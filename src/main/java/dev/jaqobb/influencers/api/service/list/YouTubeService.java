package dev.jaqobb.influencers.api.service.list;

import dev.jaqobb.influencers.api.InfluencersPlugin;
import dev.jaqobb.influencers.api.rank.type.YouTubeRank;
import dev.jaqobb.influencers.api.service.Service;
import dev.jaqobb.influencers.api.service.ServiceType;
import dev.jaqobb.influencers.api.service.list.YouTubeService.Channel.Video;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import kong.unirest.Unirest;
import org.json.JSONArray;
import org.json.JSONObject;

public class YouTubeService extends Service {

	public static final String CHANNEL_INFO_URL = "https://www.googleapis.com/youtube/v3/channels?key=%s&id=%s&part=snippet,contentDetails,statistics";
	public static final String PLAYLIST_INFO_URL = "https://www.googleapis.com/youtube/v3/playlistItems?key=%s&playlistId=%s&part=snippet";

	public static final String SERVICE_DISABLED_ERROR = "Service is disabled";
	public static final String CHANNEL_ALREADY_VERIFIED_ERROR = "Channel is already verified";
	public static final String CHANNEL_INFORMATION_RETRIEVE_ERROR = "Could not retrieve channel information";
	public static final String WRONG_CHANNEL_DESCRIPTION_ERROR = "Channel description does not contain your verification key";
	public static final String PLAYLIST_INFORMATION_RETRIEVE_ERROR = "Could not retrieve playlist information";

	private String channelId;
	private String channelVerificationKey;
	private boolean channelVerified;
	private Channel channel;
	private YouTubeRank currentRank;

	public YouTubeService(String playerName, String channelId) {
		super(Instant.ofEpochMilli(0));
		this.channelId = channelId;
		this.channelVerificationKey = "youtube_" + playerName;
		this.channelVerified = false;
		this.channel = null;
		this.currentRank = null;
	}

	public YouTubeService(String playerName, String channelId, boolean channelVerified) {
		super(Instant.ofEpochMilli(0));
		this.channelId = channelId;
		this.channelVerificationKey = "youtube_" + playerName;
		this.channelVerified = channelVerified;
		this.channel = null;
		this.currentRank = null;
	}

	public YouTubeService(Instant lastCacheTime, String playerName, String channelId, String channelVerificationKey, boolean channelVerified, Channel channel, YouTubeRank currentRank) {
		super(lastCacheTime);
		this.channelId = channelId;
		if (channelVerificationKey != null && !channelVerificationKey.isEmpty()) {
			this.channelVerificationKey = channelVerificationKey;
		} else {
			this.channelVerificationKey = "youtube_" + playerName;
		}
		this.channelVerified = channelVerified;
		this.channel = channel;
		this.currentRank = currentRank;
	}

	@Override
	public ServiceType getType() {
		return ServiceType.YOUTUBE;
	}

	public String getChannelId() {
		return this.channelId;
	}

	public String getChannelVerificationKey() {
		return this.channelVerificationKey;
	}

	public boolean isChannelVerified() {
		return this.channelVerified;
	}

	public void setChannelVerified(boolean verified) {
		this.channelVerified = verified;
	}

	public Channel getChannel() {
		return this.channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	public YouTubeRank getCurrentRank() {
		return this.currentRank;
	}

	public void setCurrentRank(YouTubeRank rank) {
		this.currentRank = rank;
	}

	@Override
	public String verify(Object... parameters) {
		InfluencersPlugin plugin = (InfluencersPlugin) parameters[0];
		plugin.debug("Verifying " + this.channelId + "'s YouTube channel...");
		if (!plugin.getConfiguration().isYouTubeEnabled()) {
			return this.verifyError(plugin, SERVICE_DISABLED_ERROR);
		}
		if (this.channelVerified) {
			return this.verifyError(plugin, CHANNEL_ALREADY_VERIFIED_ERROR);
		}
		String apiKey = plugin.getConfiguration().getYouTubeAPIKey();
		JSONObject channelInfo = Unirest.get(String.format(CHANNEL_INFO_URL, apiKey, this.channelId)).asJson().getBody().getObject();
		if (channelInfo.has("error")) {
			return this.verifyError(plugin, CHANNEL_INFORMATION_RETRIEVE_ERROR);
		}
		JSONArray channelItems = channelInfo.getJSONArray("items");
		if (channelItems.isEmpty()) {
			return this.verifyError(plugin, CHANNEL_INFORMATION_RETRIEVE_ERROR);
		}
		JSONObject channelItem = channelItems.getJSONObject(0);
		JSONObject channelSnippet = channelItem.getJSONObject("snippet");
		if (!channelSnippet.getString("description").contains(this.channelVerificationKey)) {
			return this.verifyError(plugin, WRONG_CHANNEL_DESCRIPTION_ERROR);
		}
		this.channelVerified = true;
		plugin.debug("Successfully verified " + this.channelId + "'s YouTube channel.");
		return null;
	}

	private String verifyError(InfluencersPlugin plugin, String error) {
		plugin.debug("Could not verify " + this.channelId + "'s YouTube channel: " + error + ".");
		return error;
	}

	@Override
	public String update(Object... parameters) {
		InfluencersPlugin plugin = (InfluencersPlugin) parameters[0];
		plugin.debug("Updating " + this.channelId + "'s YouTube channel...");
		if (!plugin.getConfiguration().isYouTubeEnabled()) {
			return this.updateError(plugin, SERVICE_DISABLED_ERROR);
		}
		String apiKey = plugin.getConfiguration().getYouTubeAPIKey();
		JSONObject channelInformation = Unirest.get(String.format(CHANNEL_INFO_URL, apiKey, this.channelId)).asJson().getBody().getObject();
		if (channelInformation.has("error")) {
			return this.updateError(plugin, CHANNEL_INFORMATION_RETRIEVE_ERROR);
		}
		String playlistId = channelInformation.getJSONArray("items").getJSONObject(0).getJSONObject("contentDetails").getJSONObject("relatedPlaylists").getString("uploads");
		JSONObject playlistInformation = Unirest.get(String.format(PLAYLIST_INFO_URL, apiKey, playlistId)).asJson().getBody().getObject();
		if (playlistInformation.has("error")) {
			return this.updateError(plugin, PLAYLIST_INFORMATION_RETRIEVE_ERROR);
		}
		JSONArray channelItems = channelInformation.getJSONArray("items");
		if (channelItems.isEmpty()) {
			return this.verifyError(plugin, CHANNEL_INFORMATION_RETRIEVE_ERROR);
		}
		JSONObject channelItem = channelItems.getJSONObject(0);
		JSONObject channelSnippet = channelItem.getJSONObject("snippet");
		String name = channelSnippet.getString("title");
		JSONObject channelStatistics = channelItem.getJSONObject("statistics");
		long subscribers = channelStatistics.getBoolean("hiddenSubscriberCount") ? -1 : channelStatistics.getLong("subscriberCount");
		long videos = channelStatistics.getLong("videoCount");
		long totalViews = channelStatistics.getLong("viewCount");
		JSONArray playlistItems = playlistInformation.getJSONArray("items");
		List<Video> lastVideos = new ArrayList<>(5);
		for (int index = 0; index < playlistItems.length(); index++) {
			JSONObject playlistItem = playlistItems.getJSONObject(index);
			JSONObject playlistItemSnippet = playlistItem.getJSONObject("snippet");
			String id = playlistItem.getString("id");
			String title = playlistItemSnippet.getString("title");
			String description = playlistItemSnippet.getString("description");
			Instant publishTime = Instant.parse(playlistItemSnippet.getString("publishedAt"));
			lastVideos.add(new Video(id, title, description, publishTime));
		}
		this.channel = new Channel(name, subscribers, videos, totalViews, lastVideos);
		YouTubeRank currentRank = null;
		for (YouTubeRank rank : plugin.getConfiguration().getYouTubeRanks()) {
			if (rank.meetRequirements(this.channel)) {
				currentRank = rank;
			}
		}
		this.currentRank = currentRank;
		plugin.debug("Successfully updated " + this.channelId + "'s YouTube channel.");
		return null;
	}

	private String updateError(InfluencersPlugin plugin, String error) {
		plugin.debug("Could not update " + this.channelId + "'s YouTube channel: " + error + ".");
		return error;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (object == null || this.getClass() != object.getClass()) {
			return false;
		}
		YouTubeService that = (YouTubeService) object;
		return this.channelVerified == that.channelVerified &&
			Objects.equals(this.channelId, that.channelId) &&
			Objects.equals(this.channelVerificationKey, that.channelVerificationKey) &&
			Objects.equals(this.channel, that.channel) &&
			Objects.equals(this.currentRank, that.currentRank);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.channelId, this.channelVerificationKey, this.channelVerified, this.channel, this.currentRank);
	}

	@Override
	public String toString() {
		return "YouTubeService{" +
			"channelId='" + this.channelId + "'" +
			", channelVerificationKey='" + this.channelVerificationKey + "'" +
			", channelVerified=" + this.channelVerified +
			", channel=" + this.channel +
			", currentRank=" + this.currentRank +
			"} " + super.toString();
	}

	public static class Channel {

		private String name;
		private long subscribers;
		private long videos;
		private long totalViews;
		private long averageViews;
		private List<Video> lastVideos;

		public Channel(String name, long subscribers, long videos, long totalViews, List<Video> lastVideos) {
			this.name = name;
			this.subscribers = subscribers;
			this.videos = videos;
			this.totalViews = totalViews;
			if (totalViews > 0 && videos > 0) {
				this.averageViews = totalViews / videos;
			} else {
				this.averageViews = 0;
			}
			this.lastVideos = lastVideos;
		}

		public String getName() {
			return this.name;
		}

		public long getSubscribers() {
			return this.subscribers;
		}

		public long getVideos() {
			return this.videos;
		}

		public long getTotalViews() {
			return this.totalViews;
		}

		public long getAverageViews() {
			return this.averageViews;
		}

		public List<Video> getLastVideos() {
			return Collections.unmodifiableList(this.lastVideos);
		}

		@Override
		public boolean equals(Object object) {
			if (this == object) {
				return true;
			}
			if (object == null || this.getClass() != object.getClass()) {
				return false;
			}
			Channel that = (Channel) object;
			return this.subscribers == that.subscribers &&
				this.videos == that.videos &&
				this.totalViews == that.totalViews &&
				this.averageViews == that.averageViews &&
				Objects.equals(this.name, that.name) &&
				Objects.equals(this.lastVideos, that.lastVideos);
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.name, this.subscribers, this.videos, this.totalViews, this.averageViews, this.lastVideos);
		}

		@Override
		public String toString() {
			return "Channel{" +
				"name='" + this.name + "'" +
				", subscribers=" + this.subscribers +
				", videos=" + this.videos +
				", totalViews=" + this.totalViews +
				", averageViews=" + this.averageViews +
				", lastVideos=" + this.lastVideos +
				"}";
		}

		public static class Video {

			private String id;
			private String title;
			private String description;
			private Instant publishTime;

			public Video(String id, String title, String description, Instant publishTime) {
				this.id = id;
				this.title = title;
				this.description = description;
				this.publishTime = publishTime;
			}

			public String getId() {
				return this.id;
			}

			public String getTitle() {
				return this.title;
			}

			public String getDescription() {
				return this.description;
			}

			public Instant getPublishTime() {
				return this.publishTime;
			}

			@Override
			public boolean equals(Object object) {
				if (this == object) {
					return true;
				}
				if (object == null || this.getClass() != object.getClass()) {
					return false;
				}
				Video that = (Video) object;
				return Objects.equals(this.id, that.id) &&
					Objects.equals(this.title, that.title) &&
					Objects.equals(this.description, that.description) &&
					Objects.equals(this.publishTime, that.publishTime);
			}

			@Override
			public int hashCode() {
				return Objects.hash(this.id, this.title, this.description, this.publishTime);
			}

			@Override
			public String toString() {
				return "Video{" +
					"id='" + this.id + "'" +
					", title='" + this.title + "'" +
					", description='" + this.description + "'" +
					", publishTime=" + this.publishTime +
					"}";
			}
		}
	}
}
