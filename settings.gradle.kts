
rootProject.name = "kotlin-semver-tools"

include(":kotlin-semver-tool")
include(":semver-demo-java")
include(":semver-demo-kotlin")

dependencyResolutionManagement {
    repositories {
        mavenLocal()
        mavenCentral()
    }
}
