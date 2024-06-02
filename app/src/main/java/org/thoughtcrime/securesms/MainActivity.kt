package org.thoughtcrime.securesms

import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import org.signal.core.util.logging.Log
import org.thoughtcrime.securesms.dependencies.ApplicationDependencies
import org.thoughtcrime.securesms.jobs.MessageFetchJob

class MainActivity : AppCompatActivity() {

    private val onFirstRender = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        findViewById<Button>(R.id.button).setOnClickListener {
            ApplicationDependencies.jobManager.add(MessageFetchJob())
        }

//        val content = findViewById<View>(android.R.id.content)
//        content.viewTreeObserver.addOnPreDrawListener(
//            object : ViewTreeObserver.OnPreDrawListener {
//                override fun onPreDraw(): Boolean {
//                    Log.d(TAG, "=====$onFirstRender")
//                    if (onFirstRender) {
//                        content.viewTreeObserver.removeOnPreDrawListener(this)
//                        return true
//                    } else {
//                        return false
//                    }
//                }
//            }
//        )


    }

    companion object {
        private val TAG = Log.tag(MainActivity::class.java)
    }
}