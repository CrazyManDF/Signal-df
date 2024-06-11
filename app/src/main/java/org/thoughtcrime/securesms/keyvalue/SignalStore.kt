package org.thoughtcrime.securesms.keyvalue

import org.thoughtcrime.securesms.dependencies.ApplicationDependencies
import kotlin.concurrent.Volatile

class SignalStore(store: KeyValueStore) {

    private val accountValues: AccountValues = AccountValues(store)

    private val internalValues: InternalValues = InternalValues(store)

    private val proxyValues: ProxyValues = ProxyValues(store)

    companion object {
        internal fun account(): AccountValues {
            return getInstance().accountValues
        }

        fun internalValues(): InternalValues {
            return getInstance().internalValues
        }

        fun proxy(): ProxyValues {
            return getInstance().proxyValues
        }

        @Volatile
        private var instance: SignalStore? = null

        fun getInstance(): SignalStore {
            if (instance == null) {
                synchronized(SignalStore::class.java) {
                    if (instance == null) {
                        instance = SignalStore(
                            KeyValueStore(
                                KeyValueDatabase.getInstance(ApplicationDependencies.getApplication())
                            )
                        )
                    }
                }
            }
            return instance!!
        }
    }

}