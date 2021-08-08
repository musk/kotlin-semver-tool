package io.github.musk.semver

/**
 * Defines a semantic version acccording to [Semantic Version 2.0](https://semver.org)
 */
class Semver(val major: Int, val minor: Int, val patch: Int, val prerel: String = "", val build: String = "") :
    Comparable<Semver> {

    companion object {
        private const val NAT = "0|[1-9][0-9]*"
        private const val ALPHANUM = "[0-9]*[A-Za-z-][0-9A-Za-z-]*"
        private const val IDENT = "$NAT|$ALPHANUM"
        private const val FIELD = "[0-9A-Za-z-]+"
        private const val PREREL_REGEX = "((?:$IDENT)(?:\\.(?:$IDENT))*)"
        private const val BUILD_REGEX = "($FIELD(?:\\.$FIELD)*)"
        private const val SEMVER_REGEX =
            "^[vV]?($NAT)\\.($NAT)\\.($NAT)(?:-$PREREL_REGEX)?(?:\\+$BUILD_REGEX)?$"

        private fun match(version: String): MatchResult? = Regex(SEMVER_REGEX).find(version)

        /**
         * Turn a [String] into a [Semver]
         *
         * Throws an [IllegalArgumentException] when the [String] does not define a valid semantic version
         */
        fun String.toSemver(): Semver = parse(this)

        /**
         * Checks if the [String] contains a valid semantic version
         */
        fun String.isSemver(): Boolean = validate(this)

        /**
         * Convenience method to execute a block and return the [String] as a [Semver] if it
         * is a valid semantic version. If not the method return null.
         * The [String] is turned into a [Semver] and passed into the block as an argument.
         */
        inline fun String.ifSemver(block: (Semver) -> Unit): Semver? {
            return if (validate(this)) {
                val semver = parse(this)
                block(semver)
                semver
            } else null
        }

        /**
         * Validates the [String] whether it is a valid semantic version.
         */
        fun validate(version: String): Boolean = match(version) != null

        /**
         * Parses the given [String] into a [Semver].
         *
         * Throws [IllegalArgumentException] when the [String] does not designate a valid semantic version.
         */
        fun parse(version: String): Semver {
            val matches = match(version)
            requireNotNull(matches) { "Invalid semantic version '$version'" }
            val (majorStr, minorStr, patchStr, prerelStr, buildStr) = matches.destructured
            return Semver(majorStr.toInt(), minorStr.toInt(), patchStr.toInt(), prerelStr, buildStr)
        }

        /**
         * Creates a copy of the given [Semver].
         */
        fun copy(semver: Semver): Semver = Semver(semver.major, semver.minor, semver.patch, semver.prerel, semver.build)

        /**
         * Compares two labels according to the rules of [semantic versioning 2.0.0](https://semver.org). A label is the
         * part given after the first hyphen and before the first plus sign.
         */
        fun comparePrerel(leftPrerel: String, rightPrerel: String): Int {
            if (leftPrerel.isEmpty()) return 1
            if (rightPrerel.isEmpty()) return -1

            return compareIdentifiers(leftPrerel, rightPrerel)
        }

        /**
         * Precedence for two pre-release versions with the same major, minor, and patch version MUST be determined
         * by comparing each dot separated identifier from left to right until a difference is found:
         * * identifiers consisting of only digits are compared numerically
         * * identifiers with letters or hyphens are compared lexically in ASCII sort order.
         * * the shorter one is considered smaller
         */
        private fun compareIdentifiers(leftPrerel: String, rightPrerel: String): Int {
            val lIdentifiers = leftPrerel.split('.')
            val rIdentifiers = rightPrerel.split('.')

            val pairs = lIdentifiers.zip(rIdentifiers)
            for ((left, right) in pairs) {
                if (left != right) {
                    return try {
                        // compare digit by digit
                        val l = left.toInt()
                        val r = right.toInt()
                        l.compareTo(r)
                    } catch (ex: NumberFormatException) {
                        // not a digit compare lexically
                        left.compareTo(right)
                    }
                }
            }
            return lIdentifiers.size.compareTo(rIdentifiers.size)
        }
    }

    fun clone(): Semver = copy(this)

    /**
     * Create a release version by stripping off the label and build information.
     */
    fun release(): Semver = bump(Bump.RELEASE)

    /**
     * Bump the major version of this [Semver] setting the minor and patch
     * version to 0 and removing any label or build information.
     */
    fun major(): Semver = bump(Bump.MAJOR)

    /**
     * Bump the minor version of this [Semver] setting the patch version to 0 and
     * removing any label or build information.
     */
    fun minor(): Semver = bump(Bump.MINOR)

    /**
     * Bump the patch version of this [Semver] removing any label or build information.
     */
    fun patch(): Semver = bump(Bump.PATCH)

    /**
     * Set the label part of this [Semver] removing any build information.
     *
     * Throws [IllegalArgumentException] when the [String] does not designate
     * a valid label according to [semantic version 2.0](https://semver.org)
     */
    fun prerel(prerel: String): Semver {
        if (Regex(PREREL_REGEX).matches(prerel)) return Semver(major, minor, patch, prerel)
        else throw IllegalArgumentException("Invalid prerel '$prerel'")
    }

    /**
     * Set the build part of this [Semver].
     *
     * Throws [IllegalArgumentException] when the [String] does not designate
     * a valid build number according to [semantic version 2.0](https://semver.org)
     */
    fun build(build: String): Semver {
        if (Regex(BUILD_REGEX).matches(build)) return Semver(major, minor, patch, prerel, build)
        else throw IllegalArgumentException("Invalid build number '$build'")
    }

    /**
     * Bump the specified part of this [Semver]. A bump always sets any of the following
     * version parts to 0 and removes any label or build information.
     */
    fun bump(bump: Bump): Semver {
        return when (bump) {
            Bump.MAJOR -> Semver(this.major + 1, 0, 0)
            Bump.MINOR -> Semver(this.major, this.minor + 1, 0)
            Bump.PATCH -> Semver(major, minor, patch + 1)
            Bump.RELEASE -> Semver(major, minor, patch)
        }
    }

    /**
     * Returns this [Semver] as [String]. The given [String] can be passed
     * into [Semver.parse] to recreate this [Semver].
     * In other words `"1.2.3-SNAPSHOT+12" == Semver(1,2,3,"SNAPSHOT", "12").toString()` and
     * `Semver.parse("1.2.3-SNAPSHOT+12").toString() == "1.2.3-SNAPSHOT+12"`
     */
    override fun toString(): String {
        return "$major.$minor.$patch" +
                (if (prerel.isNotEmpty()) "-$prerel" else "") +
                (if (build.isNotEmpty()) "+$build" else "")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Semver) return false

        if (major != other.major) return false
        if (minor != other.minor) return false
        if (patch != other.patch) return false
        if (prerel != other.prerel) return false
        if (build != other.build) return false

        return true
    }

    override fun hashCode(): Int {
        var result = major
        result = 31 * result + minor
        result = 31 * result + patch
        result = 31 * result + prerel.hashCode()
        result = 31 * result + build.hashCode()
        return result
    }

    /**
     * Implements a natural ordering for [Semver] according to the rules defined by
     * [semantic versioning 2.0.0](https://semver.org)
     */
    override operator fun compareTo(other: Semver): Int = when {
        (major != other.major) -> major.compareTo(other.major)
        (minor != other.minor) -> minor.compareTo(other.minor)
        (patch != other.patch) -> patch.compareTo(other.patch)
        (prerel != other.prerel) -> comparePrerel(prerel, other.prerel)
        (build.isEmpty() && other.build.isNotEmpty()) -> 1
        (build.isNotEmpty() && other.build.isEmpty()) -> -1
        // build numbers are ignored in precedence
        else -> 0
    }
}
