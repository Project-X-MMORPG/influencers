package dev.arttention.influencers.api;

import dev.arttention.influencers.api.basic.Configuration;
import dev.arttention.influencers.api.basic.Messages;
import dev.arttention.influencers.api.user.UserRepository;

public interface InfluencersPlugin {

	Configuration getConfiguration();

	Messages getMessages();

	UserRepository getUserRepository();

	void debug(String message);

	void debug(String message, Exception exception);
}
