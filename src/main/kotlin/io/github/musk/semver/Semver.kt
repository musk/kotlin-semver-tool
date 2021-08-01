package io.github.musk.semver


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

        fun String.toSemver(): Semver = parse(this)
        fun String.isSemver(): Boolean = validate(this)
        inline fun String.ifSemver(block: (Semver) -> Unit): Semver? {
            return if (validate(this)) {
                val semver = parse(this)
                block(semver)
                semver
            } else null
        }

        fun validate(version: String): Boolean = match(version) != null

        fun parse(version: String): Semver {
            val matches = match(version)
            requireNotNull(matches) { "Invalid semantic version '$version'" }
            val (majorStr, minorStr, patchStr, prerelStr, buildStr) = matches.destructured
            return Semver(majorStr.toInt(), minorStr.toInt(), patchStr.toInt(), prerelStr, buildStr)
        }

        fun copy(semver: Semver): Semver = Semver(semver.major, semver.minor, semver.patch, semver.prerel, semver.build)

        fun comparePrerel(leftPrerel: String, rightPrerel: String): Int {
            if (leftPrerel.isEmpty()) return 1
            if (rightPrerel.isEmpty()) return -1

            // Precedence for two pre-release versions with the same major, minor, and patch version MUST be determined
            // by comparing each dot separated identifier from left to right until a difference is found as follows:
            val lIdentifiers = leftPrerel.split('.')
            val rIdentifiers = rightPrerel.split('.')

            val pairs = lIdentifiers.zip(rIdentifiers)
            for ((left, right) in pairs) {
                if (left != right) {
                    try {
                        // identifiers consisting of only digits are compared numerically and
                        val l = left.toInt()
                        val r = right.toInt()
                        return l.compareTo(r)
                    } catch (ex: NumberFormatException) {
                        // identifiers with letters or hyphens are compared lexically in ASCII sort order.
                        return left.compareTo(right)
                    }
                }
            }
            return lIdentifiers.size.compareTo(rIdentifiers.size)
        }
    }

    fun clone(): Semver = copy(this)

    fun release(): Semver = bump(Bump.RELEASE)

    fun major(): Semver = bump(Bump.MAJOR)

    fun minor(): Semver = bump(Bump.MINOR)

    fun patch(): Semver = bump(Bump.PATCH)

    fun prerel(prerel: String): Semver {
        if (Regex(PREREL_REGEX).matches(prerel)) return Semver(major, minor, patch, prerel)
        else throw IllegalArgumentException("Invalid prerel '$prerel'")
    }

    fun build(build: String): Semver {
        if (Regex(BUILD_REGEX).matches(build)) return Semver(major, minor, patch, prerel, build)
        else throw IllegalArgumentException("Invalid build number '$build'")
    }

    fun bump(bump: Bump): Semver {
        return when (bump) {
            Bump.MAJOR -> Semver(this.major + 1, 0, 0)
            Bump.MINOR -> Semver(this.major, this.minor + 1, 0)
            Bump.PATCH -> Semver(major, minor, patch + 1)
            Bump.RELEASE -> Semver(major, minor, patch)
        }
    }

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

    override operator fun compareTo(other: Semver): Int {
        if (major != other.major)
            return major.compareTo(other.major)
        else if (minor != other.minor)
            return minor.compareTo(other.minor)
        else if (patch != other.patch)
            return patch.compareTo(other.patch)
        else if (prerel != other.prerel) {
            return comparePrerel(prerel, other.prerel)
        } else if (build.isEmpty() && other.build.isNotEmpty()) {
            return 1
        } else if (build.isNotEmpty() && other.build.isEmpty()) {
            return -1
        }
        // build numbers are ignored in precedence
        return 0
    }
}
