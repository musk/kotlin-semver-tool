import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    `maven-publish`
    `java-library`
    signing
}

group = parent?.group ?: "io.github.musk.semver"
version = parent?.version ?: "0.0.0-SNAPSHOT"
project.extra["signRelease"] = (project.property("signRelease") as String?)?.toBoolean() ?: false

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
        create<MavenPublication>("kotlin-semver-library") {
            pom {
                name.set("Kotlin Semver Library")
                description.set("""Toolset to handle semantic versioning according to "Semver Specification 2.0"(https://semver.org/spec/v2.0.0.html)""")
                url.set("https://github.com/musk/kotlin-semver-tool")
                packaging="jar"
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
                        organizationUrl.set("http://github.com/musk")
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

signing {
    sign(publishing.publications["kotlin-semver-library"])
}

tasks.withType<Sign>().configureEach {
    onlyIf { project.extra["signRelease"] as Boolean }
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

