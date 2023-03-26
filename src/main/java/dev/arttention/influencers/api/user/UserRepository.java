package dev.arttention.influencers.api.user;

import dev.arttention.influencers.api.service.ServiceType;
import dev.arttention.influencers.api.InfluencersPlugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class UserRepository {

	private final InfluencersPlugin plugin;
	private Map<UUID, User> cache = new HashMap<>(100, 0.85F);

	public UserRepository(InfluencersPlugin plugin) {
		this.plugin = plugin;
	}

	public abstract void loadAll();

	public abstract boolean save(User user);

	public abstract void updateService(User user, ServiceType serviceType);

	public void saveAll() {
		this.plugin.debug("Saving all users...");
		Collection<UUID> usersToRemove = new ArrayList<>(this.cache.size());
		for (User user : this.cache.values()) {
			if (!this.save(user)) {
				usersToRemove.add(user.getUniqueId());
			}
		}
		for (UUID user : usersToRemove) {
			this.cache.remove(user);
		}
	}

	public Collection<User> getAll() {
		return Collections.unmodifiableCollection(this.cache.values());
	}

	public User get(UUID uniqueId) {
		return this.cache.get(uniqueId);
	}

	public User get(String name) {
		return this.cache.values().stream()
			.filter(element -> element.getName().equalsIgnoreCase(name))
			.findFirst()
			.orElse(null);
	}

	public void add(User user) {
		this.cache.put(user.getUniqueId(), user);
	}

	public void remove(User user) {
		this.cache.remove(user.getUniqueId());
	}

	public void remove(UUID uniqueId) {
		this.cache.remove(uniqueId);
	}
}
