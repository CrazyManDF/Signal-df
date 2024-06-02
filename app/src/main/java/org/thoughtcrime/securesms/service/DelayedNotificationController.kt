package org.thoughtcrime.securesms.service

class DelayedNotificationController : AutoCloseable{
    override fun close() {
        TODO("Not yet implemented")
    }

//    fun create(delayMillis: Long, createTask: Create): DelayedNotificationController{
//
//    }

    interface Create {
        fun create(): NotificationController
    }
}