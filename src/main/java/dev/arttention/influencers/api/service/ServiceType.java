package dev.arttention.influencers.api.service;

import dev.arttention.influencers.api.util.map.NameMap;

public enum ServiceType {

	YOUTUBE("youtube", "YouTube"),
	TWITCH("twitch", "Twitch");

	public static final NameMap<ServiceType> LIST = new NameMap<>(values(), constant -> constant.id);

	private String id;
	private String name;
	private boolean allowsUploadingVideos;
	private boolean allowsStreaming;

	ServiceType(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public String getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}
}
