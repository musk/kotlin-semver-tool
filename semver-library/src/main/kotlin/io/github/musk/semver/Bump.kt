package io.github.musk.semver

/**
 * Defines the part to change in a semantic version
 *
 * See [Semver.bump]
 */
enum class Bump {
    MAJOR, MINOR, PATCH, RELEASE
}