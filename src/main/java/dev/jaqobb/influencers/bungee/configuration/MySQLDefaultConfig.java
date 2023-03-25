package dev.jaqobb.influencers.bungee.configuration;

import dev.arttention.libraries.api.configuration.ConfigurationData;
import dev.arttention.libraries.api.configuration.DefaultConfiguration;
import lombok.Getter;

@Getter
@ConfigurationData(filePath = "plugins/BungeeCore", fileName = "mysql.yml")
public class MySQLDefaultConfig implements DefaultConfiguration {

    private String url;
    private String user;
    private String password;

    @Override
    public void setDefaultValues() {
        this.url = "jdbc:";
        this.user = "name";
        this.password = "password";
    }
}
