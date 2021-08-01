package io.github.musk.semver

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.test.assertFailsWith

internal class SemverTest {
    @ParameterizedTest
    @CsvSource(
        "0.2.1                 , RELEASE, 0.2.1 , no-op",
        "0.2.1                 , MAJOR  , 1.0.0 , major",
        "1.9.1                 , MAJOR  , 2.0.0 , minor",
        "v0.2.1                , MAJOR  , 1.0.0 , v major",
        "V0.2.1                , MAJOR  , 1.0.0 , V major",
        "0.2.1                 , MINOR  , 0.3.0 , minor",
        "1.9.1                 , MINOR  , 1.10.0, patch",
        "0.2.1                 , PATCH  , 0.2.2 , patch",
        "0.2.1-rc1.0           , PATCH  , 0.2.2 , strip pre-release",
        "0.2.1-rc1.0+build-1234, PATCH  , 0.2.2 , strip pre-release and build"
    )
    fun `Test that bump() creates correct Semver`(version: String, level: String, result: String, details: String) {
        val semver = Semver.parse(version).bump(Bump.valueOf(level))
        assertEquals(result, semver.toString(), details)
    }

    @ParameterizedTest
    @CsvSource(
        "0.2.1         , rc.1 , 0.2.1-rc.1 , add prerel",
        "0.2.1-0.2+b13 , rc.1 , 0.2.1-rc.1 , replace and strip build metadata",
        "0.2.1+b13     , rc.1 , 0.2.1-rc.1 , strip build metadata"
    )
    fun `Test that prerel() set prerel properly`(version: String, prerel: String, result: String, details: String) {
        val semver = Semver.parse(version).prerel(prerel)
        assertEquals(result, semver.toString(), details)
    }

    @ParameterizedTest
    @CsvSource(
        "1.0.0, x.7.z.092",
        "1.0.0, x.=.z.92",  //
        "1.0.0, x.7.z..92", //
        "1.0.0, .x.7.z.92", //
        "1.0.0, x.7.z.92."
    )
    fun `Test that prerel() throws IllegalArgumentException for illegal characters in prerel string`(
        version: String,
        prerel: String
    ) {
        // given
        val semver = Semver.parse(version)
        // then
        assertFailsWith<IllegalArgumentException>("Prerel:$prerel") { semver.prerel(prerel) }
    }

    @ParameterizedTest
    @CsvSource(
        "0.2.1+b13      , b.1       , 0.2.1+b.1       , replace build metadata",
        "0.2.1-rc12+b13 , b.1       , 0.2.1-rc12+b.1  , preserve prerel, replace build metadata",
        "1.0.0          , x.7.z.092 , 1.0.0+x.7.z.092 , attach build metadata"
    )
    fun `Test that build() replaces the build version properly`(
        version: String,
        build: String,
        result: String,
        details: String
    ) {
        val semver = Semver.parse(version).build(build)
        assertEquals(result, semver.toString(), details)
    }

    @ParameterizedTest
    @CsvSource(
        "1.0.0  , x.=.z.92  , bump invalid character in build-metadata: x.=.z.92",
        "1.0.0  , x.7.z..92 , bump invalid character in build-metadata: x.7.z..92",
        "1.0.0  , .x.7.z.92 , bump invalid character in build-metadata: .x.7.z.92",
        "1.0.0  , x.7.z.92. , bump invalid character in build-metadata: ",
        "1.0.0  , 7.z\\$.92  , bump invalid character in build-metadata: 7.z\\$.92",
        "1.0.0  , 7.z.92._  , bump invalid character in build-metadata: 7.z.92._",
        "1.0.0  , 7.z..92   , bump empty identifier in build-metadata (embedded)",
        "1.0.0  , .x.7.z.92 , bump empty identifier in build-metadata (leading)",
        "1.0.0  , z.92.     , bump empty identifier in build-metadata (trailing)"
    )
    fun `Test that build() fails with IllegalArgumentException for illegal characters in build number`(
        version: String,
        build: String,
        details: String
    ) {
        val semver = Semver.parse(version)
        assertFailsWith<java.lang.IllegalArgumentException>(details) { semver.build(build) }
    }

    @Test
    fun `Test that major is parsed properly`() {
        val semver = Semver.parse("0.2.1-rc1.0+build-1234")
        assertEquals(0, semver.major)
    }

