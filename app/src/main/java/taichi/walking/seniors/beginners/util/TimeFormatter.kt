package taichi.walking.seniors.beginners.util

import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import org.ocpsoft.prettytime.PrettyTime

class TimeFormatter @Inject constructor() {

    /**
     * Returns localised time formatted relative to now (1 day ago, 2 days ago, etc)
     * For lower api levels the date is returned in dd-MM-yyyy format due to library limitations
     */
    fun formatTimeRelativeToNow(timestampInMillis: Long): String =
        try {
            PrettyTime().format(Date(timestampInMillis))
        } catch (t: Throwable) {
            val fmtOut = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
            fmtOut.format(timestampInMillis) ?: ""
        }

    companion object {
        private const val DATE_FORMAT = "dd-MM-yyyy"
    }
}