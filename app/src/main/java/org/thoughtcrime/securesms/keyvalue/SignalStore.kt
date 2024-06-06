package org.thoughtcrime.securesms.keyvalue

import kotlin.concurrent.Volatile

class SignalStore {

    private val accountValues: AccountValues? = AccountValues()

    private val internalValues: InternalValues? = InternalValues()

    companion object {
        internal fun account(): AccountValues {
            return getInstance().accountValues!!
        }

        fun internalValues(): InternalValues {
            return getInstance().internalValues!!
        }

        fun proxy(): ProxyValues{

        }

        @Volatile
        private var instance: SignalStore? = null

        fun getInstance(): SignalStore {
            if (instance == null) {
                synchronized(SignalStore::class.java) {
                    if (instance == null) {
                        instance = SignalStore()
                    }
                }
            }
            return instance!!
        }
    }

}