package org.thoughtcrime.securesms

import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.appcompat.app.AppCompatActivity
import org.signal.core.util.logging.Log

class MainActivity : AppCompatActivity() {

    private val onFirstRender = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

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