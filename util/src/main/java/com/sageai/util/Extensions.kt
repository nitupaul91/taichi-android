package com.sageai.util

import android.os.Looper
import kotlinx.coroutines.delay

suspend fun <T> retryIO(
    times: Int = 3,
    initialDelay: Long = 500, // 0.1 second
    maxDelay: Long = 5000,    // 1 second
    factor: Double = 3.0,
    block: suspend () -> T
): T {
    var currentDelay = initialDelay
    repeat(times - 1) {
        try {
            return block()
        } catch (e: Throwable) {
            // you can log an error here and/or make a more finer-grained
            // analysis of the cause to see if retry is needed
        }
        delay(currentDelay)
        currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
    }
    return block() // last attempt
}

fun requireMainThread() =
    require(Looper.myLooper() == Looper.getMainLooper()) { "This operation must be executed on the main thread" }