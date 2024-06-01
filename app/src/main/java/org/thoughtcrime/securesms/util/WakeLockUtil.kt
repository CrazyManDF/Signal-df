package org.thoughtcrime.securesms.util

import android.content.Context
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import org.signal.core.util.logging.Log

object WakeLockUtil {

    private val TAG = Log.tag(WakeLockUtil::class.java)

    fun acquire(
        context: Context,
        lockType: Int,
        timeout: Long,
        oldTag: String
    ): PowerManager.WakeLock? {
        val tag = prefixTag(oldTag)
        runCatching {
            val powerManager = ServiceUtil.getPowerManager(context)
            val weakLock = powerManager.newWakeLock(lockType, tag)

            weakLock.acquire(timeout)

            return weakLock
        }.getOrElse { e ->
            Log.w(TAG, "Failed to acquire wakelock with tag: $tag", e)
            return null
        }
    }

    fun release(wakeLock: WakeLock?, oldTag: String) {
        val tag = prefixTag(oldTag)
        runCatching {
            if (wakeLock == null) {
                Log.d(TAG, "Wakelock was null. Skipping. Tag: $tag")
            } else if (wakeLock.isHeld) {
                wakeLock.release()
            } else {
                Log.d(TAG, "Wakelock wasn't held at time of release: $tag")
            }
        }.onFailure { e ->
            Log.w(TAG, "Failed to release wakelock with tag: $tag", e)
        }
    }

    private fun prefixTag(tag: String): String {
        return if (tag.startsWith("signal:")) tag else "signal:$tag"
    }
}