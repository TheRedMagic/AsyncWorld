import com.undefinedcreations.nova.ServerType

plugins {
    kotlin("jvm") version "2.2.20"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.19"
    id("com.undefinedcreations.nova") version "0.0.8"
    id("com.gradleup.shadow") version "8.3.5"
}

group = "gg.undefinedaquatic"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    paperweight.paperDevBundle("1.21.9-R0.1-SNAPSHOT")
}

java {
    sourceCompatibility = JavaVersion.VERSION_24
    targetCompatibility = JavaVersion.VERSION_24
}
kotlin {
    jvmToolchain(24)
}


tasks {
    jar {
        dependsOn(shadowJar)
    }
    runServer {
        serverType(ServerType.PAPERMC)
        minecraftVersion("1.21.9")
        acceptMojangEula()
    }
}