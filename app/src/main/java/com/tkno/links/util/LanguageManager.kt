package com.tkno.links.util

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import com.tkno.links.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.util.Locale

data class BuiltInLanguage(
    val displayName: String,
    val tag: String,
    val locale: Locale
)

data class RemoteLanguageItem(
    val code: String,
    val name: String,
    val englishName: String,
    val direction: String = "ltr",
    val file: String,
    val stringCount: Int = 0
)

data class CustomLanguage(
    val code: String,
    val name: String,
    val englishName: String,
    val direction: String = "ltr",
    val stringCount: Int = 0,
    val strings: Map<String, String> = emptyMap()
)

object LanguageManager {
    // Official GitHub raw base URL for Links language packs
    var GITHUB_BASE_URL: String = "https://raw.githubusercontent.com/hamzabellouch/language/main/Links"

    val builtInLanguages: List<BuiltInLanguage> = listOf(
        BuiltInLanguage("العربية", "ar", Locale.forLanguageTag("ar")),
        BuiltInLanguage("English", "en", Locale.ENGLISH),
        BuiltInLanguage("Français", "fr", Locale.FRENCH)
    )

    fun isBuiltInLanguage(tag: String): Boolean {
        if (tag == "system") return true
        return builtInLanguages.any { it.tag.equals(tag, ignoreCase = true) }
    }

    private fun getLanguagesDir(context: Context): File {
        val dir = File(context.filesDir, "custom_languages")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun getInstalledCustomLanguages(context: Context): List<CustomLanguage> {
        val dir = getLanguagesDir(context)
        val files = dir.listFiles { file -> file.extension == "json" } ?: return emptyList()
        val list = mutableListOf<CustomLanguage>()
        for (file in files) {
            try {
                val json = JSONObject(file.readText(Charsets.UTF_8))
                val code = json.optString("code", file.nameWithoutExtension)
                val name = json.optString("name", code)
                val englishName = json.optString("english_name", name)
                val direction = json.optString("direction", "ltr")
                val stringsObj = json.optJSONObject("strings")
                val count = stringsObj?.length() ?: 0
                list.add(
                    CustomLanguage(
                        code = code,
                        name = name,
                        englishName = englishName,
                        direction = direction,
                        stringCount = count
                    )
                )
            } catch (_: Exception) {}
        }
        return list
    }

    fun loadCustomLanguage(context: Context, code: String): CustomLanguage? {
        val dir = getLanguagesDir(context)
        val file = File(dir, "$code.json")
        if (!file.exists()) return null
        return try {
            val json = JSONObject(file.readText(Charsets.UTF_8))
            val langCode = json.optString("code", code)
            val name = json.optString("name", langCode)
            val englishName = json.optString("english_name", name)
            val direction = json.optString("direction", "ltr")
            val stringsMap = mutableMapOf<String, String>()
            val stringsObj = json.optJSONObject("strings")
            if (stringsObj != null) {
                val keys = stringsObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    stringsMap[key] = stringsObj.optString(key, "")
                }
            }
            CustomLanguage(
                code = langCode,
                name = name,
                englishName = englishName,
                direction = direction,
                stringCount = stringsMap.size,
                strings = stringsMap
            )
        } catch (_: Exception) {
            null
        }
    }

