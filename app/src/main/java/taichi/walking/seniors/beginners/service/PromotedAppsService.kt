package taichi.walking.seniors.beginners.service

import taichi.walking.seniors.beginners.model.PromotedApp
import taichi.walking.seniors.beginners.util.AiAssistantRemoteConfig
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import javax.inject.Inject

class PromotedAppsService @Inject constructor(
    private val remoteConfig: AiAssistantRemoteConfig,
    private val gson: Gson,
) {

    fun getPromotedApps(): List<PromotedApp> {
        val type = object : TypeToken<List<PromotedApp>>() {}.type
        return gson.fromJson(remoteConfig.getPromotedAppsJson(), type)
    }
}