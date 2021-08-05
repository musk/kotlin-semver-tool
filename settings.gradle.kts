
rootProject.name = "semver-tool"

include(":semver-library")
include(":semver-demo-java")
include(":semver-demo-kotlin")

dependencyResolutionManagement {
    repositories {
        mavenLocal()
        mavenCentral()
    }
}
