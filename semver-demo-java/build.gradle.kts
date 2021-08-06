plugins {
    `maven-publish`
    java
}

group = "${parent?.group ?: "io.github.musk.semver"}.demo"
version = parent?.version ?: "0.0.0-SNAPSHOT"

dependencies {
    implementation(project(":semver-library"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.0")
}

java {
    withSourcesJar()
    withJavadocJar()
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.test {
    useJUnitPlatform()
}