    @Test
    fun `Test that minor is parsed properly`() {
        val semver = Semver.parse("0.2.1-rc1.0+build-1234")
        assertEquals(2, semver.minor)
    }

    @Test
    fun `Test that patch is properly parsed`() {
        val semver = Semver.parse("0.2.1-rc1.0+build-1234")
        assertEquals(1, semver.patch)
    }

    @ParameterizedTest
    @CsvSource(
        "0.2.1-rc1.0+build-1234 , rc1.0",
        "1.0.0-alpha            , alpha",
        "1.0.0-alpha.1          , alpha.1",
        "1.0.0-0alpha.1         , 0alpha.1",
        "1.0.0-0.3.7            , 0.3.7",
        "1.0.0-x.7.z.92         , x.7.z.92",
        "1.0.0-x-.7.--z.92-     , x-.7.--z.92-"
    )
    fun `Test that prerel is properly parsed`(version: String, prerel: String) {
        val semver = Semver.parse(version)
        assertEquals(prerel, semver.prerel)
    }

    @ParameterizedTest
    @CsvSource(
        "0.2.1-rc1.0+build-1234     , build-1234",
        "1.0.0-alpha+001            , 001",
        "1.0.0+20130313144700       , 20130313144700",
        "1.0.0-beta+exp.sha.5114f85 , exp.sha.5114f85",
        "1.0.0+exp.sha.5114f85      , exp.sha.5114f85",
        "1.0.0-x.7.z.92+02          , 02",
        "1.0.0-x.7.z.92+-alpha-2    , -alpha-2",
        "1.0.0-x.7.z.92+-alpha-2-   , -alpha-2-"
    )
    fun `Test that build is properly parsed`(version: String, build: String) {
        val semver = Semver.parse(version)
        assertEquals(build, semver.build)
    }

    @Test
    fun `Test that bump RELEASE creates a release version`() {
        val semver = Semver(0, 2, 1, "rc1.0", "build-1234").release()
        assertEquals("0.2.1", semver.toString())
    }

    @Test
    fun `Test that bump MAJOR increases the major version`() {
        val semver = Semver(1, 2, 3).major()
        assertEquals(2, semver.major)
        assertEquals("2.0.0", semver.toString())
    }

    @Test
    fun `Test that bump MINOR increases the minor version`() {
        val semver = Semver(1, 2, 3).minor()
        assertEquals(3, semver.minor)
        assertEquals("1.3.0", semver.toString())
    }

    @Test
    fun `Test that bump PATCH increases patch version`() {
        val semver = Semver(1, 2, 3).patch()
        assertEquals(4, semver.patch)
        assertEquals("1.2.4", semver.toString())
    }

    @Test
    fun `Test that bump RELEASE creates release version`() {
        val semver = Semver(1, 2, 3, "-SNAPSHOT").bump(Bump.RELEASE)
        assertEquals("1.2.3", semver.toString())
    }

    @ParameterizedTest
    @CsvSource(
        "Invalid semantic version 'foo', foo",
        "Invalid semantic version '1.2.', 1.2.",
        "Invalid semantic version '1.2.4-', 1.2.4-",
        "Invalid semantic version '1.2.4+', 1.2.4+"
    )
    fun `Test that message for illegal version is as expected`(message: String, version: String) {
        val ex = assertFailsWith<IllegalArgumentException> { Semver.parse(version) }
        assertEquals(message, ex.message)
    }

    @ParameterizedTest
    @CsvSource(
        "1.",
        "1.2",
        ".2.3",
        "01.9.1",
        "1.09.1",
        "1.9.01",
        "1.9.00",
        "1.9a.0",
        "-1.9.0",
        "1.0.0-x.7.z\\$.92",
        "1.0.0-x_.7.z.92",
        "1.0.0-x.7.z.092",
        "1.0.0-x.07.z.092",
        "1.0.0-x.7.z..92",
        "1.0.0-.x.7.z.92",
        "1.0.0-x.7.z.92.",
        "1.0.0-x+7.z\\$.92",
        "1.0.0-x+7.z.92._",
        "1.0.0+7.z\\$.92",
        "1.0.0-x+7.z..92",
        "1.0.0+.x.7.z.92",
        "1.0.0-x.7+z.92."
    )
    fun `Test that validate returns False on invalid version`(version: String) {
        assertFalse(Semver.validate(version), "Version: $version")
    }

