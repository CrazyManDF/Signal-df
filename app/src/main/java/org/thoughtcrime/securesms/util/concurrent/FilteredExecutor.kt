package org.thoughtcrime.securesms.util.concurrent

import java.util.concurrent.Executor

class FilteredExecutor(
    private val backgroundExecutor: Executor,
    private val filter: Filter
) : Executor {

    override fun execute(command: Runnable) {
        if (filter.shouldRunOnExecutor()) {
            backgroundExecutor.execute(command)
        } else {
            command.run()
        }
    }

    interface Filter {
        fun shouldRunOnExecutor(): Boolean
    }
}