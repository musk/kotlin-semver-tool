package io.github.musk.semver.demo.java;

import org.jetbrains.annotations.NotNull;

import io.github.musk.semver.Bump;
import io.github.musk.semver.Semver;

/**
 * Demonstrate the usage of {@link Semver} in a Java context.
 * HandleRelease provides two functionalities
 * 1. Create a release from a snapshot
 * 2. Create the next snapshot from a release
 */
public class HandleRelease {

    /**
     * SNAPSHOT identifier
     */
    public static final String SNAPSHOT = "SNAPSHOT";

    /**
     * Release the given version. Only works if version is a snapshot version (ends with -SNAPSHOT)
     *
     * @param versionStr the version to release
     * @return the released version as a {@link Semver}
     * @throws IllegalArgumentException when version is not a semantic or snapshot version
     */
    public static Semver release(String versionStr) {
        var version = Semver.Companion.parse(versionStr);
        if (!version.getPrerel().endsWith(SNAPSHOT)) {
            throw new IllegalArgumentException("Version " + version + " is not a SNAPSHOT version! Unable to release");
        }
        return version.release();
    }

    /**
     * Creates the next snapshot version by bumping the specified part of the release version and
     * then appending -SNAPASHOT to it. If this version is already a SNAPSHOT version (has -SNAPSHOT )
     * it simply returns this version.
     *
     * @param versionStr the version
     * @param bump       the part of the version to bump
     * @return the newly created snapshot version as a {@link Semver}
     * @throws IllegalArgumentException when version is not a semantic version.
     */
    public static Semver nextSnapshot(@NotNull String versionStr, Bump bump) {
        var version = Semver.Companion.toSemver(versionStr);
        if (version.getPrerel().endsWith(SNAPSHOT)) {
            return version;
        } else {
            return version.bump(bump).prerel(SNAPSHOT);
        }
    }

    /**
     * Simple main that demonstrates the usage of {@link HandleRelease}
     * @param args the passed in command line arguments
     */
    public static void main(String[] args) {
        var releaseVersion = HandleRelease.release("1.2.3-SNAPSHOT");
        var nextSnapshot = HandleRelease.nextSnapshot("1.2.3", Bump.MINOR);

        System.out.println("Release version: " + releaseVersion);
        System.out.println("Next snapshot: " + nextSnapshot);

        try {
            HandleRelease.release("1.2.3");
        } catch (IllegalArgumentException ex) {
            System.out.println(ex.getMessage());
        }
    }

}
