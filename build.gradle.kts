plugins {
    kotlin("jvm") version "1.5.21"
    `maven-publish`
    `java-library`
    signing
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
}

group = "io.github.musk.semver"
version = if (hasProperty("projectVersion")) project.property("projectVersion")!! else "0.0.0-SNAPSHOT"


buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.eclipse.jgit:org.eclipse.jgit.ssh.jsch:5.12.0.202106070339-r")
        classpath("org.eclipse.jgit:org.eclipse.jgit.gpg.bc:5.12.0.202106070339-r")
    }
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            username.set(project.property("ossrh.user").toString())
            password.set(project.property("ossrh.password").toString())
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}
