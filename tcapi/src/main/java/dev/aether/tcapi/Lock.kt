@file:Suppress("unused")
package dev.aether.tcapi

/**
 * A single resource that cannot be used by more than one task at a time.
 * Common usages include representations of hardware (physical devices) and proxy objects.
 */
interface Lock {
    /**
     * A name that could be used to refer to this lock in error messages or logs.
     * Hopefully descriptive enough to locate when problems occur.
     */
    fun getFriendlyName(): String

    /**
     * Called internally when this lock becomes 'acquired' (in use) by a Task.
     */
    fun onAcquire(source: ITask) {}

    /**
     * Called internally when this lock is no longer 'acquired' by any Task and becomes
     * able to be claimed by any task.
     */
    fun onRelease(source: ITask) {}

    /**
     * Called internally when this lock is *explicitly* transferred to another Task.
     * Transferring locks prevents 'lock stealing' during the small transition period
     * due to `.then()`.
     *
     * **Warning** - transferring locks like this can lead to deadlocking if the `destination`
     * task cannot start due to missing some other dependency.
     *
     * @param source Previous owner of the [Lock].
     * @param destination Next owner of the [Lock], when it begins.
     */
    fun onTransfer(source: ITask, destination: ITask) {}

    /**
     * Create a new Lock derived from this Lock. The new lock should have a unique
     * identity (it should not be equivalent to any other lock) but should be obviously
     * associated with this one. The provided [annotation] may optionally be used to
     * provide additional information.
     *
     * @param annotation Descriptive information about the derivation. May be used to
     *                   provide additional information about the derived Lock.
     */
    fun derive(annotation: String?): Lock

    /**
     * Create a new Lock derived from this Lock. The new lock should have a unique
     * identity (it should not be equivalent to any other lock) but should be obviously
     * associated with this one.
     */
    fun derive(): Lock = derive(null)

    /**
     * Simple implementation of [Lock] based around string IDs.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    class Str(val name: String) : Lock {
        private var deriveCount = 0
        override fun getFriendlyName() = name

        override fun derive(annotation: String?): Str {
            val n = deriveCount++
            return Str(buildString {
                append(name)
                append(".derive")
                append(n)
                if (annotation != null) {
                    append(":")
                    append(annotation)
                }
            })
        }
    }
}