package org.thoughtcrime.securesms.messages

enum class MessageState {
    DECRYPTED_OK,
    INVALID_VERSION,
    CORRUPT_MESSAGE,  // Not used, but can't remove due to serialization
    NO_SESSION,  // Not used, but can't remove due to serialization
    LEGACY_MESSAGE,
    DUPLICATE_MESSAGE,
    UNSUPPORTED_DATA_MESSAGE,
    NOOP,
    DECRYPTION_ERROR
}