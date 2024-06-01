package org.thoughtcrime.securesms.database

import android.content.Context
import org.thoughtcrime.securesms.util.SingletonHolder

open class JobDatabase private constructor(context: Context) {

    init {

    }


    companion object : SingletonHolder<JobDatabase, Context>(::JobDatabase)
}
