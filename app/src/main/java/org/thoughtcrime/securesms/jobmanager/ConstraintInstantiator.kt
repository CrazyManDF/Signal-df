package org.thoughtcrime.securesms.jobmanager

class ConstraintInstantiator<T : Constraint>(
    constraintFactories: Map<String, Constraint.Factory<T>>
) {

    private val constraintFactories = hashMapOf<String, Constraint.Factory<T>>().apply {
        putAll(constraintFactories)
    }

    fun instantiate(constraintFactoryKey: String): Constraint {
        return if (constraintFactories.containsKey(constraintFactoryKey)) {
            constraintFactories[constraintFactoryKey]!!.create()
        } else {
            throw IllegalStateException("Tried to instantiate a constraint with key '$constraintFactoryKey', but no matching factory was found.")
        }
    }
}