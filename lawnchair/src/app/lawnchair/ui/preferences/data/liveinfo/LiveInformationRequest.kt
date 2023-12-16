package app.lawnchair.ui.preferences.data.liveinfo


import android.util.Log
import app.lawnchair.util.kotlinxJson
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.create

private val retrofit = Retrofit.Builder()
    .baseUrl("https://lawnchair.app/")
    .addConverterFactory(kotlinxJson.asConverterFactory("application/json".toMediaType()))
    .build()

val liveInformationService: LiveInformationService = retrofit.create()

suspend fun getLiveInformation(): LiveInformation? = withContext(Dispatchers.IO) {
    try {
        val response: Response<ResponseBody> = liveInformationService.getLiveInformation()

        if (response.isSuccessful) {
            val responseBody = response.body()?.string() ?: return@withContext null

            val liveInformationObject = JSONObject(responseBody)
            val announcements: List<LiveInformation.Announcement> =
                parseAnnouncements(liveInformationObject)

            val liveInformation = LiveInformation(
                announcements = announcements,
            )

            Log.v("LiveInformation", "getLiveInformation: $liveInformation")
            return@withContext liveInformation
        } else {
            Log.d("LiveInformation", ": response code ${response.code()}")
            return@withContext null
        }
    } catch (e: Exception) {
        Log.e("LiveInformation", "Error during news retrieval: ${e.message}")
        return@withContext null
    }
}

private fun parseAnnouncements(
    liveInformationObject: JSONObject,
): List<LiveInformation.Announcement> =
    liveInformationObject
        .getJSONArray("announcements").let { array ->
            (0 until array.length())
                .map { array.getJSONObject(it) }
                .map {
                    LiveInformation.Announcement(
                        text = it.getString("text"),
                        url = it.getString("url"),
                        isActive = it.getBoolean("active"),
                    )
                }
        }
