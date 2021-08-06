plugins {
    kotlin("jvm") version "1.5.21"
    `maven-publish`
    `java-library`
    signing
}

group = "io.github.musk.semver"
version = project.property("projectVersion") ?: "0.0.0-SNAPSHOT"


buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
    }
    dependencies {
        classpath("io.github.musk.semver:semver-library:1.0.0-SNAPSHOT")
        classpath("org.eclipse.jgit:org.eclipse.jgit.ssh.jsch:5.12.0.202106070339-r")
        classpath("org.eclipse.jgit:org.eclipse.jgit.gpg.bc:5.12.0.202106070339-r")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
}

