package dev.arttention.influencers.api.basic;

import dev.arttention.influencers.api.rank.type.TwitchRank;
import dev.arttention.influencers.api.rank.type.YouTubeRank;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

public interface Configuration {

	boolean isDebugModeEnabled();

	void setDebugModeEnabled(boolean enabled);

	boolean areMetricsEnabled();

	void setMetricsEnabled(boolean enabled);

	ZoneOffset getTimeZone();

	void setTimeZone(ZoneOffset zone);

	String getTimeFormat();

	void setTimeFormat(String format);

	boolean shouldSaveUsersOnPluginDisable();

	void setShouldSaveUsersOnPluginDisable(boolean should);

	long getUserSafeToSaveDelay();

	void setUserSaveToSaveDelay(long delay);

	long getUsersUpdateDelay();

	void setUsersUpdateDelay(long delay);

	long getUsersUpdatePeriod();

	void setUsersUpdatePeriod(long period);

	boolean isYouTubeEnabled();

	void setYouTubeEnabled(boolean enabled);

	String getYouTubeAPIKey();

	void setYouTubeAPIKey(String apiKey);

	Instant getYouTubeCacheTime();

	void setYouTubeCacheTime(Instant time);

	List<YouTubeRank> getYouTubeRanks();

	void setYouTubeRanks(List<YouTubeRank> ranks);

	default YouTubeRank getYouTubeRank(String id) {
		return this.getYouTubeRanks().stream()
			.filter(youtubeRank -> youtubeRank.getId().equalsIgnoreCase(id))
			.findFirst()
			.orElse(null);
	}

	boolean isTwitchEnabled();

	void setTwitchEnabled(boolean enabled);

	String getTwitchAPIKey();

	void setTwitchAPIKey(String apiKey);

	Instant getTwitchCacheTime();

	void setTwitchCacheTime(Instant time);

	List<TwitchRank> getTwitchRanks();

	void setTwitchRanks(List<TwitchRank> ranks);

	default TwitchRank getTwitchRank(String id) {
		return this.getTwitchRanks().stream()
			.filter(twitchRank -> twitchRank.getId().equalsIgnoreCase(id))
			.findFirst()
			.orElse(null);
	}

	void load();

	void save();

	void reload();
}
