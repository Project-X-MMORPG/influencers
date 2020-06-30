package dev.jaqobb.influencers.api;

import dev.jaqobb.influencers.api.basic.Configuration;
import dev.jaqobb.influencers.api.basic.Messages;
import dev.jaqobb.influencers.api.user.UserRepository;

public interface InfluencersPlugin {

	Configuration getConfiguration();

	Messages getMessages();

	UserRepository getUserRepository();

	void debug(String message);

	void debug(String message, Exception exception);
}
