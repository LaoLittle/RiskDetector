plugins {
    val kotlinVersion = "1.5.31"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("net.mamoe.mirai-console") version "2.10.0-RC2"
}

group = "org.laolittle.plugin"
version = "1.0.1"

repositories {
    maven("https://maven.aliyun.com/repository/central")
    mavenCentral()
}
