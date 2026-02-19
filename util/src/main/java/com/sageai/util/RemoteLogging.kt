package com.sageai.util

import android.content.Context
import android.util.Log
import com.bugfender.sdk.Bugfender
import com.bugfender.sdk.LogLevel
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

        Bugfender.init(context, BUGFENDER_KEY, BuildConfig.DEBUG)

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

        Timber.plant(BugfenderLogTree(mappedLogLevel))

        Timber.plant(CrashlyticsLogTree(FirebaseCrashlytics.getInstance(), mappedLogLevel))
    }

    fun updateLoggingUserId(userId: String) {
        Bugfender.setDeviceString(PARAM_USER_ID, userId)
        FirebaseCrashlytics.getInstance().setUserId(userId)
    }

    private fun getMappedLogLevel(minLogLevel: String): Int {
        return when (minLogLevel) {
            com.sageai.util.LogLevel.VERBOSE -> Log.VERBOSE
            com.sageai.util.LogLevel.DEBUG -> Log.DEBUG
            com.sageai.util.LogLevel.INFO -> Log.INFO
            com.sageai.util.LogLevel.WARN -> Log.WARN
            com.sageai.util.LogLevel.ERROR -> Log.ERROR
            com.sageai.util.LogLevel.ASSERT -> Log.ASSERT
            else -> {
                Timber.e(IllegalArgumentException("invalid log level $minLogLevel"))

                Log.ERROR
            }
        }
    }

    private companion object {
        private const val BUGFENDER_KEY = "mdWAauZb3CZsYT7hzu8B93fIapTKlYFM"

        private const val PARAM_USER_ID = "userId"
    }
}

class BugfenderLogTree(
    private val minLogPriority: Int
) : Timber.Tree() {

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority < minLogPriority) {
            return
        }
        val logLevel = when (priority) {
            Log.VERBOSE -> LogLevel.Trace
            Log.DEBUG -> LogLevel.Debug
            Log.INFO -> LogLevel.Info
            Log.WARN -> LogLevel.Warning
            Log.ERROR -> LogLevel.Error
            Log.ASSERT -> LogLevel.Fatal
            else -> LogLevel.Debug
        }

        Bugfender.log(-1, "", "", logLevel, tag, message);
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