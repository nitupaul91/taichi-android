package taichi.walking.seniors.beginners.util

import taichi.walking.seniors.beginners.BuildConfig
import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.mobteq.billing.datastore.DataStorePrefs
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import javax.inject.Inject

class FirebaseAppCheckUtil @Inject constructor(
    private val sharedPrefs: DataStorePrefs
) {

    fun initFirebaseAppCheck(context: Context) {
        init(context)
        fetchTokenIfNeeded()
    }

    suspend fun getToken(): String {
        return if (isTokenExpired()) {
            Timber.d("token is expired")
            refreshToken()
            Timber.d("new token is fetched ${sharedPrefs.getFirebaseAppCheckToken().first()}")
            sharedPrefs.getFirebaseAppCheckToken().first()
        } else {
            Timber.d("token is valid")
            sharedPrefs.getFirebaseAppCheckToken().first()
        }
    }

    private fun fetchTokenIfNeeded() {
        runBlocking {
            if (isTokenExpired()) {
                val expiredToken = sharedPrefs.getFirebaseAppCheckToken()
                Timber.d("token $expiredToken is expired")
                refreshToken()
            }
        }
    }

    private suspend fun isTokenExpired(): Boolean {
        val now = System.currentTimeMillis()
        val expiry = sharedPrefs.getFirebaseAppCheckTokenExpiryInMillis()
        Timber.d("expiry is at $expiry")
        return expiry - now < 0
    }

    private fun refreshToken() {
        FirebaseAppCheck.getInstance().getAppCheckToken(false)
            .addOnFailureListener {
                Timber.d("Something went wrong - Exception ${it.message}")
            }
            .addOnSuccessListener { tokenResponse ->
                val appCheckToken = tokenResponse.token
                Timber.d("token was fetched $appCheckToken")
                runBlocking {
                    sharedPrefs.setFirebaseAppCheckTokenExpiration(tokenResponse.expireTimeMillis)
                    sharedPrefs.setFirebaseAppCheckToken(appCheckToken)
                }
            }
    }

    private fun init(context: Context) {
        FirebaseApp.initializeApp(context)
        val factory = if (BuildConfig.DEBUG) {
            DebugAppCheckProviderFactory.getInstance()
        } else {
            PlayIntegrityAppCheckProviderFactory.getInstance()
        }
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(factory)
    }
}