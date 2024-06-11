package org.thoughtcrime.securesms.util

class SignalLocalMetrics private constructor(){


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