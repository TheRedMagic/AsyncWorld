import com.undefinedcreations.nova.ServerType

plugins {
    kotlin("jvm") version "2.1.20"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.19"
    id("com.undefinedcreations.nova") version "0.0.8"
}

group = "gg.undefinedaquatic"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    paperweight.paperDevBundle("1.21.9-R0.1-SNAPSHOT")
}

tasks {
    runServer {
        serverType(ServerType.PAPERMC)
        minecraftVersion("1.21.9")
        acceptMojangEula()
    }
}