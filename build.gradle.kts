import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    id("com.github.johnrengelman.shadow") version "6.0.0"
}

repositories {
    mavenCentral()
    jcenter()
    maven {
        // PaperMC
        url = uri("https://papermc.io/repo/repository/maven-public/")
    }
    maven {
        // Dmulloy2 - ProtocolLib
        url = uri("https://repo.dmulloy2.net/nexus/repository/public/")
    }
}

dependencies {
    implementation("commons-io:commons-io:2.6")
    implementation("org.apache.commons:commons-lang3:3.9")
    implementation("org.influxdb:influxdb-java:2.17")
    compileOnly("com.destroystokyo.paper:paper-api:1.16.1-R0.1-SNAPSHOT")
    compileOnly("com.comphenix.protocol:ProtocolLib:4.6.0-SNAPSHOT")
    compileOnly("org.jetbrains:annotations:18.0.0")
    compileOnly("org.projectlombok:lombok:1.18.12")
    annotationProcessor("org.projectlombok:lombok:1.18.12")
    testCompileOnly("org.projectlombok:lombok:1.18.12")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.12")
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveBaseName.set("smp")
        archiveClassifier.set("")
        archiveVersion.set("")
    }
}
