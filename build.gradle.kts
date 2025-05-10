plugins {
    kotlin("jvm") version "2.1.20"
    application
    id("com.github.johnrengelman.shadow") version "8.1.1" // For fat jar
}

group = "io.acking"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5")
}

application {
    mainClass.set("MqttWorkerKt") // Replace with your real main class name
}

tasks {
    // Generates a fat (self-contained) jar
    named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
        archiveBaseName.set("mqtt-worker")
        archiveClassifier.set("")
        archiveVersion.set("")
        manifest {
            attributes["Main-Class"] = "io.acking.tools.mqtt.loadtester.MqttWorkerKt"
        }
    }

    build {
        dependsOn(shadowJar)
    }
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(22)
}