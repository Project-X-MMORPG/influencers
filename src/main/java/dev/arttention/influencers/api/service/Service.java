package dev.arttention.influencers.api.service;

import java.time.Instant;

public abstract class Service {

	private Instant lastCacheTime;

	public Service(Instant lastCacheTime) {
		this.lastCacheTime = lastCacheTime;
	}

	public abstract ServiceType getType();

	public String getName() {
		return this.getType().getName();
	}

	public Instant getLastCacheTime() {
		return this.lastCacheTime;
	}

	public void setLastCacheTime(Instant time) {
		this.lastCacheTime = time;
	}

	public void setLastCacheTime(long time) {
		this.lastCacheTime = Instant.ofEpochMilli(time);
	}

	public void updateLastCacheTime() {
		this.lastCacheTime = Instant.now();
	}

	public abstract String verify(Object... parameters);

	public abstract String update(Object... parameters);

	@Override
	public String toString() {
		return "Service{" + "lastCacheTime=" + this.lastCacheTime + "}";
	}
}
