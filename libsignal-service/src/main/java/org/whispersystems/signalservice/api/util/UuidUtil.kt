package org.whispersystems.signalservice.api.util

import okio.ByteString
import java.nio.ByteBuffer
import java.util.Optional
import java.util.UUID
import java.util.regex.Pattern

object UuidUtil {
    val UNKNOWN_UUID: UUID = UUID(0, 0)
    val UNKNOWN_UUID_STRING: String = UNKNOWN_UUID.toString()

    private val UUID_PATTERN: Pattern = Pattern.compile(
        "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}",
        Pattern.CASE_INSENSITIVE
    )

    fun parse(uuid: String?): Optional<UUID> {
        return Optional.ofNullable(parseOrNull(uuid))
    }

    fun parseOrNull(uuid: String?): UUID? {
        return if (isUuid(uuid)) parseOrThrow(uuid) else null
    }

    fun parseOrUnknown(uuid: String?): UUID {
        return if (uuid == null || uuid.isEmpty()) UNKNOWN_UUID else parseOrThrow(uuid)
    }

    fun parseOrThrow(uuid: String?): UUID {
        return UUID.fromString(uuid)
    }

    fun parseOrThrow(bytes: ByteArray?): UUID {
        val byteBuffer = ByteBuffer.wrap(bytes)
        val high = byteBuffer.getLong()
        val low = byteBuffer.getLong()

        return UUID(high, low)
    }

    fun isUuid(uuid: String?): Boolean {
        return uuid != null && UUID_PATTERN.matcher(uuid).matches()
    }

    fun toByteArray(uuid: UUID): ByteArray {
        val buffer = ByteBuffer.wrap(ByteArray(16))
        buffer.putLong(uuid.mostSignificantBits)
        buffer.putLong(uuid.leastSignificantBits)

        return buffer.array()
    }

    fun toByteString(uuid: UUID): ByteString {
        return ByteString.of(*toByteArray(uuid))
    }

    fun fromByteString(bytes: ByteString): UUID {
        return parseOrThrow(bytes.toByteArray())
    }

    fun fromByteStringOrNull(bytes: ByteString?): UUID? {
        return parseOrNull(bytes?.toByteArray())
    }

    fun fromByteStringOrUnknown(bytes: ByteString): UUID {
        val uuid = fromByteStringOrNull(bytes)
        return uuid ?: UNKNOWN_UUID
    }

    fun parseOrNull(byteArray: ByteArray?): UUID? {
        return if (byteArray != null && byteArray.size == 16) parseOrThrow(byteArray) else null
    }

    fun fromByteStrings(byteStringCollection: Collection<ByteString>): List<UUID> {
        val result = ArrayList<UUID>(byteStringCollection.size)

        for (byteString in byteStringCollection) {
            result.add(fromByteString(byteString))
        }

        return result
    }

    /**
     * Keep only UUIDs that are not the [.UNKNOWN_UUID].
     */
    fun filterKnown(uuids: Collection<UUID>): List<UUID> {
        val result = ArrayList<UUID>(uuids.size)

        for (uuid in uuids) {
            if (UNKNOWN_UUID != uuid) {
                result.add(uuid)
            }
        }

        return result
    }
}