    @ParameterizedTest
    @CsvSource("0.0.0, 1.2.3-alpha01")
    fun `Test that validate returns True on valid version`(version: String) {
        assertTrue(Semver.validate(version), "Version: $version")
    }

    @ParameterizedTest
    @CsvSource(
        "1.2.3             , <      , 2.2.3",
        "1.0.0-alpha       , <      , 1.0.0-alpha.1",
        "1.0.0-alpha.1     , <      , 1.0.0-alpha.beta",
        "1.0.0-alpha.beta  , <      , 1.0.0-beta",
        "1.0.0-beta        , <      , 1.0.0-beta.2",
        "1.0.0-beta.2      , <      , 1.0.0-beta.11",
        "1.0.0-beta.2.4    , >      , 1.0.0-beta.2.3",
        "1.0.0-beta.2.4    , <      , 1.0.0-beta.2.4.0",
        "1.0.0-beta.2.ab   , <      , 1.0.0-beta.2.ab.0",
        "1.0.0-beta.2.ab.1 , >      , 1.0.0-beta.2.ab.0",
        "1.0.0-beta.11     , <      , 1.0.0-rc.1",
        "1.0.0-rc.1        , <      , 1.0.0",
        "1.0.0             , >      , 1.0.0-rc.1",
        "1.0.0-alpha       , >      , 1.0.0-666",
        "1.0.0             , =      , 1.0.0",
        "1.0.1             , >      , 1.0.0-rc1",
        "1.0.0-beta2       , >      , 1.0.0-beta11",
        "1.0.0-2           , <      , 1.0.0-11",
        "1.0.0-beta1+a     , <      , 1.0.0-beta2+z",
        "1.0.0-beta2+x     , =      , 1.0.0-beta2+y",
        "1.0.0-12.beta2+x  , >      , 1.0.0-11.beta2+y",
        "1.0.0+x           , =      , 1.0.0+y",
        "0.2.1             , <      , 0.2.2",
        "1.2.1             , =      , 1.2.1",
        "0.3.1             , >      , 0.2.5",
        "1.0.0+hash        , <      , 1.0.0"
    )
    fun `Test comparison works as expected`(v1: String, operator: String, v2: String) {
        val left: Semver = Semver.parse(v1)
        val right: Semver = Semver.parse(v2)
        when (operator) {
            "<" -> {
                assertTrue(
                    left.compareTo(right) <= -1 && right.compareTo(left) >= 1,
                    "$left < $right"
                )
                assertTrue(left < right, "operator $left < $right")
                assertTrue(left <= right, "operator $left <= $right")
            }
            ">" -> {
                assertTrue(
                    left.compareTo(right) >= 1 && right.compareTo(left) <= -1,
                    "$left > $right"
                )
                assertTrue(left > right, "operator $left > $right")
                assertTrue(left >= right, "operator $left >= $right")
            }
            "=" -> {
                assertTrue(
                    left.compareTo(right) == 0 && right.compareTo(left) == 0,
                    "$left = $right"
                )
                assertTrue(left <= right, "operator $left <= $right")
                assertTrue(left >= right, "operator $left >= $right")
            }
            else -> throw IllegalArgumentException("Unknown operator '$operator'")
        }
    }

    @Test
    fun `Test that equals is true for same object`() {
        val semver = Semver(1, 2, 3)
        assertEquals(semver, semver)
    }

    @Test
    fun `Test equals method`() {
        val v1 = Semver(1, 2, 3, "rc1")
        val v2 = Semver(1, 2, 3, "rc1")
        assertEquals(v1, v2)
        assertEquals(v2, v1)
    }

    @Test
    fun `Test copy`() {
        val v1 = Semver(1, 2, 3, "rc1", "abc")
        val v2 = Semver.copy(v1)
        assertNotSame(v1, v2)
        assertEquals(v1, v2)
        assertEquals("1.2.3-rc1+abc", v2.toString())
    }

    @Test
    fun `Test clone`() {
        val v1 = Semver(1, 2, 3, "rc1", "abc")
        val v2 = v1.clone()
        assertNotSame(v1, v2)
        assertEquals(v1, v2)
        assertEquals("1.2.3-rc1+abc", v2.toString())
    }

    @Test
    @DisplayName("Test toString")
    fun `Test toString`() {
        val v1 = Semver(1, 2, 3, "rc1", "abc")
        assertEquals("1.2.3-rc1+abc", v1.toString())
    }
}