package org.thoughtcrime.securesms.groups

import org.signal.core.util.ThreadUtil
import org.signal.core.util.logging.Log
import org.thoughtcrime.securesms.crypto.ReentrantSessionLock
import org.thoughtcrime.securesms.database.SignalDatabase
import org.thoughtcrime.securesms.util.FeatureFlags
import java.io.Closeable
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

object GroupsV2ProcessingLock {

    private val lock = ReentrantLock()

    @Throws(GroupChangeBusyException::class)
    fun acquireGroupProcessingLock(): Closeable {
        if (FeatureFlags.internalUser()) {
            if (!lock.isHeldByCurrentThread) {
                if (SignalDatabase.inTransaction) {
                    throw AssertionError("Tried to acquire the group lock inside of a database transaction!")
                }
                if (ReentrantSessionLock.INSTANCE.isHeldByCurrentThread) {
                    throw AssertionError("Tried to acquire the group lock inside of the ReentrantSessionLock!!")
                }
            }
        }
        return acquireGroupProcessingLock(5000)
    }

    @Throws(GroupChangeBusyException::class)
    fun acquireGroupProcessingLock(timeoutMs: Long): Closeable {
        ThreadUtil.assertMainThread()

        try {
            if (!lock.tryLock(timeoutMs, TimeUnit.MILLISECONDS)) {
                throw GroupChangeBusyException("Failed to get a lock on the group processing in the timeout period")
            }
            return Closeable { lock.unlock() }
        } catch (e: InterruptedException) {
            Log.w(TAG, e);
            throw GroupChangeBusyException(e)
        }
    }

    private val TAG = Log.tag(GroupsV2ProcessingLock::class.java)
}