    fun saveCustomLanguage(context: Context, jsonString: String): CustomLanguage? {
        return try {
            val json = JSONObject(jsonString)
            val code = json.getString("code")
            val name = json.optString("name", code)
            val englishName = json.optString("english_name", name)
            val direction = json.optString("direction", "ltr")
            val stringsMap = mutableMapOf<String, String>()
            val stringsObj = json.optJSONObject("strings")
            if (stringsObj != null) {
                val keys = stringsObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    stringsMap[key] = stringsObj.optString(key, "")
                }
            }

            val dir = getLanguagesDir(context)
            val file = File(dir, "$code.json")
            file.writeText(jsonString, Charsets.UTF_8)

            CustomLanguage(
                code = code,
                name = name,
                englishName = englishName,
                direction = direction,
                stringCount = stringsMap.size,
                strings = stringsMap
            )
        } catch (_: Exception) {
            null
        }
    }

    fun deleteCustomLanguage(context: Context, code: String): Boolean {
        val dir = getLanguagesDir(context)
        val file = File(dir, "$code.json")
        val deleted = if (file.exists()) file.delete() else false

        // If active language was this deleted language, reset to system
        val prefs = context.getSharedPreferences("links_prefs", Context.MODE_PRIVATE)
        val currentLang = prefs.getString("app_language", "system") ?: "system"
        if (currentLang == code) {
            prefs.edit().putString("app_language", "system").apply()
        }
        return deleted
    }

    suspend fun fetchRemoteLanguages(repoBaseUrl: String? = null): Result<List<RemoteLanguageItem>> = withContext(Dispatchers.IO) {
        val base = (repoBaseUrl ?: GITHUB_BASE_URL).trimEnd('/')
        val url = "$base/languages_index.json"
        try {
            val request = Request.Builder()
                .url(url)
                .build()
            val response = HttpClient.client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP error ${response.code}"))
            }
            val body = response.body.string()
            val root = JSONObject(body)
            val array = root.getJSONArray("languages")
            val resultList = mutableListOf<RemoteLanguageItem>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val code = obj.getString("code")
                val name = obj.getString("name")
                val englishName = obj.optString("english_name", name)
                val direction = obj.optString("direction", "ltr")
                val file = obj.getString("file")
                val count = obj.optInt("string_count", 0)
                resultList.add(
                    RemoteLanguageItem(
                        code = code,
                        name = name,
                        englishName = englishName,
                        direction = direction,
                        file = file,
                        stringCount = count
                    )
                )
            }
            Result.success(resultList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun downloadRemoteLanguage(
        context: Context,
        item: RemoteLanguageItem,
        repoBaseUrl: String? = null
    ): Result<CustomLanguage> = withContext(Dispatchers.IO) {
        val base = (repoBaseUrl ?: GITHUB_BASE_URL).trimEnd('/')
        val relativeFile = item.file.trimStart('/')
        val url = "$base/$relativeFile"
        try {
            val request = Request.Builder()
                .url(url)
                .build()
            val response = HttpClient.client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP error ${response.code}"))
            }
            val body = response.body.string()
            val customLang = saveCustomLanguage(context, body)
                ?: return@withContext Result.failure(Exception("Invalid language format"))
            Result.success(customLang)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getDisplayName(context: Context, tag: String): String {
        if (tag == "system") {
            return context.getString(R.string.follow_system)
        }
        val builtIn = builtInLanguages.find { it.tag.equals(tag, ignoreCase = true) }
        if (builtIn != null) {
            return builtIn.displayName
        }
        val custom = loadCustomLanguage(context, tag)
        if (custom != null) {
            return custom.name
        }
        return tag
    }

    fun wrapContext(baseContext: Context, langTag: String): Context {
        if (langTag == "system") {
            return baseContext
        }

        val builtIn = builtInLanguages.find { it.tag.equals(langTag, ignoreCase = true) }
        if (builtIn != null) {
            val config = Configuration(baseContext.resources.configuration)
            config.setLocale(builtIn.locale)
            val localizedConfigContext = baseContext.createConfigurationContext(config)
            return object : ContextWrapper(baseContext) {
                override fun getResources(): Resources = localizedConfigContext.resources
                override fun getAssets(): android.content.res.AssetManager = localizedConfigContext.assets
            }
        }

        // Custom downloaded language
        val custom = loadCustomLanguage(baseContext, langTag)
        if (custom != null) {
            val locale = Locale.forLanguageTag(custom.code)
            val config = Configuration(baseContext.resources.configuration)
            config.setLocale(locale)
            val localizedConfigContext = baseContext.createConfigurationContext(config)
            val dynamicResources = DynamicResources(localizedConfigContext.resources, custom.strings)
            return object : ContextWrapper(baseContext) {
                override fun getResources(): Resources = dynamicResources
                override fun getAssets(): android.content.res.AssetManager = localizedConfigContext.assets
            }
        }

        return baseContext
    }
}

@Suppress("DEPRECATION")
class DynamicResources(
    private val baseResources: Resources,
    private val customStrings: Map<String, String>
) : Resources(baseResources.assets, baseResources.displayMetrics, baseResources.configuration) {

    override fun getText(id: Int): CharSequence {
        try {
            val entryName = getResourceEntryName(id)
            val custom = customStrings[entryName]
            if (!custom.isNullOrEmpty()) {
                return custom
            }
        } catch (_: Exception) {}
        return baseResources.getText(id)
    }

    override fun getText(id: Int, def: CharSequence?): CharSequence {
        try {
            val entryName = getResourceEntryName(id)
            val custom = customStrings[entryName]
            if (!custom.isNullOrEmpty()) {
                return custom
            }
        } catch (_: Exception) {}
        return baseResources.getText(id, def)
    }

    override fun getString(id: Int): String {
        try {
            val entryName = getResourceEntryName(id)
            val custom = customStrings[entryName]
            if (!custom.isNullOrEmpty()) {
                return custom
            }
        } catch (_: Exception) {}
        return baseResources.getString(id)
    }

    override fun getString(id: Int, vararg formatArgs: Any?): String {
        try {
            val entryName = getResourceEntryName(id)
            val custom = customStrings[entryName]
            if (!custom.isNullOrEmpty()) {
                return String.format(custom, *formatArgs)
            }
        } catch (_: Exception) {}
        return baseResources.getString(id, *formatArgs)
    }
}
