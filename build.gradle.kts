plugins {
    kotlin("jvm") version "1.5.21"
    `maven-publish`
    `java-library`
    signing
}

group = "io.github.musk.semver"
version = project.property("projectVersion") ?: "0.0.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
}