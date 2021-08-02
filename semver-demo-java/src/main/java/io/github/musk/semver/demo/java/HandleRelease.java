package io.github.musk.semver.demo.java;

import org.jetbrains.annotations.NotNull;

import io.github.musk.semver.Bump;
import io.github.musk.semver.Semver;

public class HandleRelease {

    public static final String SNAPSHOT = "SNAPSHOT";

    public HandleRelease() {
    }

    public static Semver release(String versionStr) {
        var version = Semver.Companion.parse(versionStr);
        if(!version.getPrerel().endsWith(SNAPSHOT))
            throw new IllegalArgumentException("Version "+version.toString() + " is not a SNAPSHOT version! Unable to release");
        return version.release();
    }

    public static Semver nextSnapshot(@NotNull String versionStr, Bump bump) {
        var version = Semver.Companion.toSemver(versionStr);
        if(version.getPrerel().endsWith(SNAPSHOT))
            return version;
        else {
            return version.bump(bump).prerel(SNAPSHOT);
        }
    }

    public static void main(String[] args) {
        var releaseVersion = HandleRelease.release("1.2.3-SNAPSHOT");
        var nextSnapshot = HandleRelease.nextSnapshot("1.2.3", Bump.MINOR);

        System.out.println("Release version: " + releaseVersion);
        System.out.println("Next snapshot: " + nextSnapshot);

        try {
            HandleRelease.release("1.2.3");
        } catch(IllegalArgumentException ex) {
            System.out.println(ex.getMessage());
        }
    }

}
