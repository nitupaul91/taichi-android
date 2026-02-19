package taichi.walking.seniors.beginners.util

import com.mobteq.billing.datastore.DataStorePrefs
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class RateAppDialogHandler @Inject constructor(
    private val dataStorePrefs: DataStorePrefs

) {

    suspend fun showEnjoyAppDialog(): Boolean {
        if (!dataStorePrefs.shouldShowEnjoyAppDialog().first()) {
            return false
        }
        dataStorePrefs.increaseEnjoyAppDialogCount()
        val count = dataStorePrefs.getEnjoyAppDialogCount().first() ?: 1
        if (count > 30) {
            dataStorePrefs.changeEnjoyAppDialogStatus(false)
            return false
        }
        return setOf(1, 2, 3, 5, 9, 14, 29).contains(count)
    }

}