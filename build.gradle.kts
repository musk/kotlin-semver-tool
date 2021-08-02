
plugins {
    kotlin("jvm") version "1.5.21"
    `maven-publish`
    `java-library`
    signing
}

group = "io.github.musk.semver"
version = project.property("projectVersion") ?: "0.0.0-SNAPSHOT"
