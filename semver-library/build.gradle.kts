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
        mavenCentral()
    }
    dependencies {
        "classpath"("io.github.musk.semver:semver-library:1.1.0")
    }
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.0")
    testImplementation(kotlin("test"))
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }

    withSourcesJar()
    withJavadocJar()
}


publishing {
    publications {
        create<MavenPublication>("semver-library") {
            pom {
                name.set("Semver Library")
                description.set("""Toolset to handle semantic versioning according to 
                    |"Semver Specification 2.0"(https://semver.org/spec/v2.0.0.html)""".trimMargin())
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

tasks.withType<Sign>().configureEach {
    onlyIf { project.property("release.sign").toString().toBoolean() }
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

val prepareRelease by tasks.register("prepareRelease") {
    doFirst {
        val releasedVersion = version.toString().toSemver().release()
        version = releasedVersion
        println("setting release version '$releasedVersion'")
    }
}

tasks.build {
    mustRunAfter(prepareRelease)
    doFirst {
        println("building")
    }
}

val commitRelease by tasks.register("commitReleaseVersion") {
    dependsOn(
        prepareRelease,
    )
    doFirst {
        val releasedVersion = version.toString().toSemver().release()
        println("Setting signRelease to 'true' and version to '$releasedVersion'")
        val propertiesFile = file("${project.rootDir}/gradle.properties")
        writeVersionToPropertiesFile(releasedVersion, propertiesFile)
        commit("[Release] Version $releasedVersion", propertiesFile)
    }
}

val createTag by tasks.register("createTag") {
    mustRunAfter(commitRelease)
    doLast {
        val semver = version.toString().toSemver()
        println("Creating tag for '$semver'")
        createGitTag(semver)
    }
}

val commitSnapshot by tasks.register("commitNextSnapshot") {
    mustRunAfter(
        commitRelease,
        tasks.publish,
        "signSemver-libraryPublication",
        createTag,
    )
    doLast {
        val nextSnapshot = version.toString().toSemver().minor().prerel("SNAPSHOT")
        version = nextSnapshot
        println("Setting next snapshot version to '$version'")
        val propertiesFile = file("${project.rootDir}/gradle.properties")
        writeVersionToPropertiesFile(nextSnapshot, propertiesFile)
        commit("Setting version to $nextSnapshot", propertiesFile)
    }
}

tasks.register("release") {
    dependsOn(
        commitRelease,
        tasks.build,
        tasks.publish,
        "signSemver-libraryPublication",
        createTag,
        commitSnapshot,
    )
    doFirst {
        println("Releasing ${project.group}:${project.name}:${project.version}")
    }
}


fun writeVersionToPropertiesFile(
    version: Semver,
    gradleProperties: File,
    versionProperty: String = "projectVersion",
) {
    var doesNotContainVersion = true
    val outLines = gradleProperties.readLines().map {
        if (it.startsWith(versionProperty)) {
            doesNotContainVersion = false
            "$versionProperty=$version"
        } else it
    }.toMutableList()
    if (doesNotContainVersion)
        outLines += "$versionProperty=$version"

    println("Saving changed properties ${gradleProperties.absolutePath}")
    gradleProperties.writeText(outLines.joinToString("\n"))
}


fun commit(msg: String, file: File) {
    val rootDir: File = project.rootDir
    val git = Git.open(rootDir)
    val filePath = rootDir.absoluteFile.toPath().relativize(file.absoluteFile.toPath())
    println("committing '${filePath}' with message '$msg'")
    git.add().addFilepattern(filePath.toString()).setUpdate(true).call()
    git.commit().setMessage(msg).call()
}

fun createGitTag(version: Semver) {
    val tagName = "r$version"
    val signTag = project.property("release.signedTag").toString().toBoolean()
    val signingKeyId = project.property("signing.keyId").toString()
    val signingUser = project.property("signing.user").toString()
    val signingPwd = project.property("signing.password").toString()
    val message = when (signTag) {
        true -> "Creating signed tag '$tagName'! (keyId=$signingKeyId)"
        false -> "Creating tag '$tagName'!"
    }
    println(message)
    Git.open(project.rootDir).tag()
        .setName(tagName)
        .setAnnotated(true)
        .setMessage(message).run {
            if (signTag) {
                val provider = UsernamePasswordCredentialsProvider(signingUser, signingPwd)
                this.setSigningKey(signingKeyId)
                    .setSigned(true)
                    .setCredentialsProvider(provider)
            } else this
        }.call()
}
