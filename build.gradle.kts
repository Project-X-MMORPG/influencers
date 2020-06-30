plugins {
	`java-library`
	id("com.github.johnrengelman.shadow") version "5.1.0"
	id("net.minecrell.plugin-yml.bungee") version "0.3.0"
	id("net.minecrell.plugin-yml.bukkit") version "0.3.0"
}

plugins.apply("java-library")
plugins.apply("com.github.johnrengelman.shadow")

group = "dev.jaqobb"
version = "1.0.6"
description = "Minecraft plugin that allows players to link their YouTube accounts and receive rewards based on their influence"

java {
	sourceCompatibility = JavaVersion.VERSION_1_8
	targetCompatibility = JavaVersion.VERSION_1_8
}

defaultTasks("clean", "build", "shadowJar")

tasks {
	shadowJar {
		relocate("kong.unirest", "dev.jaqobb.influencers.api.library.kong.unirest")
		relocate("org.apache.commons.codec", "dev.jaqobb.influencers.api.library.org.apache.commons.codec")
		relocate("org.apache.commons.logging", "dev.jaqobb.influencers.api.library.org.apache.commons.logging")
		relocate("org.apache.http", "dev.jaqobb.influencers.api.library.org.apache.http")
		relocate("org.json", "dev.jaqobb.influencers.api.library.org.json")
	}
	bungee {
		name = project.name
		main = "${project.group}.influencers.bungee.InfluencersBungeePlugin"
		version = project.version as String
		description = project.description
		author = "jaqobb"
	}
	bukkit {
		name = project.name
		main = "${project.group}.influencers.spigot.InfluencersSpigotPlugin"
		version = project.version as String
		description = project.description
		author = "jaqobb"
		website = "https://jaqobb.dev"
		commands {
			create("influencers") {
				description = "Shows help and basic information regarding Influencers plugin"
			}
			create("youtube") {
				description = "Allows to view, link, unlink or verify a YouTube channel"
				aliases = listOf("yt")
			}
		}
	}
}

repositories {
	jcenter()
	maven("https://oss.sonatype.org/content/repositories/snapshots/")
	maven("https://repo.codemc.org/repository/maven-public/")
	maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
}

dependencies {
	implementation("com.konghq:unirest-java:2.3.11")
	compileOnly("net.md-5:bungeecord-api:1.14-SNAPSHOT")
	compileOnly("org.spigotmc:spigot-api:1.14.4-R0.1-SNAPSHOT")
}
