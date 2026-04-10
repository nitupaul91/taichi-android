package taichi.walking.seniors.beginners.ui.paywall

import android.content.Context
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import taichi.walking.seniors.beginners.util.AiAssistantRemoteConfig
import timber.log.Timber
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

enum class RemotePaywallVariant {
    PRIMARY,
    ONBOARDING_LIMITED_OFFER
}

data class CachedRemotePaywall(
    val html: String,
    val baseUrl: String?,
    val identifier: String
)

private data class RemotePaywallMetadata(
    val sourceUrl: String,
    val cachedAtEpochMillis: Long,
    val etag: String?,
    val lastModified: String?
)

@Singleton
class RemotePaywallCache @Inject constructor(
    @ApplicationContext private val context: Context,
    private val remoteConfig: AiAssistantRemoteConfig,
    private val gson: Gson
) {

    private val cachedPaywalls = enumValues<RemotePaywallVariant>().associateWith { variant ->
        MutableStateFlow(loadCachedPaywallFromDisk(variant))
    }

    fun observeCachedPaywall(variant: RemotePaywallVariant): StateFlow<CachedRemotePaywall?> {
        return cachedPaywalls.getValue(variant)
    }

    suspend fun prefetchAll() {
        prefetch(RemotePaywallVariant.PRIMARY)
        prefetch(RemotePaywallVariant.ONBOARDING_LIMITED_OFFER)
    }

    private suspend fun prefetch(variant: RemotePaywallVariant) = withContext(Dispatchers.IO) {
        val url = resolvePaywallUrl(variant)
        if (url.isNullOrBlank()) {
            purgeCache(variant)
            return@withContext
        }

        val existingMetadata = loadMetadata(variant)
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15_000
            readTimeout = 15_000
            instanceFollowRedirects = true

            if (existingMetadata?.sourceUrl == url) {
                existingMetadata.etag?.takeIf { it.isNotBlank() }?.let {
                    setRequestProperty("If-None-Match", it)
                }
                existingMetadata.lastModified?.takeIf { it.isNotBlank() }?.let {
                    setRequestProperty("If-Modified-Since", it)
                }
            }
        }

        runCatching {
            when (val statusCode = connection.responseCode) {
                HttpURLConnection.HTTP_NOT_MODIFIED -> {
                    existingMetadata?.let { metadata ->
                        persistMetadata(
                            variant = variant,
                            metadata = metadata.copy(cachedAtEpochMillis = System.currentTimeMillis())
                        )
                    }
                }

                in 200..299 -> {
                    val html = connection.inputStream.bufferedReader().use { it.readText() }
                    persistCachedPaywall(
                        variant = variant,
                        html = html,
                        sourceUrl = connection.url?.toString() ?: url,
                        etag = connection.getHeaderField("ETag"),
                        lastModified = connection.getHeaderField("Last-Modified")
                    )
                }

                else -> {
                    Timber.w("Paywall HTML fetch failed for %s with status %s", variant.name, statusCode)
                }
            }
        }.onFailure { error ->
            Timber.e(error, "Paywall HTML fetch failed for %s", variant.name)
        }

        connection.disconnect()
    }

    private fun resolvePaywallUrl(variant: RemotePaywallVariant): String? {
        val androidPaywallUrl = remoteConfig.getAndroidPaywallHtmlUrl()
        val rawUrl = when (variant) {
            RemotePaywallVariant.PRIMARY -> {
                androidPaywallUrl.ifBlank { remoteConfig.getPaywallHtmlUrl() }
            }
            RemotePaywallVariant.ONBOARDING_LIMITED_OFFER -> {
                remoteConfig.getLimitedOfferPaywallHtmlUrl()
                    .ifBlank { androidPaywallUrl }
                    .ifBlank { remoteConfig.getPaywallHtmlUrl() }
            }
        }
        return rawUrl.takeIf { it.isNotBlank() }
    }

    private fun persistCachedPaywall(
        variant: RemotePaywallVariant,
        html: String,
        sourceUrl: String,
        etag: String?,
        lastModified: String?
    ) {
        val directory = cacheDirectory(variant)
        if (!directory.exists()) {
            directory.mkdirs()
        }

        cacheFile(variant).writeText(html)
        val metadata = RemotePaywallMetadata(
            sourceUrl = sourceUrl,
            cachedAtEpochMillis = System.currentTimeMillis(),
            etag = etag,
            lastModified = lastModified
        )
        persistMetadata(variant, metadata)

        cachedPaywalls.getValue(variant).value = CachedRemotePaywall(
            html = html,
            baseUrl = sourceUrl,
            identifier = "${metadata.sourceUrl}#${metadata.cachedAtEpochMillis}"
        )
    }

    private fun persistMetadata(variant: RemotePaywallVariant, metadata: RemotePaywallMetadata) {
        val directory = cacheDirectory(variant)
        if (!directory.exists()) {
            directory.mkdirs()
        }
        metadataFile(variant).writeText(gson.toJson(metadata))
    }

    private fun purgeCache(variant: RemotePaywallVariant) {
        cacheFile(variant).delete()
        metadataFile(variant).delete()
        cachedPaywalls.getValue(variant).value = null
    }

    private fun loadCachedPaywallFromDisk(variant: RemotePaywallVariant): CachedRemotePaywall? {
        val htmlFile = cacheFile(variant)
        if (!htmlFile.exists()) {
            return null
        }

        val html = runCatching { htmlFile.readText() }.getOrNull() ?: return null
        val metadata = loadMetadata(variant)
        val identifierSource = metadata?.sourceUrl ?: htmlFile.absolutePath
        val modifiedAt = metadata?.cachedAtEpochMillis ?: htmlFile.lastModified()
        return CachedRemotePaywall(
            html = html,
            baseUrl = metadata?.sourceUrl,
            identifier = "$identifierSource#$modifiedAt"
        )
    }

    private fun loadMetadata(variant: RemotePaywallVariant): RemotePaywallMetadata? {
        val file = metadataFile(variant)
        if (!file.exists()) {
            return null
        }
        return runCatching {
            gson.fromJson(file.readText(), RemotePaywallMetadata::class.java)
        }.getOrNull()
    }

    private fun cacheDirectory(variant: RemotePaywallVariant): File {
        val directoryName = when (variant) {
            RemotePaywallVariant.PRIMARY -> "paywall_cache"
            RemotePaywallVariant.ONBOARDING_LIMITED_OFFER -> "paywall_cache_limited_offer"
        }
        return File(context.cacheDir, directoryName)
    }

    private fun cacheFile(variant: RemotePaywallVariant): File {
        return File(cacheDirectory(variant), "cached_paywall.html")
    }

    private fun metadataFile(variant: RemotePaywallVariant): File {
        return File(cacheDirectory(variant), "cached_paywall_metadata.json")
    }
}
