package com.tkno.links

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Patterns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URI

object LinkResolver {

    sealed class Result {
        data class Success(val destination: String, val source: String) : Result()
        data class Error(val messageResId: Int? = null, val fallbackMessage: String? = null) : Result() {
            fun getLocalizedMessage(context: Context): String {
                return if (messageResId != null) {
                    context.getString(messageResId)
                } else {
                    fallbackMessage ?: context.getString(R.string.unknown_error)
                }
            }
        }
    }

    /**
     * Checks if active internet connection is available on the device.
     */
    fun isNetworkAvailable(context: Context): Boolean {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            if (connectivityManager != null) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    val network = connectivityManager.activeNetwork ?: return false
                    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ||
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                } else {
                    @Suppress("DEPRECATION")
                    val activeNetworkInfo = connectivityManager.activeNetworkInfo
                    @Suppress("DEPRECATION")
                    activeNetworkInfo != null && activeNetworkInfo.isConnected
                }
            } else {
                true
            }
        } catch (e: Exception) {
            true
        }
    }

    /**
     * Checks if the given string is a valid web URL format.
     */
    fun isValidUrl(url: String): Boolean {
        val trimmed = url.trim()
        if (trimmed.isEmpty() || trimmed.contains(" ")) return false
        val urlToTest = if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            "https://$trimmed"
        } else {
            trimmed
        }
        return try {
            val uri = URI(urlToTest)
            val host = uri.host
            val isValidHost = host != null && host.contains(".") && host.split(".").all { it.isNotEmpty() }
            isValidHost || Patterns.WEB_URL.matcher(urlToTest).matches()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Resolves short links by following HTTP redirects on the IO thread.
     * Returns a [Result] containing success URLs or an error message.
     */
    suspend fun resolveDetailed(context: Context, shortUrl: String): Result = withContext(Dispatchers.IO) {
        val trimmedInput = shortUrl.trim()
        if (!isValidUrl(trimmedInput)) {
            return@withContext Result.Error(messageResId = R.string.invalid_url_format)
        }

        if (!isNetworkAvailable(context)) {
            return@withContext Result.Error(messageResId = R.string.no_internet_error)
        }

        var currentUrl = if (!trimmedInput.startsWith("http://") && !trimmedInput.startsWith("https://")) {
            "https://$trimmedInput"
        } else {
            trimmedInput
        }

        var redirects = 0
        val maxRedirects = 10
        val userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

        try {
            while (redirects < maxRedirects) {
                val urlObj = URI(currentUrl).toURL()
                val connection = urlObj.openConnection() as HttpURLConnection
                try {
                    connection.requestMethod = "HEAD"
                    connection.connectTimeout = 5000
                    connection.readTimeout = 5000
                    connection.instanceFollowRedirects = false
                    connection.setRequestProperty("User-Agent", userAgent)

                    val responseCode = connection.responseCode
                    if (responseCode in 300..399) {
                        val location = connection.getHeaderField("Location")
                        if (location != null) {
                            val nextUrl = URI(urlObj.toString()).resolve(location).toString()
                            if (nextUrl == currentUrl) {
                                break
                            }
                            currentUrl = nextUrl
                            redirects++
                        } else {
                            break
                        }
                    } else {
                        break
                    }
                } finally {
                    connection.disconnect()
                }
            }
            val formattedSource = if (trimmedInput.startsWith("http://") || trimmedInput.startsWith("https://")) {
                trimmedInput
            } else {
                "https://$trimmedInput"
            }
            Result.Success(destination = currentUrl, source = formattedSource)
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            e.printStackTrace()
            // Fallback to GET request with followRedirects = true if HEAD fails or is blocked
            try {
                val urlObj = URI(currentUrl).toURL()
                val connection = urlObj.openConnection() as HttpURLConnection
                try {
                    connection.requestMethod = "GET"
                    connection.connectTimeout = 5000
                    connection.readTimeout = 5000
                    connection.instanceFollowRedirects = true
                    connection.setRequestProperty("User-Agent", userAgent)
                    connection.connect()
                    val finalUrl = connection.url.toString()
                    val formattedSource = if (trimmedInput.startsWith("http://") || trimmedInput.startsWith("https://")) {
                        trimmedInput
                    } else {
                        "https://$trimmedInput"
                    }
                    Result.Success(destination = finalUrl, source = formattedSource)
                } finally {
                    connection.disconnect()
                }
            } catch (ex: Exception) {
                if (ex is kotlinx.coroutines.CancellationException) throw ex
                ex.printStackTrace()
                if (!isNetworkAvailable(context)) {
                    Result.Error(messageResId = R.string.no_internet_error)
                } else {
                    val errorResId = when (ex) {
                        is java.net.UnknownHostException -> R.string.cannot_resolve_domain
                        is java.net.SocketTimeoutException -> R.string.connection_timeout
                        else -> null
                    }
                    if (errorResId != null) {
                        Result.Error(messageResId = errorResId)
                    } else {
                        Result.Error(fallbackMessage = ex.message ?: "Connection error")
                    }
                }
            }
        }
    }

    /**
     * Resolves short links by following HTTP redirects on the IO thread.
     * Returns the final destination URL or empty string.
     */
    suspend fun resolve(context: Context, shortUrl: String): String {
        return when (val result = resolveDetailed(context, shortUrl)) {
            is Result.Success -> result.destination
            is Result.Error -> ""
        }
    }
}

