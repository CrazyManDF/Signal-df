package org.thoughtcrime.securesms.database

import androidx.compose.runtime.saveable.autoSaver
import org.thoughtcrime.securesms.recipients.RecipientId
import org.thoughtcrime.securesms.util.LRUCache

class EarlyDeliveryReceiptCache {

    private val cache = LRUCache<Long, HashMap<RecipientId, Receipt>>(100)

    @Synchronized
    fun increment(targetTimestamp: Long, receiptAuthor: RecipientId, receiptSentTimestamp: Long) {
        var receipts = cache[targetTimestamp]

        if (receipts == null) {
            receipts = hashMapOf()
        }

        var receipt = receipts[receiptAuthor]
        if (receipt != null) {
            receipt.count++
            receipt.timestamp = receiptSentTimestamp
        } else {
            receipt = Receipt(1, receiptSentTimestamp)
        }
        receipts[receiptAuthor] = receipt
        cache[targetTimestamp] = receipts
    }

    @Synchronized
    fun remove(timestamp: Long): Map<RecipientId, Receipt> {
        val receipts = cache.remove(timestamp)
        return receipts ?: hashMapOf()
    }

    class Receipt(
        var count: Long, var timestamp: Long
    )
}