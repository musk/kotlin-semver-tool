import io.github.musk.semver.Semver
import io.github.musk.semver.Semver.Companion.toSemver
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    `maven-publish`
    `java-library`
    signing
}

group = parent?.group ?: "io.github.musk.semver"
version = parent?.version ?: "0.0.0-SNAPSHOT"
project.extra["release.sign"] = (project.property("release.sign") as String?)?.toBoolean() ?: false


buildscript {
    repositories {
        mavenLocal()
    }
    dependencies {
        "classpath"("io.github.musk.semver:semver-library:1.0.0-SNAPSHOT")
    }
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.0")
    testImplementation(kotlin("test"))
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("semver-library") {
            pom {
                name.set("Semver Library")
                description.set("""Toolset to handle semantic versioning according to "Semver Specification 2.0"(https://semver.org/spec/v2.0.0.html)""")
                url.set("https://github.com/musk/semver-tool")
                packaging = "jar"
                licenses {
                    license {
                        name.set("The MIT License")
                        url.set("https://github.com/musk/semver-tool/blob/main/LICENSE")
                    }
                }
                developers {
                    developer {
                        id.set("musk")
                        name.set("Stefan Langer")
                        email.set("mailtolanger@gmail.com")
                        organizationUrl.set("http://github.com/musk")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/musk/semver-tool.git")
                    developerConnection.set("scm:git:ssh://github.com:musk/semver-tool.git")
                    url.set("https://github.com/musk/semver-tool")
                }
            }
            from(components["java"])
        }
    }
}

signing {
    sign(publishing.publications["semver-library"])
}

tasks.register("release") {
    dependsOn(
        "build",
        // TODO change this to publish once everything works
        "publishToMavenLocal",
        "signSemver-libraryPublication",
    )
    val versionProperty = "projectVersion"

    fun writeVersionToPropertiesFile(version: Semver, gradleProperties: File) {
        var doesNotContainVersion = true
        val outLines = gradleProperties.readLines().map {
            if (it.startsWith(versionProperty)) {
                doesNotContainVersion = false
                "$versionProperty=$version"
            } else it
        }.toMutableList()
        if (doesNotContainVersion)
            outLines += "$versionProperty=$version"

        logger.debug("Saving changed properties ${gradleProperties.absolutePath}")
        gradleProperties.writeText(outLines.joinToString("\n"))
    }

    fun createGitTag(version: Semver) {
        val tagName = "r$version"
        val signTag = (project.property("release.signedTag") as String).toBoolean()
        val signingKeyId = project.property("signing.keyId").toString()
        val signingUser = project.property("signing.user").toString()
        val signingPwd = project.property("signing.password").toString()
        logger.info("Creating tag {}! (signed with key: $signingKeyId)", tagName)
        Git.open(project.rootDir).tag()
            .setName(tagName)
            .setAnnotated(true)
            .setMessage("[Release] Version: $version").run {
                if (signTag) {
                    val provider = UsernamePasswordCredentialsProvider(signingUser, signingPwd)
                    this.setSigningKey(signingKeyId)
                        .setSigned(true)
                        .setCredentialsProvider(provider)
                } else this
            }.call()
    }

    project.extra["release.sign"] = true
    val releasedVersion = version.toString().toSemver().release()
    version = releasedVersion
    logger.debug("Setting signRelease to 'true' and version to '$version'")
    doFirst {
        logger.info("Releasing $version")
        createGitTag(releasedVersion)
    }
    doLast {
        extra["release.sign"] = false
        val nextSnapshot = version.toString().toSemver().minor().prerel("SNAPSHOT")
        version = nextSnapshot
        logger.info("Setting next snapshot version to '$version'")
        writeVersionToPropertiesFile(nextSnapshot, file("${project.rootDir}/gradle.properties"))
    }
}

tasks.withType<Sign>().configureEach {
    onlyIf { project.extra["release.sign"] as Boolean }
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
}

tasks.jar {
    manifest {
        attributes(mapOf(
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version,
            "Built-By" to System.getProperty("user.name"),
            "Multi-Release" to true,
        ))
    }
}

