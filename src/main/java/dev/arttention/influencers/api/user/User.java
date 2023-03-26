package dev.arttention.influencers.api.user;

import dev.arttention.influencers.api.service.Service;
import dev.arttention.influencers.api.service.ServiceType;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class User {

	private UUID uniqueId;
	private String name;
	private Set<ServiceType> blacklistedServices;
	private Map<ServiceType, Service> services;
	private Instant createdAt;

	public User(UUID uniqueId, String name, Set<ServiceType> blacklistedServices, Map<ServiceType, Service> services, Instant createdAt) {
		this.uniqueId = uniqueId;
		this.name = name;
		this.blacklistedServices = blacklistedServices;
		this.services = services;
		this.createdAt = createdAt;
	}

	public UUID getUniqueId() {
		return this.uniqueId;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<ServiceType> getBlacklistedServices() {
		return Collections.unmodifiableSet(this.blacklistedServices);
	}

	public boolean hasServiceBlacklisted(ServiceType serviceType) {
		return this.blacklistedServices.contains(serviceType);
	}

	public boolean blacklistService(ServiceType serviceType) {
		if (this.blacklistedServices.contains(serviceType)) {
			return false;
		}
		this.blacklistedServices.add(serviceType);
		return true;
	}

	public boolean unblacklistService(ServiceType serviceType) {
		if (!this.blacklistedServices.contains(serviceType)) {
			return false;
		}
		this.blacklistedServices.remove(serviceType);
		return true;
	}

	public Map<ServiceType, Service> getServices() {
		return Collections.unmodifiableMap(this.services);
	}

	public boolean hasServiceLinked(ServiceType serviceType) {
		return this.services.containsKey(serviceType);
	}

	public Service getService(ServiceType serviceType) {
		return this.services.get(serviceType);
	}

	public boolean linkService(Service service) {
		if (this.services.containsKey(service.getType())) {
			return false;
		}
		this.services.put(service.getType(), service);
		return true;
	}

	public boolean unlinkService(Service service) {
		return this.unlinkService(service.getType());
	}

	public boolean unlinkService(ServiceType serviceType) {
		if (!this.services.containsKey(serviceType)) {
			return false;
		}
		this.services.remove(serviceType);
		return true;
	}

	public Instant getCreatedAt() {
		return this.createdAt;
	}

	public void setCreatedAt(Instant time) {
		this.createdAt = time;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (object == null || this.getClass() != object.getClass()) {
			return false;
		}
		User that = (User) object;
		return Objects.equals(this.uniqueId, that.uniqueId) &&
			Objects.equals(this.name, that.name) &&
			Objects.equals(this.blacklistedServices, that.blacklistedServices) &&
			Objects.equals(this.services, that.services) &&
			Objects.equals(this.createdAt, that.createdAt);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.uniqueId, this.name, this.blacklistedServices, this.services, this.createdAt);
	}

	@Override
	public String toString() {
		return "User{" +
			"uniqueId=" + this.uniqueId +
			", name='" + this.name + "'" +
			", blacklistedServices=" + this.blacklistedServices +
			", services=" + this.services +
			", createdAt=" + this.createdAt +
			"}";
	}
}
