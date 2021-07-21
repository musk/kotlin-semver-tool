import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.20"
    `maven-publish`
    `java-library`
}

group = "com.github.musk.semver"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.0")
    testImplementation(kotlin("test"))
}

publishing {
    publications {
        create<MavenPublication>("kotlin-semver-library") {
            pom {
                name.set("Kotlin Semver Library")
                description.set("""Toolset to handle semantic versioning according to "Semver Specification 2.0"(https://semver.org/spec/v2.0.0.html)""")
                url.set("https://github.com/musk/kotlin-semver-tool")
                licenses {
                    license {
                        name.set("The MIT License")
                        url.set("https://github.com/musk/kotlin-semver-tool/blob/main/LICENSE")
                    }
                }
                developers {
                    developer {
                        id.set("musk")
                        name.set("Stefan Langer")
                        email.set("mailtolanger@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/musk/kotlin-semver-tool.git")
                    developerConnection.set("scm:git:ssh://github.com:musk/kotlin-semver-tool.git")
                    url.set("https://github.com/musk/kotlin-semver-tool")
                }
            }
            from(components["java"])
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
}

tasks.jar {
    manifest {
        attributes(mapOf("Implementation-Title" to project.name,
            "Implementation-Version" to project.version,
            "Built-By" to System.getProperty("user.name")))
    }
}

java {
    withSourcesJar()
    withJavadocJar()
}

