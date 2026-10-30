plugins {
    java
    id("com.gradleup.shadow") version "8.3.6"
}

group = "com.worldplugins"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/groups/public/")
    maven("https://jitpack.io")
    maven("https://libraries.minecraft.net")
    maven("https://repo.rosewooddev.io/repository/public/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.8.8-R0.1-SNAPSHOT") {
        exclude(group = "net.md-5", module = "bungeecord-chat")
    }
    compileOnly("net.md-5:bungeecord-chat:1.16-R0.4")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7") {
        exclude(group = "org.bukkit", module = "bukkit")
    }
    compileOnly("com.mojang:authlib:1.5.21")
    compileOnly("org.black_ixx:playerpoints:3.2.7")
    compileOnly(fileTree("libs") { include("*.jar") })
    implementation("commons-io:commons-io:2.11.0")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks {
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(8)
    }

    processResources {
        val props = mapOf("version" to project.version.toString())
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    jar {
        enabled = false
    }

    shadowJar {
        archiveBaseName.set("WorldBlaze")
        archiveVersion.set("")
        archiveClassifier.set("")
        mergeServiceFiles()
    }

    build {
        dependsOn(shadowJar)
    }

    // ./gradlew deploy  (override with -PpluginsDir=...)
    register<Copy>("deploy") {
        dependsOn(shadowJar)
        from(shadowJar.flatMap { it.archiveFile })
        into(providers.gradleProperty("pluginsDir").orElse("C:/projetos/mine/SRV/plugins"))
    }
}
