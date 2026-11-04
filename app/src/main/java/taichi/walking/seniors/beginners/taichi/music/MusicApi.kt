package taichi.walking.seniors.beginners.taichi.music

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET

/**
 * Catalog of workout background-music tracks. The tracks used to be bundled in the APK (~11 MB);
 * they now live on the CDN and the server owns the list (`MusicController` backend-side), so
 * tracks can be added or retired without an app release. Ids match [WorkoutMusicTrack.id]; the
 * app ignores ids it does not know.
 */
interface MusicApi {

    @GET("music/tracks")
    suspend fun getTracks(): List<MusicTrackDto>
}

// @SerializedName on every field: Gson serializes by reflection and release R8 renames
// unannotated fields, which silently breaks the wire contract in release builds only.
data class MusicTrackDto(
    @SerializedName("id") val id: String,
    @SerializedName("url") val url: String,
)
