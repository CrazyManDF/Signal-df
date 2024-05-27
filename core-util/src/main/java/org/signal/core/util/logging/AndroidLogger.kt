package org.signal.core.util.logging


class AndroidLogger : Log.Logger() {
    override fun v(tag: String, message: String?, t: Throwable?, keepLonger: Boolean) {
        android.util.Log.v(tag, message, t)
    }

    override fun d(tag: String, message: String?, t: Throwable?, keepLonger: Boolean) {
        android.util.Log.d(tag, message, t)
    }

    override fun i(tag: String, message: String?, t: Throwable?, keepLonger: Boolean) {
        android.util.Log.i(tag, message, t)
    }

    override fun w(tag: String, message: String?, t: Throwable?, keepLonger: Boolean) {
        android.util.Log.w(tag, message, t)
    }

    override fun e(tag: String, message: String?, t: Throwable?, keepLonger: Boolean) {
        android.util.Log.e(tag, message, t)
    }

    override fun flush() {

    }
}