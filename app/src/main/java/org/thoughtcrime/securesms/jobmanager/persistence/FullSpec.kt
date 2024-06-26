package org.thoughtcrime.securesms.jobmanager.persistence

class FullSpec(
    val jobSpec: JobSpec,
    val constraintSpecs: List<ConstraintSpec>,
    val dependencySpecs: List<DependencySpec>
) {

    val isMemoryOnly: Boolean
        get() = jobSpec.isMemoryOnly
}