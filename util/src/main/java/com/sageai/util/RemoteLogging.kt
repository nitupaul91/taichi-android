package com.sageai.util

import android.content.Context
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named

class RemoteLogging @Inject constructor(
    @ApplicationContext private val context: Context,
    @Named("log_level_flow") private val logLevelFlow: Flow<String>,
    private val loggingDataStore: LoggingDataStore,
) {

    fun init() {
        val isInitialized = Timber.treeCount != 0
        if (isInitialized) {
            Timber.d("RemoteLogging already initialized.")
            return
        }

        GlobalScope.launch {
            val minLogLevel = loggingDataStore.getMinLogLevel().first()
            plantRemoteTrees(minLogLevel)

            observeLogLevelUpdates()
        }
    }

    private suspend fun observeLogLevelUpdates() {
        logLevelFlow.collect {
            updateLogLevel(it)
        }
    }

    private suspend fun updateLogLevel(minLogLevel: String) {
        loggingDataStore.setMinLogLevel(minLogLevel)

        plantRemoteTrees(minLogLevel)
    }

    private fun plantRemoteTrees(minLogLevel: String) {
        val mappedLogLevel = getMappedLogLevel(minLogLevel)
        Timber.uprootAll()

        Timber.plant(CrashlyticsLogTree(FirebaseCrashlytics.getInstance(), mappedLogLevel))
    }

    fun updateLoggingUserId(userId: String) {
        FirebaseCrashlytics.getInstance().setUserId(userId)
    }

    private fun getMappedLogLevel(minLogLevel: String): Int {
        return when (minLogLevel) {
            LogLevel.VERBOSE -> Log.VERBOSE
            LogLevel.DEBUG -> Log.DEBUG
            LogLevel.INFO -> Log.INFO
            LogLevel.WARN -> Log.WARN
            LogLevel.ERROR -> Log.ERROR
            LogLevel.ASSERT -> Log.ASSERT
            else -> {
                Timber.e(IllegalArgumentException("invalid log level $minLogLevel"))

                Log.ERROR
            }
        }
    }

}

class CrashlyticsLogTree(
    private val firebaseCrashlytics: FirebaseCrashlytics,
    private val minLogPriority: Int
) : Timber.Tree() {

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority < minLogPriority) {
            return
        }

        when {
            priority == Log.ERROR -> {
                firebaseCrashlytics.recordException(
                    CrashlyticsNonFatalError("$tag : $message", t)
                )
            }

            priority >= Log.INFO -> {
                firebaseCrashlytics.log("${mapPriority(priority)}/$tag: $message")
            }
        }
    }

    private fun mapPriority(priority: Int): String {
        return when (priority) {
            2 -> "V"
            3 -> "D"
            4 -> "I"
            5 -> "W"
            6 -> "E"
            7 -> "A"
            else -> ""
        }
    }

    class CrashlyticsNonFatalError constructor(message: String, cause: Throwable?) :
        RuntimeException(message, cause)
}

object LogLevel {
    const val VERBOSE = "v"
    const val DEBUG = "d"
    const val INFO = "i"
    const val WARN = "w"
    const val ERROR = "e"
    const val ASSERT = "a"
}