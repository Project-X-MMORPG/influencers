package dev.arttention.influencers.api.rank.type;

import dev.arttention.influencers.api.service.list.YouTubeService;
import dev.arttention.influencers.api.util.common.StringHelper;
import dev.arttention.influencers.api.rank.Rank;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public class YouTubeRank extends Rank {

	private long subscribers;
	private long totalViews;
	private long averageViews;
	private Instant publishTime;
	private List<String> titleContains;
	private List<String> descriptionContains;
	private List<String> commands;

	public YouTubeRank(String id, String name, long subscribers, long totalViews, long averageViews, Instant publishTime, List<String> titleContains, List<String> descriptionContains, List<String> commands) {
		super(id, name);
		this.subscribers = subscribers;
		this.totalViews = totalViews;
		this.averageViews = averageViews;
		this.publishTime = publishTime;
		this.titleContains = titleContains;
		this.descriptionContains = descriptionContains;
		this.commands = commands;
	}

	public long getSubscribers() {
		return this.subscribers;
	}

	public void setSubscribers(long subscribers) {
		this.subscribers = subscribers;
	}

	public long getTotalViews() {
		return this.totalViews;
	}

	public void setTotalViews(long views) {
		this.totalViews = views;
	}

	public long getAverageViews() {
		return this.averageViews;
	}

	public void setAverageViews(long views) {
		this.averageViews = views;
	}

	public Instant getPublishTime() {
		return this.publishTime;
	}

	public void setPublishTime(Instant time) {
		this.publishTime = time;
	}

	public List<String> getTitleContains() {
		return this.titleContains;
	}

	public void setTitleContains(List<String> contains) {
		this.titleContains = contains;
	}

	public List<String> getDescriptionContains() {
		return this.descriptionContains;
	}

	public void setDescriptionContains(List<String> contains) {
		this.descriptionContains = contains;
	}

	public List<String> getCommands() {
		return this.commands;
	}

	public void setCommands(List<String> commands) {
		this.commands = commands;
	}

	public boolean meetRequirements(YouTubeService.Channel channel) {
		if (this.subscribers > 0 && this.subscribers > channel.getSubscribers()) {
			return false;
		}
		if (this.totalViews > 0 && this.totalViews > channel.getTotalViews()) {
			return false;
		}
		if (this.averageViews > 0 && this.averageViews > channel.getAverageViews()) {
			return false;
		}
		if (this.publishTime.toEpochMilli() <= 0 && this.titleContains.isEmpty() && this.descriptionContains.isEmpty()) {
			return true;
		}
		Instant now = Instant.now();
		for (YouTubeService.Channel.Video lastVideo : channel.getLastVideos()) {
			if (this.publishTime.toEpochMilli() > 0 && this.publishTime.plusMillis(lastVideo.getPublishTime().toEpochMilli()).compareTo(now) < 0) {
				continue;
			}
			if (!this.titleContains.isEmpty() && !StringHelper.containsIgnoreCase(lastVideo.getTitle(), this.titleContains)) {
				continue;
			}
			if (!this.descriptionContains.isEmpty() && !StringHelper.containsIgnoreCase(lastVideo.getDescription(), this.descriptionContains)) {
				continue;
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (object == null || this.getClass() != object.getClass()) {
			return false;
		}
		YouTubeRank that = (YouTubeRank) object;
		return this.subscribers == that.subscribers &&
			this.totalViews == that.totalViews &&
			this.averageViews == that.averageViews &&
			Objects.equals(this.publishTime, that.publishTime) &&
			Objects.equals(this.titleContains, that.titleContains) &&
			Objects.equals(this.descriptionContains, that.descriptionContains) &&
			Objects.equals(this.commands, that.commands);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.subscribers, this.totalViews, this.averageViews, this.publishTime, this.titleContains, this.descriptionContains, this.commands);
	}

	@Override
	public String toString() {
		return "YouTubeRank{" +
			"subscribers=" + this.subscribers +
			", totalViews=" + this.totalViews +
			", averageViews=" + this.averageViews +
			", publishTime=" + this.publishTime +
			", titleContains=" + this.titleContains +
			", descriptionContains=" + this.descriptionContains +
			", commands=" + this.commands +
			"} " + super.toString();
	}
}
