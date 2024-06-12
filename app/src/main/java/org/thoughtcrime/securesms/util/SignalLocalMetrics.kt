package org.thoughtcrime.securesms.util

import java.util.concurrent.atomic.AtomicInteger

class SignalLocalMetrics private constructor(){


    object PushWebsocketFetch {

        const val SUCCESS_EVENT: String = "push-websocket-fetch"
        const val TIMEOUT_EVENT: String = "timed-out-fetch"

        private const val SPLIT_BATCH_PROCESSED = "batches-processed"
        private const val SPLIT_PROCESS_TIME = "fetch-time"
        private const val SPLIT_TIMED_OUT = "timeout"

        private val processedBatches = AtomicInteger(0)

        fun startFetch(): String {
            val baseId = System.currentTimeMillis().toString()

            val timeoutId = TIMEOUT_EVENT + baseId
            val successId = SUCCESS_EVENT + baseId

//            LocalMetrics.getInstance().start(successId, SUCCESS_EVENT)
//            LocalMetrics.getInstance().start(timeoutId, TIMEOUT_EVENT)

            processedBatches.set(0)
            return baseId
        }

        fun onProcessedBatch() {
            processedBatches.incrementAndGet()
        }

        fun onTimedOut(metricId: String) {
//            LocalMetrics.getInstance().cancel(SUCCESS_EVENT + metricId)

            val timeoutId = TIMEOUT_EVENT + metricId

//            LocalMetrics.getInstance().split(timeoutId, SPLIT_TIMED_OUT)
//            LocalMetrics.getInstance().end(timeoutId)
        }

        fun onDrained(metricId: String) {
//            LocalMetrics.getInstance().cancel(TIMEOUT_EVENT + metricId)

            val successId = SUCCESS_EVENT + metricId

//            LocalMetrics.getInstance().split(successId, SPLIT_PROCESS_TIME)
//            LocalMetrics.getInstance()
//                .splitWithDuration(successId, SPLIT_BATCH_PROCESSED, processedBatches.get())
//            LocalMetrics.getInstance().end(successId)
        }
    }

    class MessageLatency {


        companion object {
            fun onMessageReceived(serverReceiveTimestamp: Long , serverDeliverTimestamp: Long , highPriority: Boolean ) {

            }
        }
    }

     class MessageReceive {

         fun onEnvelopeDecrypted() {
             split(SPLIT_DECRYPTION)
         }

         private fun split(name: String) {
//             LocalMetrics.getInstance().split(groupMetricId, name)
//             LocalMetrics.getInstance().split(individualMetricId, name)
         }

         companion object {

             private const val NAME_GROUP = "group-message-receive"
             private const val NAME_INDIVIDUAL = "individual-message-receive"

             private const val SPLIT_DECRYPTION = "decryption"
             private const val SPLIT_PRE_PROCESS = "pre-process"
             private const val SPLIT_GROUPS_PROCESSING = "groups-v2"
             private const val SPLIT_DB_INSERT_MEDIA = "media-insert"
             private const val SPLIT_DB_INSERT_TEXT = "text-insert"
             private const val SPLIT_POST_PROCESS = "post-process"

             fun start(): MessageReceive {
                 return MessageReceive()
             }
         }
    }
}