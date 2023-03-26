package dev.arttention.influencers.api.service.list;

import dev.arttention.influencers.api.InfluencersPlugin;
import dev.arttention.influencers.api.service.Service;
import dev.arttention.influencers.api.service.ServiceType;
import java.time.Instant;

public class TwitchService extends Service {

	public static final String SERVICE_DISABLED_ERROR = "Service is disabled";

	public TwitchService() {
		super(Instant.ofEpochMilli(0));
	}

	@Override
	public ServiceType getType() {
		return ServiceType.TWITCH;
	}

	@Override
	public String verify(Object... parameters) {
		InfluencersPlugin plugin = (InfluencersPlugin) parameters[0];
		if (!plugin.getConfiguration().isTwitchEnabled()) {
			return SERVICE_DISABLED_ERROR;
		}
		return null;
	}

	@Override
	public String update(Object... parameters) {
		InfluencersPlugin plugin = (InfluencersPlugin) parameters[0];
		if (!plugin.getConfiguration().isTwitchEnabled()) {
			return SERVICE_DISABLED_ERROR;
		}
		return null;
	}
}
