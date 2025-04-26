package io.github.gearup12499.taskshark.legacy

/**
 * it's a SharedResource! a "lock" but without any of the atomic magic
 */
@Deprecated("the legacy api is deprecated")
class SharedResource @JvmOverloads constructor(val id: String, val panic: () -> Unit = {}) {
    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return when (other) {
            (other == null) -> false
            is String -> id == other
            is SharedResource -> id == other.id
            else -> false
        }
    }

    override fun toString(): String {
        return "SharedResource:$id"
    }
}