package io.github.musk.semver.demo.kotlin

import io.github.musk.semver.Bump
import io.github.musk.semver.Semver
import io.github.musk.semver.Semver.Companion.toSemver

/**
 * Demonstrate the usage of [Semver].
 * HandleRelease provides two functionalities
 * 1. Create a release from a snapshot
 * 2. Create the next snapshot from a release
 */
object HandleRelease {
    const val SNAPSHOT = "SNAPSHOT"

    /**
     * Release the version given by this [String] and turn it into a [Semver]
     *
     * Throws an [IllegalArgumentException] when the [String] does not designate
     * a semantic or is not a snapshot version.
     */
    fun String.release(): Semver {
        val semver = this.toSemver()
        require(semver.prerel.endsWith(SNAPSHOT)) { "Version '$this' is not a SNAPSHOT version! Unable to release" }
        return semver.release()
    }

    /**
     * Create a snapshot version from the given [String] by bumping its specified part
     * and appending _-SNAPSHOT_ to it. If the version already dedicates a SNAPSHOT version
     * it returns the version unchanged.
     *
     * Throws an [IllegalArgumentException] when the [String] does not designate a semantic version.
     */
    fun String.nextSnapshot(bump: Bump): Semver {
        val semver = this.toSemver()
        return if (semver.prerel.endsWith(SNAPSHOT)) semver else semver.bump(bump).prerel(SNAPSHOT)
    }

    /**
     * Simple main to demonstrate the usage of [HandleRelease]
     */
    @JvmStatic
    fun main(args: Array<String>) {
        val releaseVersion: Semver = "1.2.3-SNAPSHOT".release()
        val nextSnapshot: Semver = "1.2.3".nextSnapshot(Bump.MINOR)

        println("Release version: $releaseVersion")
        println("Next snapshot: $nextSnapshot")

        try {
            "1.2.3".release()
        } catch (ex: IllegalArgumentException) {
            println(ex.message)
        }
    }
}


