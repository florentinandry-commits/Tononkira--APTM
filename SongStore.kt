package mg.aptm.tononkira

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class Song(val id: Long, val title: String, val lyrics: String, val audioUri: String? = null)

class SongStore(context: Context) {
    private val prefs = context.getSharedPreferences("songs", Context.MODE_PRIVATE)

    fun all(): MutableList<Song> {
        val result = mutableListOf<Song>()
        val arr = try { JSONArray(prefs.getString("items", "[]")) } catch (_: Exception) { JSONArray() }
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            result.add(Song(o.optLong("id"), o.optString("title"), o.optString("lyrics"), o.optString("audio").takeIf { it.isNotBlank() }))
        }
        return result
    }

    fun save(song: Song) {
        val list = all()
        val index = list.indexOfFirst { it.id == song.id }
        if (index >= 0) list[index] = song else list.add(song)
        persist(list)
    }

    fun delete(id: Long) = persist(all().filterNot { it.id == id })

    private fun persist(list: List<Song>) {
        val arr = JSONArray()
        list.forEach {
            arr.put(JSONObject().put("id", it.id).put("title", it.title).put("lyrics", it.lyrics).put("audio", it.audioUri ?: ""))
        }
        prefs.edit().putString("items", arr.toString()).apply()
    }
}
