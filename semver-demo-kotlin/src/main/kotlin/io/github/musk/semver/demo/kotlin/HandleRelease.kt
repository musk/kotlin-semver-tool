package io.github.musk.semver.demo.kotlin

import io.github.musk.semver.Bump
import io.github.musk.semver.Semver
import io.github.musk.semver.Semver.Companion.toSemver

object HandleRelease {
    const val SNAPSHOT = "SNAPSHOT"

    fun String.release(): Semver {
        val semver = this.toSemver()
        require(semver.prerel.endsWith(SNAPSHOT)) { "Version '$this' is not a SNAPSHOT version! Unable to release" }
        return semver.release()
    }

    fun String.nextSnapshot(bump: Bump): Semver {
        val semver = this.toSemver()
        return if (semver.prerel.endsWith(SNAPSHOT)) semver else semver.bump(bump).prerel(SNAPSHOT)
    }

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


