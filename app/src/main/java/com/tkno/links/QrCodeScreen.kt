package com.tkno.links

import android.Manifest
import android.graphics.Bitmap
import android.widget.Toast
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.SelectAll
import androidx.compose.material.icons.outlined.Deselect
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.FlashOff
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.ContentPaste
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSuggestion
import android.provider.Settings
import android.os.Bundle
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateRectAsState
import androidx.compose.animation.core.spring
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.res.stringResource
import com.tkno.links.R
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.compose.material.icons.outlined.Image
import com.tkno.links.ui.icon.QrCodeIcon
import com.tkno.links.ui.icon.CenterFocusWeak
import com.tkno.links.ui.icon.PictureAsPdf
import androidx.compose.material.icons.outlined.Cameraswitch
import androidx.compose.material.icons.outlined.ZoomIn
import androidx.compose.material.icons.outlined.ZoomOut
import androidx.compose.material.icons.outlined.QrCodeScanner
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import android.content.Intent
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.GridOn
import com.tkno.links.ui.icon.Delete
import androidx.compose.ui.text.style.TextOverflow
import androidx.activity.compose.BackHandler
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.tkno.links.ui.component.BackButton
import com.tkno.links.util.PdfQrDecoder
import com.tkno.links.util.PdfQrResult
import kotlinx.coroutines.launch

fun decodeQrFromUri(context: Context, uri: Uri): String? {
    return try {
        val bitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
            android.graphics.BitmapFactory.decodeStream(stream)
        } ?: return null

        val results = PdfQrDecoder.decodeQrsFromBitmap(bitmap)
        bitmap.recycle()
        results.firstOrNull()
    } catch (e: Exception) {
        null
    }
}

data class WifiInfo(
    val ssid: String,
    val password: String,
    val type: String,
    val isHidden: Boolean = false
)

fun parseWifiQr(content: String): WifiInfo? {
    val trimmed = content.trim()
    if (!trimmed.startsWith("WIFI:", ignoreCase = true)) return null
    val raw = trimmed.substring(5)

    var ssid = ""
    var password = ""
    var type = "WPA/WPA2"
    var isHidden = false

    val tokens = raw.split(";")
    for (token in tokens) {
        val colonIndex = token.indexOf(':')
        if (colonIndex != -1) {
            val key = token.substring(0, colonIndex).trim().uppercase()
            val value = token.substring(colonIndex + 1).trim()
            when (key) {
                "S" -> ssid = value
                "P" -> password = value
                "T" -> type = when (value.uppercase()) {
                    "WPA", "WPA2" -> "WPA/WPA2"
                    "WPA3" -> "WPA3"
                    "WEP" -> "WEP"
                    "NOPASS", "" -> "Open"
                    else -> value
                }
                "H" -> isHidden = value.equals("true", ignoreCase = true)
            }
        }
    }
    return if (ssid.isNotEmpty()) WifiInfo(ssid, password, type, isHidden) else null
}

fun detectQrContentType(content: String): String {
    val trimmed = content.trim()
    if (trimmed.isEmpty()) return "Text"
    if (trimmed.startsWith("WIFI:", ignoreCase = true)) return "Wi-Fi"
    if (LinkResolver.isValidUrl(trimmed)) return "URL"
    if (trimmed.all { it.isDigit() || it == '+' || it == ' ' || it == '-' || it == '(' || it == ')' } && trimmed.any { it.isDigit() }) {
        return "Numbers"
    }
    return "Text"
}

fun connectToWifi(context: Context, wifiInfo: WifiInfo) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val suggestionBuilder = WifiNetworkSuggestion.Builder()
                .setSsid(wifiInfo.ssid)
                .setIsHiddenSsid(wifiInfo.isHidden)

            when {
                wifiInfo.type.contains("WPA3", ignoreCase = true) -> {
                    if (wifiInfo.password.isNotEmpty()) {
                        suggestionBuilder.setWpa3Passphrase(wifiInfo.password)
                    }
                }
                wifiInfo.type.contains("WPA", ignoreCase = true) -> {
                    if (wifiInfo.password.isNotEmpty()) {
                        suggestionBuilder.setWpa2Passphrase(wifiInfo.password)
                    }
                }
            }

            val suggestions = listOf(suggestionBuilder.build())
            val bundle = Bundle().apply {
                putParcelableArrayList(Settings.EXTRA_WIFI_NETWORK_LIST, ArrayList(suggestions))
            }
            val addNetIntent = Intent(Settings.ACTION_WIFI_ADD_NETWORKS).apply {
                putExtras(bundle)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            if (addNetIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(addNetIntent)
            } else {
                val panelIntent = android.content.Intent(Settings.Panel.ACTION_WIFI).apply {
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (panelIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(panelIntent)
                } else {
                    context.startActivity(android.content.Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                    })
                }
            }
        } else {
            context.startActivity(android.content.Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        }
    } catch (e: Exception) {
        try {
            context.startActivity(android.content.Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (ex: Exception) {
            Toast.makeText(context, "Could not open Wi-Fi settings", Toast.LENGTH_SHORT).show()
        }
    }
}

data class ScanHistoryItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
) {
    val isWifi: Boolean
        get() = content.trim().startsWith("WIFI:", ignoreCase = true)

    val isUrl: Boolean
        get() = !isWifi && LinkResolver.isValidUrl(content)

    val isNumbers: Boolean
        get() {
            if (isWifi || isUrl) return false
            val trimmed = content.trim()
            return trimmed.isNotEmpty() && trimmed.all { it.isDigit() || it == '+' || it == ' ' || it == '-' || it == '(' || it == ')' } && trimmed.any { it.isDigit() }
        }

    val typeLabel: String
        get() = when {
            isWifi -> "Wi-Fi"
            isUrl -> "URL"
            isNumbers -> "Numbers"
            else -> "Text"
        }
}

enum class ExportFormat(val extension: String, val mimeType: String) {
    TXT("txt", "text/plain"),
    CSV("csv", "text/csv"),
    JSON("json", "application/json"),
    XLSX("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
}

enum class HistoryMenuState {
    Root,
    ExportAs
}

object ScanHistoryManager {
    private const val PREFS_NAME = "scan_history_prefs"
    private const val KEY_HISTORY = "scan_history_list"

    fun getHistoryItems(context: Context): List<ScanHistoryItem> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonString = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        return try {
            val jsonArray = org.json.JSONArray(jsonString)
            val list = mutableListOf<ScanHistoryItem>()
            for (i in 0 until jsonArray.length()) {
                val elem = jsonArray.opt(i)
                if (elem is org.json.JSONObject) {
                    val id = elem.optString("id", java.util.UUID.randomUUID().toString())
                    val content = elem.optString("content", "")
                    val timestamp = elem.optLong("timestamp", System.currentTimeMillis() - i * 60000L)
                    val isFavorite = elem.optBoolean("isFavorite", false)
                    if (content.isNotBlank()) {
                        list.add(ScanHistoryItem(id = id, content = content, timestamp = timestamp, isFavorite = isFavorite))
                    }
                } else if (elem != null) {
                    val content = elem.toString()
                    if (content.isNotBlank()) {
                        list.add(
                            ScanHistoryItem(
                                content = content,
                                timestamp = System.currentTimeMillis() - i * 60000L,
                                isFavorite = false
                            )
                        )
                    }
                }
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getHistory(context: Context): List<String> {
        return getHistoryItems(context).map { it.content }
    }

    fun saveHistoryItems(context: Context, items: List<ScanHistoryItem>) {
        val jsonArray = org.json.JSONArray()
        items.forEach { item ->
            val obj = org.json.JSONObject()
            obj.put("id", item.id)
            obj.put("content", item.content)
            obj.put("timestamp", item.timestamp)
            obj.put("isFavorite", item.isFavorite)
            jsonArray.put(obj)
        }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_HISTORY, jsonArray.toString()).apply()
    }

    fun addHistoryItem(context: Context, content: String) {
        if (content.isBlank()) return
        val current = getHistoryItems(context).toMutableList()
        val existingIndex = current.indexOfFirst { it.content == content }
        val wasFavorite = if (existingIndex >= 0) current[existingIndex].isFavorite else false
        if (existingIndex >= 0) {
            current.removeAt(existingIndex)
        }
        val newItem = ScanHistoryItem(
            content = content,
            timestamp = System.currentTimeMillis(),
            isFavorite = wasFavorite
        )
        current.add(0, newItem)
        if (current.size > 100) current.removeAt(current.lastIndex)

        saveHistoryItems(context, current)
    }

    fun toggleFavorite(context: Context, id: String): List<ScanHistoryItem> {
        val current = getHistoryItems(context).toMutableList()
        val index = current.indexOfFirst { it.id == id }
        if (index >= 0) {
            val item = current[index]
            current[index] = item.copy(isFavorite = !item.isFavorite)
            saveHistoryItems(context, current)
        }
        return current
    }

    fun deleteHistoryItem(context: Context, id: String): List<ScanHistoryItem> {
        val current = getHistoryItems(context).toMutableList()
        current.removeAll { it.id == id }
        saveHistoryItems(context, current)
        return current
    }

    fun deleteHistoryItems(context: Context, ids: Set<String>): List<ScanHistoryItem> {
        val current = getHistoryItems(context).toMutableList()
        current.removeAll { it.id in ids }
        saveHistoryItems(context, current)
        return current
    }

    fun clearHistory(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_HISTORY).apply()
    }

    fun importHistoryItems(context: Context, newItems: List<String>): Int {
        val validItems = newItems.map { it.trim() }.filter { it.isNotBlank() }
        if (validItems.isEmpty()) return 0

        val current = getHistoryItems(context).toMutableList()
        var addedCount = 0
        for (itemContent in validItems.reversed()) {
            val existingIndex = current.indexOfFirst { it.content == itemContent }
            if (existingIndex < 0) {
                addedCount++
                current.add(
                    0,
                    ScanHistoryItem(
                        content = itemContent,
                        timestamp = System.currentTimeMillis(),
                        isFavorite = false
                    )
                )
            } else {
                val existing = current.removeAt(existingIndex)
                current.add(0, existing.copy(timestamp = System.currentTimeMillis()))
            }
        }
        while (current.size > 100) {
            current.removeAt(current.lastIndex)
        }

        saveHistoryItems(context, current)
        return if (addedCount > 0) addedCount else validItems.size
    }

    fun exportHistoryToUri(context: Context, uri: Uri, format: ExportFormat, history: List<ScanHistoryItem>): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                when (format) {
                    ExportFormat.TXT -> outputStream.write(exportToTxt(history).toByteArray(Charsets.UTF_8))
                    ExportFormat.CSV -> outputStream.write(exportToCsv(history).toByteArray(Charsets.UTF_8))
                    ExportFormat.JSON -> outputStream.write(exportToJson(history).toByteArray(Charsets.UTF_8))
                    ExportFormat.XLSX -> outputStream.write(exportToXlsx(history))
                }
                outputStream.flush()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun exportToTxt(history: List<ScanHistoryItem>): String {
        return history.joinToString(separator = "\n") { it.content }
    }

    fun exportToCsv(history: List<ScanHistoryItem>): String {
        val sb = StringBuilder()
        sb.append("Index,Type,Date,Content,Favorite\n")
        val dateFormatter = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US)
        history.forEachIndexed { index, item ->
            val type = item.typeLabel
            val date = dateFormatter.format(java.util.Date(item.timestamp))
            val escaped = "\"" + item.content.replace("\"", "\"\"") + "\""
            val fav = if (item.isFavorite) "Yes" else "No"
            sb.append("${index + 1},$type,$date,$escaped,$fav\n")
        }
        return sb.toString()
    }

    fun exportToJson(history: List<ScanHistoryItem>): String {
        val sb = StringBuilder()
        sb.append("[\n")
        val dateFormatter = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US)
        history.forEachIndexed { index, item ->
            val escapedContent = escapeJson(item.content)
            val date = dateFormatter.format(java.util.Date(item.timestamp))
            sb.append("  {\n")
            sb.append("    \"id\": \"${item.id}\",\n")
            sb.append("    \"type\": \"${item.typeLabel}\",\n")
            sb.append("    \"date\": \"$date\",\n")
            sb.append("    \"content\": \"$escapedContent\",\n")
            sb.append("    \"favorite\": ${item.isFavorite}\n")
            sb.append("  }")
            if (index < history.size - 1) {
                sb.append(",")
            }
            sb.append("\n")
        }
        sb.append("]")
        return sb.toString()
    }

    fun exportToXlsx(history: List<ScanHistoryItem>): ByteArray {
        val baos = java.io.ByteArrayOutputStream()
        val zos = java.util.zip.ZipOutputStream(baos)

        fun addEntry(path: String, content: String) {
            val entry = java.util.zip.ZipEntry(path)
            zos.putNextEntry(entry)
            zos.write(content.toByteArray(Charsets.UTF_8))
            zos.closeEntry()
        }

        addEntry(
            "[Content_Types].xml",
            """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
  <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
  <Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
</Types>"""
        )

        addEntry(
            "_rels/.rels",
            """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>"""
        )

        addEntry(
            "xl/_rels/workbook.xml.rels",
            """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
</Relationships>"""
        )

        addEntry(
            "xl/workbook.xml",
            """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <sheets>
    <sheet name="Scan History" sheetId="1" r:id="rId1"/>
  </sheets>
</workbook>"""
        )

        addEntry(
            "xl/styles.xml",
            """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
  <fonts count="1"><font><sz val="11"/><name val="Calibri"/></font></fonts>
  <fills count="2"><fill><patternFill patternType="none"/></fill><fill><patternFill patternType="gray125"/></fill></fills>
  <borders count="1"><border><left/><right/><top/><bottom/><diagonal/></border></borders>
  <cellStyleXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0"/></cellStyleXfs>
  <cellXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/></cellXfs>
</styleSheet>"""
        )

        val sheetRows = StringBuilder()
        sheetRows.append("""<row r="1"><c r="A1" t="inlineStr"><is><t>#</t></is></c><c r="B1" t="inlineStr"><is><t>Type</t></is></c><c r="C1" t="inlineStr"><is><t>Date</t></is></c><c r="D1" t="inlineStr"><is><t>Scanned Content</t></is></c><c r="E1" t="inlineStr"><is><t>Favorite</t></is></c></row>""")
        val dateFormatter = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US)
        history.forEachIndexed { index, item ->
            val rowNum = index + 2
            val type = item.typeLabel
            val date = dateFormatter.format(java.util.Date(item.timestamp))
            val escaped = escapeXml(item.content)
            val fav = if (item.isFavorite) "Yes" else "No"
            sheetRows.append("""<row r="$rowNum"><c r="A$rowNum"><v>${index + 1}</v></c><c r="B$rowNum" t="inlineStr"><is><t>$type</t></is></c><c r="C$rowNum" t="inlineStr"><is><t>$date</t></is></c><c r="D$rowNum" t="inlineStr"><is><t xml:space="preserve">$escaped</t></is></c><c r="E$rowNum" t="inlineStr"><is><t>$fav</t></is></c></row>""")
        }

        addEntry(
            "xl/worksheets/sheet1.xml",
            """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
  <sheetData>
    $sheetRows
  </sheetData>
</worksheet>"""
        )

        zos.finish()
        zos.close()
        return baos.toByteArray()
    }

    fun parseJsonString(content: String): List<String> {
        val items = mutableListOf<String>()
        if (content.startsWith("[") && content.endsWith("]")) {
            val contentPropPattern = java.util.regex.Pattern.compile("\"content\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"")
            val propMatcher = contentPropPattern.matcher(content)
            while (propMatcher.find()) {
                val raw = propMatcher.group(1) ?: ""
                val unescaped = unescapeJson(raw)
                if (unescaped.isNotBlank()) {
                    items.add(unescaped)
                }
            }
            if (items.isEmpty()) {
                val pattern = java.util.regex.Pattern.compile("\"((?:\\\\.|[^\"\\\\])*)\"")
                val matcher = pattern.matcher(content)
                while (matcher.find()) {
                    val raw = matcher.group(1) ?: ""
                    val unescaped = unescapeJson(raw)
                    if (unescaped.isNotBlank()) {
                        items.add(unescaped)
                    }
                }
            }
        }
        return items
    }

    fun importHistoryFromUri(context: Context, uri: Uri): Int {
        return try {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return 0
            val items = mutableListOf<String>()

            if (bytes.size >= 4 && bytes[0] == 0x50.toByte() && bytes[1] == 0x4B.toByte()) {
                items.addAll(parseXlsxBytes(bytes))
            } else {
                val content = String(bytes, java.nio.charset.StandardCharsets.UTF_8).trim()
                val jsonItems = parseJsonString(content)
                if (jsonItems.isNotEmpty()) {
                    items.addAll(jsonItems)
                } else {
                    val lines = content.lines().map { it.trim() }.filter { it.isNotBlank() }
                    for (line in lines) {
                        val csvMatch = Regex("""^\d+\s*,\s*\"?(.*?)\"?$""").matchEntire(line)
                        if (csvMatch != null) {
                            val extracted = csvMatch.groupValues[1].replace("\"\"", "\"").trim()
                            if (extracted.isNotBlank() &&
                                !extracted.equals("content", ignoreCase = true) &&
                                !extracted.equals("scanned content", ignoreCase = true)) {
                                items.add(extracted)
                                continue
                            }
                        }
                        if (line.equals("Index,Content", ignoreCase = true) ||
                            line.equals("Index,Scanned Content", ignoreCase = true) ||
                            line.equals("#,Content", ignoreCase = true) ||
                            line.equals("Content", ignoreCase = true) ||
                            line.equals("Scanned Content", ignoreCase = true)) {
                            continue
                        }
                        var item = line
                        if (item.startsWith("\"") && item.endsWith("\"") && item.length >= 2) {
                            item = item.substring(1, item.length - 1).replace("\"\"", "\"")
                        }
                        if (item.isNotBlank()) {
                            items.add(item)
                        }
                    }
                }
            }

            importHistoryItems(context, items)
        } catch (e: Exception) {
            0
        }
    }

    fun parseXlsxBytes(bytes: ByteArray): List<String> {
        val items = mutableListOf<String>()
        try {
            val sharedStrings = mutableListOf<String>()
            val bais = java.io.ByteArrayInputStream(bytes)
            val zis = java.util.zip.ZipInputStream(bais)
            var entry = zis.nextEntry
            val files = mutableMapOf<String, ByteArray>()
            while (entry != null) {
                val name = entry.name
                val out = java.io.ByteArrayOutputStream()
                zis.copyTo(out)
                files[name] = out.toByteArray()
                entry = zis.nextEntry
            }
            zis.close()

            val sharedXmlBytes = files["xl/sharedStrings.xml"]
            if (sharedXmlBytes != null) {
                val xml = String(sharedXmlBytes, java.nio.charset.StandardCharsets.UTF_8)
                val matcher = java.util.regex.Pattern.compile("<t[^>]*>(.*?)</t>", java.util.regex.Pattern.DOTALL).matcher(xml)
                while (matcher.find()) {
                    sharedStrings.add(unescapeXml(matcher.group(1) ?: ""))
                }
            }

            val sheetBytes = files["xl/worksheets/sheet1.xml"]
            if (sheetBytes != null) {
                val xml = String(sheetBytes, java.nio.charset.StandardCharsets.UTF_8)
                val rowMatcher = java.util.regex.Pattern.compile("<row[^>]*>(.*?)</row>", java.util.regex.Pattern.DOTALL).matcher(xml)
                while (rowMatcher.find()) {
                    val rowXml = rowMatcher.group(1) ?: ""
                    val cellMatcher = java.util.regex.Pattern.compile("<c([^>]*)>(.*?)</c>", java.util.regex.Pattern.DOTALL).matcher(rowXml)
                    val rowCells = mutableListOf<String>()
                    while (cellMatcher.find()) {
                        val attrs = cellMatcher.group(1) ?: ""
                        val cellBody = cellMatcher.group(2) ?: ""

                        val isShared = attrs.contains("t=\"s\"") || attrs.contains("t='s'")
                        val tMatcher = java.util.regex.Pattern.compile("<t[^>]*>(.*?)</t>", java.util.regex.Pattern.DOTALL).matcher(cellBody)
                        var cellVal = ""
                        if (tMatcher.find()) {
                            cellVal = unescapeXml(tMatcher.group(1) ?: "")
                        } else {
                            val vMatcher = java.util.regex.Pattern.compile("<v>(.*?)</v>", java.util.regex.Pattern.DOTALL).matcher(cellBody)
                            if (vMatcher.find()) {
                                val vStr = vMatcher.group(1) ?: ""
                                if (isShared) {
                                    val idx = vStr.toIntOrNull()
                                    if (idx != null && idx in sharedStrings.indices) {
                                        cellVal = sharedStrings[idx]
                                    }
                                } else {
                                    cellVal = unescapeXml(vStr)
                                }
                            }
                        }
                        rowCells.add(cellVal.trim())
                    }

                    val value = when {
                        rowCells.size >= 4 -> rowCells[3]
                        rowCells.size >= 2 -> rowCells[1]
                        else -> rowCells.firstOrNull() ?: ""
                    }
                    if (value.isNotBlank() &&
                        !value.equals("Scanned Content", ignoreCase = true) &&
                        !value.equals("Content", ignoreCase = true) &&
                        !value.equals("#", ignoreCase = true)) {
                        items.add(value)
                    }
                }
            }
        } catch (e: Exception) {}
        return items
    }

    private fun escapeXml(str: String): String {
        return str.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    private fun unescapeXml(str: String): String {
        return str.replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
    }

    private fun escapeJson(str: String): String {
        val sb = StringBuilder()
        for (c in str) {
            when (c) {
                '\\' -> sb.append("\\\\")
                '"' -> sb.append("\\\"")
                '\b' -> sb.append("\\b")
                '\u000C' -> sb.append("\\f")
                '\n' -> sb.append("\\n")
                '\r' -> sb.append("\\r")
                '\t' -> sb.append("\\t")
                else -> {
                    if (c.code < 0x20) {
                        sb.append(String.format("\\u%04x", c.code))
                    } else {
                        sb.append(c)
                    }
                }
            }
        }
        return sb.toString()
    }

    private fun unescapeJson(str: String): String {
        val sb = StringBuilder()
        var i = 0
        while (i < str.length) {
            val c = str[i]
            if (c == '\\' && i + 1 < str.length) {
                when (val next = str[i + 1]) {
                    '"' -> { sb.append('"'); i += 2 }
                    '\\' -> { sb.append('\\'); i += 2 }
                    '/' -> { sb.append('/'); i += 2 }
                    'b' -> { sb.append('\b'); i += 2 }
                    'f' -> { sb.append('\u000C'); i += 2 }
                    'n' -> { sb.append('\n'); i += 2 }
                    'r' -> { sb.append('\r'); i += 2 }
                    't' -> { sb.append('\t'); i += 2 }
                    'u' -> {
                        if (i + 5 < str.length) {
                            val hex = str.substring(i + 2, i + 6)
                            val code = hex.toIntOrNull(16)
                            if (code != null) {
                                sb.append(code.toChar())
                                i += 6
                            } else {
                                sb.append("\\u")
                                i += 2
                            }
                        } else {
                            sb.append("\\u")
                            i += 2
                        }
                    }
                    else -> { sb.append(next); i += 2 }
                }
            } else {
                sb.append(c)
                i++
            }
        }
        return sb.toString()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanHistoryPage(
    historyList: List<ScanHistoryItem>,
    onHistoryUpdated: (List<ScanHistoryItem>) -> Unit = {},
    onClearHistory: () -> Unit = {},
    onNavigateToSecurity: (String) -> Unit = {},
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val softBlue = MaterialTheme.colorScheme.primary
    val textMuted = MaterialTheme.colorScheme.onSurfaceVariant
    val borderGrey = MaterialTheme.colorScheme.outlineVariant

    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf(setOf<String>()) }
    var isFavoritesFilterActive by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    var menuState by remember { mutableStateOf(HistoryMenuState.Root) }
    var pendingExportFormat by remember { mutableStateOf<ExportFormat?>(null) }

    BackHandler(enabled = isSelectionMode) {
        isSelectionMode = false
        selectedIds = emptySet()
    }

    val displayedList = remember(historyList, isFavoritesFilterActive) {
        if (isFavoritesFilterActive) historyList.filter { it.isFavorite } else historyList
    }

    val dateFormatter = remember { java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()) }
    val timeFormatter = remember { java.text.SimpleDateFormat("M/d/yy HH:mm:ss", java.util.Locale.getDefault()) }

    val groupedItems = remember(displayedList) {
        displayedList.groupBy { item ->
            dateFormatter.format(java.util.Date(item.timestamp))
        }
    }

    val exportFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("*/*")
    ) { uri: Uri? ->
        val format = pendingExportFormat
        pendingExportFormat = null
        if (uri != null && format != null) {
            val success = ScanHistoryManager.exportHistoryToUri(context, uri, format, historyList)
            if (success) {
                Toast.makeText(
                    context,
                    context.getString(R.string.history_exported_successfully),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    context,
                    context.getString(R.string.failed_to_export),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    val importFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val count = ScanHistoryManager.importHistoryFromUri(context, uri)
            if (count > 0) {
                onHistoryUpdated(ScanHistoryManager.getHistoryItems(context))
                Toast.makeText(
                    context,
                    context.getString(R.string.history_imported_successfully, count),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    context,
                    context.getString(R.string.no_valid_items_found),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    if (isSelectionMode) {
                        Text(
                            text = if (selectedIds.isEmpty()) {
                                stringResource(R.string.select_items)
                            } else {
                                stringResource(R.string.selected_count, selectedIds.size)
                            },
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.scan_history),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    if (isSelectionMode) {
                        IconButton(
                            onClick = {
                                isSelectionMode = false
                                selectedIds = emptySet()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.cancel),
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    } else {
                        BackButton(onClick = onBack)
                    }
                },
                actions = {
                    if (isSelectionMode) {
                        val allDisplayedIds = displayedList.map { it.id }.toSet()
                        val isAllSelected = displayedList.isNotEmpty() && selectedIds.containsAll(allDisplayedIds)

                        // Select All / Deselect All
                        IconButton(
                            onClick = {
                                selectedIds = if (isAllSelected) {
                                    emptySet()
                                } else {
                                    allDisplayedIds
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (isAllSelected) Icons.Outlined.Deselect else Icons.Outlined.SelectAll,
                                contentDescription = if (isAllSelected) stringResource(R.string.deselect_all) else stringResource(R.string.select_all),
                                tint = softBlue,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Delete Selected
                        IconButton(
                            onClick = {
                                if (selectedIds.isNotEmpty()) {
                                    val count = selectedIds.size
                                    val updated = ScanHistoryManager.deleteHistoryItems(context, selectedIds)
                                    onHistoryUpdated(updated)
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.items_deleted_successfully, count),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    isSelectionMode = false
                                    selectedIds = emptySet()
                                }
                            },
                            enabled = selectedIds.isNotEmpty()
                        ) {
                            Icon(
                                imageVector = Delete,
                                contentDescription = stringResource(R.string.delete_selected),
                                tint = if (selectedIds.isNotEmpty()) MaterialTheme.colorScheme.error else textMuted.copy(alpha = 0.4f),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    } else {
                        // 1. Favorite Filter Icon (Heart)
                        IconButton(
                            onClick = { isFavoritesFilterActive = !isFavoritesFilterActive }
                        ) {
                            Icon(
                                imageVector = if (isFavoritesFilterActive) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = stringResource(R.string.filter_favorites),
                                tint = if (isFavoritesFilterActive) Color(0xFFFF5252) else softBlue,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // 2. Delete Icon (Trash) -> Activates Selection Mode
                        if (historyList.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    isSelectionMode = true
                                    selectedIds = emptySet()
                                }
                            ) {
                                Icon(
                                    imageVector = Delete,
                                    contentDescription = stringResource(R.string.delete_item),
                                    tint = softBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        // 3. Three-dots Menu
                        Box {
                            IconButton(
                                onClick = {
                                    menuState = HistoryMenuState.Root
                                    menuExpanded = true
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = stringResource(R.string.more_options),
                                    tint = softBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = {
                                    menuExpanded = false
                                    menuState = HistoryMenuState.Root
                                },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainer)
                            ) {
                                when (menuState) {
                                    HistoryMenuState.Root -> {
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = stringResource(R.string.export_history),
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Outlined.FileDownload,
                                                    contentDescription = null,
                                                    tint = softBlue,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            },
                                            onClick = {
                                                if (historyList.isEmpty()) {
                                                    Toast.makeText(
                                                        context,
                                                        context.getString(R.string.no_items_to_export),
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    menuExpanded = false
                                                } else {
                                                    menuState = HistoryMenuState.ExportAs
                                                }
                                            }
                                        )

                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = stringResource(R.string.import_history),
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Outlined.FileUpload,
                                                    contentDescription = null,
                                                    tint = softBlue,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            },
                                            onClick = {
                                                menuExpanded = false
                                                importFileLauncher.launch(
                                                    arrayOf(
                                                        "text/plain",
                                                        "text/csv",
                                                        "text/comma-separated-values",
                                                        "application/json",
                                                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                                        "*/*"
                                                    )
                                                )
                                            }
                                        )
                                    }

                                    HistoryMenuState.ExportAs -> {
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = stringResource(R.string.export_as),
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                    contentDescription = "Back",
                                                    tint = softBlue,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            },
                                            onClick = {
                                                menuState = HistoryMenuState.Root
                                            }
                                        )

                                        HorizontalDivider(
                                            color = MaterialTheme.colorScheme.outlineVariant,
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        )

                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = stringResource(R.string.format_txt),
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Outlined.Description,
                                                    contentDescription = null,
                                                    tint = softBlue,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            },
                                            onClick = {
                                                menuExpanded = false
                                                menuState = HistoryMenuState.Root
                                                pendingExportFormat = ExportFormat.TXT
                                                val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US).format(java.util.Date())
                                                exportFileLauncher.launch("links_scan_history_$timestamp.txt")
                                            }
                                        )

                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = stringResource(R.string.format_csv),
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Outlined.TableChart,
                                                    contentDescription = null,
                                                    tint = softBlue,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            },
                                            onClick = {
                                                menuExpanded = false
                                                menuState = HistoryMenuState.Root
                                                pendingExportFormat = ExportFormat.CSV
                                                val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US).format(java.util.Date())
                                                exportFileLauncher.launch("links_scan_history_$timestamp.csv")
                                            }
                                        )

                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = stringResource(R.string.format_json),
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Outlined.Code,
                                                    contentDescription = null,
                                                    tint = softBlue,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            },
                                            onClick = {
                                                menuExpanded = false
                                                menuState = HistoryMenuState.Root
                                                pendingExportFormat = ExportFormat.JSON
                                                val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US).format(java.util.Date())
                                                exportFileLauncher.launch("links_scan_history_$timestamp.json")
                                            }
                                        )

                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = stringResource(R.string.format_xlsx),
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Outlined.GridOn,
                                                    contentDescription = null,
                                                    tint = softBlue,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            },
                                            onClick = {
                                                menuExpanded = false
                                                menuState = HistoryMenuState.Root
                                                pendingExportFormat = ExportFormat.XLSX
                                                val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US).format(java.util.Date())
                                                exportFileLauncher.launch("links_scan_history_$timestamp.xlsx")
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (displayedList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (isFavoritesFilterActive) Icons.Filled.Favorite else Icons.Default.Link,
                            contentDescription = null,
                            tint = textMuted,
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (isFavoritesFilterActive) stringResource(R.string.no_favorites_yet) else stringResource(R.string.no_scan_history_yet),
                            color = textMuted,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 104.dp)
                ) {
                    groupedItems.forEach { (date, itemsInGroup) ->
                        item(key = "header_$date") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = date,
                                    color = textMuted,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        item(key = "card_$date") {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                                border = androidx.compose.foundation.BorderStroke(1.dp, borderGrey),
                                shape = RoundedCornerShape(18.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    itemsInGroup.forEachIndexed { index, item ->
                                        val isSelected = isSelectionMode && selectedIds.contains(item.id)

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .then(
                                                    if (isSelectionMode) {
                                                        Modifier.clickable {
                                                            selectedIds = if (selectedIds.contains(item.id)) {
                                                                selectedIds - item.id
                                                            } else {
                                                                selectedIds + item.id
                                                            }
                                                        }
                                                    } else {
                                                        Modifier
                                                    }
                                                )
                                                .background(
                                                    if (isSelected) softBlue.copy(alpha = 0.08f) else Color.Transparent
                                                )
                                                .padding(horizontal = 16.dp, vertical = 14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Leading badge / icon:
                                            // When selected: pale blue background circle with a soft blue checkmark.
                                            // Otherwise: normal T (for text) or Link icon (for URL).
                                            Surface(
                                                shape = CircleShape,
                                                color = if (isSelected) {
                                                    softBlue.copy(alpha = 0.22f)
                                                } else {
                                                    MaterialTheme.colorScheme.surfaceVariant
                                                },
                                                border = if (isSelected) {
                                                    androidx.compose.foundation.BorderStroke(1.5.dp, softBlue.copy(alpha = 0.7f))
                                                } else null,
                                                modifier = Modifier.size(40.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    if (isSelected) {
                                                        Icon(
                                                            imageVector = Icons.Default.Check,
                                                            contentDescription = "Selected",
                                                            tint = softBlue,
                                                            modifier = Modifier.size(22.dp)
                                                        )
                                                    } else if (item.isWifi) {
                                                        Icon(
                                                            imageVector = Icons.Default.Wifi,
                                                            contentDescription = "Wi-Fi",
                                                            tint = softBlue,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                    } else if (item.isUrl) {
                                                        Icon(
                                                            imageVector = Icons.Default.Link,
                                                            contentDescription = "URL",
                                                            tint = softBlue,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                    } else if (item.isNumbers) {
                                                        Text(
                                                            text = "#",
                                                            color = MaterialTheme.colorScheme.onSurface,
                                                            fontSize = 18.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    } else {
                                                        Text(
                                                            text = "T",
                                                            color = MaterialTheme.colorScheme.onSurface,
                                                            fontSize = 18.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(14.dp))

                                            // Content (Wi-Fi, URL, Numbers, or Text)
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = item.typeLabel,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = timeFormatter.format(java.util.Date(item.timestamp)),
                                                    color = textMuted,
                                                    fontSize = 12.sp,
                                                    modifier = Modifier.padding(top = 1.dp, bottom = 2.dp)
                                                )
                                                if (item.isWifi) {
                                                    val wifiInfo = remember(item.content) { parseWifiQr(item.content) }
                                                    if (wifiInfo != null) {
                                                        Column(
                                                            modifier = Modifier.padding(top = 3.dp),
                                                            verticalArrangement = Arrangement.spacedBy(2.dp)
                                                        ) {
                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                Text(
                                                                    text = "${stringResource(R.string.network_name)}: ",
                                                                    color = softBlue,
                                                                    fontSize = 13.sp,
                                                                    fontWeight = FontWeight.SemiBold
                                                                )
                                                                Text(
                                                                    text = wifiInfo.ssid,
                                                                    color = MaterialTheme.colorScheme.onSurface,
                                                                    fontSize = 13.sp,
                                                                    fontWeight = FontWeight.Medium,
                                                                    maxLines = 1,
                                                                    overflow = TextOverflow.Ellipsis
                                                                )
                                                            }
                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                Text(
                                                                    text = "${stringResource(R.string.password)}: ",
                                                                    color = softBlue,
                                                                    fontSize = 13.sp,
                                                                    fontWeight = FontWeight.SemiBold
                                                                )
                                                                Text(
                                                                    text = if (wifiInfo.password.isNotEmpty()) wifiInfo.password else stringResource(R.string.none),
                                                                    color = textMuted,
                                                                    fontSize = 13.sp,
                                                                    maxLines = 1,
                                                                    overflow = TextOverflow.Ellipsis
                                                                )
                                                            }
                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                Text(
                                                                    text = "${stringResource(R.string.security_type)}: ",
                                                                    color = softBlue,
                                                                    fontSize = 13.sp,
                                                                    fontWeight = FontWeight.SemiBold
                                                                )
                                                                Text(
                                                                    text = wifiInfo.type,
                                                                    color = textMuted,
                                                                    fontSize = 13.sp,
                                                                    maxLines = 1,
                                                                    overflow = TextOverflow.Ellipsis
                                                                )
                                                            }
                                                        }
                                                    } else {
                                                        Text(
                                                            text = item.content,
                                                            color = textMuted,
                                                            fontSize = 14.sp,
                                                            lineHeight = 20.sp,
                                                            maxLines = 3,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                } else {
                                                    Text(
                                                        text = item.content,
                                                        color = textMuted,
                                                        fontSize = 14.sp,
                                                        lineHeight = 20.sp,
                                                        maxLines = 3,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }

                                            if (!isSelectionMode) {
                                                Spacer(modifier = Modifier.width(6.dp))

                                                // Heart Favorite Button
                                                IconButton(
                                                    onClick = {
                                                        val updated = ScanHistoryManager.toggleFavorite(context, item.id)
                                                        onHistoryUpdated(updated)
                                                    },
                                                    modifier = Modifier.size(36.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = if (item.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                                        contentDescription = if (item.isFavorite) "Unfavorite" else "Favorite",
                                                        tint = if (item.isFavorite) Color(0xFFFF5252) else textMuted,
                                                        modifier = Modifier.size(22.dp)
                                                    )
                                                }

                                                // Item 3-dots Menu
                                                Box {
                                                    var itemMenuExpanded by remember { mutableStateOf(false) }
                                                    IconButton(
                                                        onClick = { itemMenuExpanded = true },
                                                        modifier = Modifier.size(36.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.MoreVert,
                                                            contentDescription = "More",
                                                            tint = textMuted,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                    }

                                                    DropdownMenu(
                                                        expanded = itemMenuExpanded,
                                                        onDismissRequest = { itemMenuExpanded = false },
                                                        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainer)
                                                    ) {
                                                        val wifiInfo = if (item.isWifi) parseWifiQr(item.content) else null

                                                        DropdownMenuItem(
                                                            text = { Text(stringResource(R.string.copy), color = MaterialTheme.colorScheme.onSurface) },
                                                            leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null, tint = softBlue, modifier = Modifier.size(20.dp)) },
                                                            onClick = {
                                                                itemMenuExpanded = false
                                                                val textToCopy = if (wifiInfo != null) {
                                                                    if (wifiInfo.password.isNotEmpty()) wifiInfo.password else wifiInfo.ssid
                                                                } else {
                                                                    item.content
                                                                }
                                                                clipboardManager.setText(AnnotatedString(textToCopy))
                                                                val toastMsg = if (wifiInfo != null && wifiInfo.password.isNotEmpty()) {
                                                                    context.getString(R.string.password_copied)
                                                                } else {
                                                                    context.getString(R.string.copied_to_clipboard)
                                                                }
                                                                Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
                                                            }
                                                        )

                                                        DropdownMenuItem(
                                                            text = { Text(stringResource(R.string.share), color = MaterialTheme.colorScheme.onSurface) },
                                                            leadingIcon = { Icon(Icons.Outlined.Share, contentDescription = null, tint = softBlue, modifier = Modifier.size(20.dp)) },
                                                            onClick = {
                                                                itemMenuExpanded = false
                                                                val textToShare = if (wifiInfo != null) {
                                                                    if (wifiInfo.password.isNotEmpty()) {
                                                                        "Wi-Fi: ${wifiInfo.ssid}\nPassword: ${wifiInfo.password}\nSecurity: ${wifiInfo.type}"
                                                                    } else {
                                                                        "Wi-Fi: ${wifiInfo.ssid}\nSecurity: ${wifiInfo.type}"
                                                                    }
                                                                } else {
                                                                    item.content
                                                                }
                                                                val sendIntent = android.content.Intent().apply {
                                                                    action = android.content.Intent.ACTION_SEND
                                                                    putExtra(android.content.Intent.EXTRA_TEXT, textToShare)
                                                                    type = "text/plain"
                                                                }
                                                                context.startActivity(android.content.Intent.createChooser(sendIntent, null))
                                                            }
                                                        )

                                                        if (wifiInfo != null) {
                                                            DropdownMenuItem(
                                                                text = { Text(stringResource(R.string.connect), color = MaterialTheme.colorScheme.onSurface) },
                                                                leadingIcon = { Icon(Icons.Default.Wifi, contentDescription = null, tint = softBlue, modifier = Modifier.size(20.dp)) },
                                                                onClick = {
                                                                    itemMenuExpanded = false
                                                                    connectToWifi(context, wifiInfo)
                                                                }
                                                            )
                                                        }

                                                        if (item.isUrl) {
                                                            DropdownMenuItem(
                                                                text = { Text(stringResource(R.string.open), color = MaterialTheme.colorScheme.onSurface) },
                                                                leadingIcon = { Icon(Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = null, tint = softBlue, modifier = Modifier.size(20.dp)) },
                                                                onClick = {
                                                                    itemMenuExpanded = false
                                                                    try {
                                                                        val parsedUri = if (item.content.startsWith("http://") || item.content.startsWith("https://")) {
                                                                            Uri.parse(item.content)
                                                                        } else {
                                                                            Uri.parse("https://${item.content}")
                                                                        }
                                                                        context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, parsedUri))
                                                                    } catch (e: Exception) {
                                                                        Toast.makeText(context, "Could not open URL", Toast.LENGTH_SHORT).show()
                                                                    }
                                                                }
                                                            )

                                                            DropdownMenuItem(
                                                                text = { Text(stringResource(R.string.check_security), color = MaterialTheme.colorScheme.onSurface) },
                                                                leadingIcon = { Icon(Icons.Filled.Security, contentDescription = null, tint = softBlue, modifier = Modifier.size(20.dp)) },
                                                                onClick = {
                                                                    itemMenuExpanded = false
                                                                    onNavigateToSecurity(item.content)
                                                                }
                                                            )
                                                        }

                                                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 4.dp))

                                                         DropdownMenuItem(
                                                            text = { Text(stringResource(R.string.delete_item), color = MaterialTheme.colorScheme.error) },
                                                            leadingIcon = { Icon(Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp)) },
                                                            onClick = {
                                                                itemMenuExpanded = false
                                                                val updated = ScanHistoryManager.deleteHistoryItem(context, item.id)
                                                                onHistoryUpdated(updated)
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        if (index < itemsInGroup.size - 1) {
                                            HorizontalDivider(
                                                color = borderGrey.copy(alpha = 0.5f),
                                                modifier = Modifier.padding(horizontal = 16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

enum class QrMode {
    Scan, Generate, Costume
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun QrCodeScreen(
    onNavigateToSecurity: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("links_prefs", android.content.Context.MODE_PRIVATE) }
    var showCostumeTab by remember { mutableStateOf(prefs.getBoolean("show_costume_tab", true)) }

    DisposableEffect(prefs) {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { p, key ->
            if (key == "show_costume_tab") {
                showCostumeTab = p.getBoolean("show_costume_tab", true)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    var currentMode by remember { mutableStateOf(QrMode.Scan) }

    LaunchedEffect(showCostumeTab) {
        if (!showCostumeTab && currentMode == QrMode.Costume) {
            currentMode = QrMode.Scan
        }
    }

    var selectedHistoryItem by remember { mutableStateOf<String?>(null) }
    var isHistoryPageOpen by remember { mutableStateOf(false) }

    var historyItems by remember { mutableStateOf(ScanHistoryManager.getHistoryItems(context)) }

    val softBlue = MaterialTheme.colorScheme.primary
    val borderGrey = MaterialTheme.colorScheme.outlineVariant
    val textMuted = MaterialTheme.colorScheme.onSurfaceVariant
    val onSurface = MaterialTheme.colorScheme.onSurface

    BackHandler(enabled = isHistoryPageOpen) {
        isHistoryPageOpen = false
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (isHistoryPageOpen) {
            ScanHistoryPage(
                historyList = historyItems,
                onHistoryUpdated = { updatedList ->
                    historyItems = updatedList
                },
                onClearHistory = {
                    ScanHistoryManager.clearHistory(context)
                    historyItems = emptyList()
                },
                onNavigateToSecurity = onNavigateToSecurity,
                onBack = { isHistoryPageOpen = false }
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(bottom = 96.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp, bottom = 12.dp)
                        .height(48.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (currentMode) {
                            QrMode.Scan -> stringResource(R.string.scan_qr_code)
                            QrMode.Generate -> stringResource(R.string.qr_code_generator)
                            QrMode.Costume -> stringResource(R.string.costume_qr_code)
                        },
                        color = onSurface,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (currentMode == QrMode.Scan) {
                        IconButton(
                            onClick = {
                                historyItems = ScanHistoryManager.getHistoryItems(context)
                                isHistoryPageOpen = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.History,
                                contentDescription = "Scan History",
                                tint = softBlue,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(48.dp))
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    when (currentMode) {
                        QrMode.Scan -> {
                            ScanContent(
                                softBlue = softBlue,
                                borderGrey = borderGrey,
                                textMuted = textMuted,
                                externalScannedResult = selectedHistoryItem,
                                onClearExternalScannedResult = { selectedHistoryItem = null },
                                onNavigateToSecurity = onNavigateToSecurity
                            )
                        }
                        QrMode.Generate -> {
                            GenerateContent(softBlue = softBlue, borderGrey = borderGrey, textMuted = textMuted)
                        }
                        QrMode.Costume -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(horizontal = 24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Info,
                                        contentDescription = null,
                                        tint = textMuted,
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = stringResource(R.string.option_currently_unavailable),
                                        color = textMuted,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                // Mode Selection Segmented Bar
                QrModeSelectionBar(
                    selectedMode = currentMode,
                    onSelect = { currentMode = it },
                    showCostumeTab = showCostumeTab,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrModeSelectionBar(
    selectedMode: QrMode,
    onSelect: (QrMode) -> Unit,
    showCostumeTab: Boolean = true,
    modifier: Modifier = Modifier
) {
    val modes = remember(showCostumeTab) {
        if (showCostumeTab) {
            listOf(QrMode.Scan, QrMode.Generate, QrMode.Costume)
        } else {
            listOf(QrMode.Scan, QrMode.Generate)
        }
    }
    val softBlue = MaterialTheme.colorScheme.primary
    val borderGrey = MaterialTheme.colorScheme.outlineVariant
    
    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        modes.forEachIndexed { index, mode ->
            val isSelected = selectedMode == mode
            
            val colors = SegmentedButtonDefaults.colors(
                activeContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                activeContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                inactiveContainerColor = Color.Transparent,
                inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                activeBorderColor = borderGrey,
                inactiveBorderColor = borderGrey
            )
            
            SegmentedButton(
                selected = isSelected,
                onClick = { onSelect(mode) },
                shape = SegmentedButtonDefaults.itemShape(index, modes.size),
                colors = colors,
                label = {
                    Text(
                        text = when (mode) {
                            QrMode.Scan -> stringResource(R.string.scan)
                            QrMode.Generate -> stringResource(R.string.generate)
                            QrMode.Costume -> stringResource(R.string.costume)
                        },
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            )
        }
    }
}

@Composable
fun GoogleStyleViewfinder(
    centerX: Float,
    centerY: Float,
    width: Float,
    height: Float,
    rotationDegrees: Float = 0f,
    color: Color = Color(0xFF8AB4F8),
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val side = minOf(width, height)
        if (side <= 0f) return@Canvas

        val strokeWidth = 5.dp.toPx()
        val cornerLength = (side * 0.22f).coerceIn(24.dp.toPx(), 44.dp.toPx())
        val cornerRadius = (cornerLength * 0.5f).coerceIn(12.dp.toPx(), 22.dp.toPx())

        val halfW = width / 2f
        val halfH = height / 2f
        val left = centerX - halfW
        val top = centerY - halfH
        val right = centerX + halfW
        val bottom = centerY + halfH

        rotate(degrees = rotationDegrees, pivot = androidx.compose.ui.geometry.Offset(centerX, centerY)) {
            // Top-Left
            drawPath(
                path = Path().apply {
                    moveTo(left, top + cornerLength)
                    lineTo(left, top + cornerRadius)
                    quadraticTo(left, top, left + cornerRadius, top)
                    lineTo(left + cornerLength, top)
                },
                color = color,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )

            // Top-Right
            drawPath(
                path = Path().apply {
                    moveTo(right - cornerLength, top)
                    lineTo(right - cornerRadius, top)
                    quadraticTo(right, top, right, top + cornerRadius)
                    lineTo(right, top + cornerLength)
                },
                color = color,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )

            // Bottom-Left
            drawPath(
                path = Path().apply {
                    moveTo(left, bottom - cornerLength)
                    lineTo(left, bottom - cornerRadius)
                    quadraticTo(left, bottom, left + cornerRadius, bottom)
                    lineTo(left + cornerLength, bottom)
                },
                color = color,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )

            // Bottom-Right
            drawPath(
                path = Path().apply {
                    moveTo(right - cornerLength, bottom)
                    lineTo(right - cornerRadius, bottom)
                    quadraticTo(right, bottom, right, bottom - cornerRadius)
                    lineTo(right, bottom - cornerLength)
                },
                color = color,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
        }
    }
}

@Composable
fun FullScreenScannerModal(
    onClose: () -> Unit,
    onQrCodeScanned: (String) -> Unit,
) {
    var isTorchEnabled by remember { mutableStateOf(false) }
    var cameraLensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var zoomRatio by remember { mutableFloatStateOf(0f) }
    val softBlue = Color(0xFF8AB4F8)
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("links_prefs", Context.MODE_PRIVATE) }
    var showZoomSlider by remember {
        mutableStateOf(
            if (prefs.contains("show_zoom_slider")) {
                prefs.getBoolean("show_zoom_slider", false)
            } else if (prefs.contains("qr_pinch_zoom")) {
                !prefs.getBoolean("qr_pinch_zoom", true)
            } else {
                false
            }
        )
    }

    DisposableEffect(prefs) {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { p, key ->
            if (key == "show_zoom_slider") {
                showZoomSlider = p.getBoolean("show_zoom_slider", false)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    // Resolve the host Activity even when context is wrapped (e.g. inside a Dialog)
    val activity = remember(context) {
        var ctx: android.content.Context = context
        while (ctx is android.content.ContextWrapper && ctx !is android.app.Activity) {
            ctx = ctx.baseContext
        }
        ctx as? android.app.Activity
    }

    // True if the front camera has a hardware flash unit
    val hasFrontCameraFlash = remember {
        try {
            val cm = context.getSystemService(android.content.Context.CAMERA_SERVICE)
                    as android.hardware.camera2.CameraManager
            val frontId = cm.cameraIdList.firstOrNull { id ->
                cm.getCameraCharacteristics(id)
                    .get(android.hardware.camera2.CameraCharacteristics.LENS_FACING) ==
                    android.hardware.camera2.CameraCharacteristics.LENS_FACING_FRONT
            }
            frontId?.let {
                cm.getCameraCharacteristics(it)
                    .get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            } ?: false
        } catch (e: Exception) { false }
    }

    // Active when front camera has no hardware flash and the user presses the flash button
    var isScreenFlashEnabled by remember { mutableStateOf(false) }

    // Raise / restore screen brightness to simulate a flash via the display
    LaunchedEffect(isScreenFlashEnabled) {
        activity?.window?.let { win ->
            val lp = win.attributes
            lp.screenBrightness = if (isScreenFlashEnabled) 0.33f
                                   else android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            win.attributes = lp
        }
    }

    // Reset all flash states whenever the user flips the camera
    LaunchedEffect(cameraLensFacing) {
        isTorchEnabled = false
        isScreenFlashEnabled = false
    }

    // Restore normal screen brightness and reset torch when the scanner dialog is dismissed
    DisposableEffect(Unit) {
        onDispose {
            isTorchEnabled = false
            isScreenFlashEnabled = false
            activity?.window?.let { win ->
                val lp = win.attributes
                lp.screenBrightness = android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                win.attributes = lp
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val scanned = decodeQrFromUri(context, it)
            if (scanned != null) {
                onQrCodeScanned(scanned)
                onClose()
            } else {
                Toast.makeText(context, context.getString(R.string.no_qr_found_in_image), Toast.LENGTH_SHORT).show()
            }
        }
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
        ) {
            val density = LocalDensity.current
            val screenWidthPx = constraints.maxWidth.toFloat()
            val screenHeightPx = constraints.maxHeight.toFloat()
            val defaultBoxSizePx = with(density) { 240.dp.toPx() }

            val defaultBox = remember(screenWidthPx, screenHeightPx, defaultBoxSizePx) {
                QrBox(
                    centerX = screenWidthPx / 2f,
                    centerY = screenHeightPx / 2f,
                    width = defaultBoxSizePx,
                    height = defaultBoxSizePx,
                    rotationDegrees = 0f
                )
            }

            var detectedBox by remember { mutableStateOf<QrBox?>(null) }
            var isLocked by remember { mutableStateOf(false) }
            var pendingScannedResult by remember { mutableStateOf<String?>(null) }

            val targetBox = detectedBox ?: defaultBox

            val animCenterX by animateFloatAsState(
                targetValue = targetBox.centerX,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "animCenterX"
            )

            val animCenterY by animateFloatAsState(
                targetValue = targetBox.centerY,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "animCenterY"
            )

            val animWidth by animateFloatAsState(
                targetValue = targetBox.width,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "animWidth"
            )

            val animHeight by animateFloatAsState(
                targetValue = targetBox.height,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "animHeight"
            )

            val animRotation by animateFloatAsState(
                targetValue = targetBox.rotationDegrees,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "animRotation"
            )

            // Trigger completion after animation delay when a code is detected
            LaunchedEffect(isLocked, pendingScannedResult) {
                if (isLocked && pendingScannedResult != null) {
                    try {
                        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? android.os.VibratorManager
                            vm?.defaultVibrator
                        } else {
                            @Suppress("DEPRECATION")
                            context.getSystemService(Context.VIBRATOR_SERVICE) as? android.os.Vibrator
                        }
                        vibrator?.vibrate(android.os.VibrationEffect.createOneShot(45, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                    } catch (_: Exception) {}

                    kotlinx.coroutines.delay(360)
                    onQrCodeScanned(pendingScannedResult!!)
                    onClose()
                }
            }

            // Camera Stream
            QrScannerView(
                onQrCodeScanned = { result, box ->
                    if (!isLocked) {
                        isLocked = true
                        detectedBox = box
                        pendingScannedResult = result
                    }
                },
                isTorchEnabled = isTorchEnabled,
                cameraLensFacing = cameraLensFacing,
                zoomRatio = zoomRatio,
                modifier = Modifier.fillMaxSize(),
            )

            // Screen Flash Overlay: white border shown when front camera has no hardware flash
            if (isScreenFlashEnabled) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(width = 18.dp, color = Color.White)
                )
            }

            // Animated Viewfinder Corners overlay
            GoogleStyleViewfinder(
                centerX = animCenterX,
                centerY = animCenterY,
                width = animWidth,
                height = animHeight,
                rotationDegrees = animRotation,
                color = softBlue,
                modifier = Modifier.fillMaxSize(),
            )

            // Top Control Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                // Close button (white circle with black X)
                Surface(
                    onClick = onClose,
                    shape = CircleShape,
                    color = Color.White,
                    modifier = Modifier.size(40.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.Black,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }

                // Title
                Text(
                    text = "Scan code",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp)
                )

                // Actions Column: Flash, Flip Camera
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 1. Flashlight toggle button (white circle)
                    Surface(
                        onClick = {
                            val isFront = cameraLensFacing == CameraSelector.LENS_FACING_FRONT
                            if (isFront && !hasFrontCameraFlash) {
                                // Front camera without hardware flash → use screen flash
                                isScreenFlashEnabled = !isScreenFlashEnabled
                            } else {
                                // Back camera or front camera with hardware flash → use torch
                                isTorchEnabled = !isTorchEnabled
                            }
                        },
                        shape = CircleShape,
                        color = Color.White,
                        modifier = Modifier.size(40.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isTorchEnabled || isScreenFlashEnabled)
                                    Icons.Outlined.FlashOn else Icons.Outlined.FlashOff,
                                contentDescription = "Flashlight",
                                tint = Color.Black,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                    }

                    // 2. Flip Camera circle button (under Flash button)
                    Surface(
                        onClick = {
                            cameraLensFacing = if (cameraLensFacing == CameraSelector.LENS_FACING_BACK) {
                                CameraSelector.LENS_FACING_FRONT
                            } else {
                                CameraSelector.LENS_FACING_BACK
                            }
                        },
                        shape = CircleShape,
                        color = Color.White,
                        modifier = Modifier.size(40.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Cameraswitch,
                                contentDescription = "Flip camera",
                                tint = Color.Black,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                    }
                }
            }

            // Bottom Control Area: Zoom Slider + Scanned by Links Footer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Zoom Control Pill Slider
                if (showZoomSlider) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.Black.copy(alpha = 0.55f),
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ZoomOut,
                                contentDescription = "Zoom out",
                                tint = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable { zoomRatio = 0f }
                            )

                            Slider(
                                value = zoomRatio,
                                onValueChange = { zoomRatio = it },
                                valueRange = 0f..1f,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color.White,
                                    activeTrackColor = softBlue,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 8.dp)
                            )

                            Icon(
                                imageVector = Icons.Outlined.ZoomIn,
                                contentDescription = "Zoom in",
                                tint = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable { zoomRatio = 1f }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Bottom Footer ("Scanned by Links")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Security,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(22.dp),
                    )

                    Text(
                        text = "Scanned by Links",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                    )

                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun MultipleQrCodesDialog(
    results: List<PdfQrResult>,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.select_qr_code),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp)
            ) {
                Text(
                    text = stringResource(R.string.found_qr_codes, results.size),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(results) { item ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(item.text) }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "${stringResource(R.string.page)} ${item.pageNumber}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = item.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    )
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalLayoutApi::class)
@Composable
fun ScanContent(
    softBlue: Color,
    borderGrey: Color,
    textMuted: Color,
    externalScannedResult: String? = null,
    onClearExternalScannedResult: () -> Unit = {},
    onNavigateToSecurity: (String) -> Unit = {}
) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    var scannedResult by remember { mutableStateOf("") }
    var resolvedDestination by remember { mutableStateOf("") }
    var resolvedSource by remember { mutableStateOf("") }
    var isResolving by remember { mutableStateOf(false) }
    var showFullScreenScanner by remember { mutableStateOf(false) }

    var isScanningPdf by remember { mutableStateOf(false) }
    var detectedPdfResults by remember { mutableStateOf<List<PdfQrResult>>(emptyList()) }
    var showPdfMultipleQrDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(externalScannedResult) {
        if (!externalScannedResult.isNullOrEmpty()) {
            scannedResult = externalScannedResult
            resolvedDestination = ""
            resolvedSource = ""
            onClearExternalScannedResult()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                val scanned = PdfQrDecoder.decodeQrFromImageUri(context, it)
                if (scanned != null) {
                    scannedResult = scanned
                    resolvedDestination = ""
                    resolvedSource = ""
                    ScanHistoryManager.addHistoryItem(context, scanned)
                } else {
                    Toast.makeText(context, context.getString(R.string.no_qr_found_in_image), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val pdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                isScanningPdf = true
                try {
                    val results = PdfQrDecoder.decodeQrFromPdf(context, it)
                    isScanningPdf = false
                    if (results.isEmpty()) {
                        Toast.makeText(context, context.getString(R.string.no_qr_found_in_pdf), Toast.LENGTH_SHORT).show()
                    } else if (results.size == 1) {
                        val single = results.first().text
                        scannedResult = single
                        resolvedDestination = ""
                        resolvedSource = ""
                        ScanHistoryManager.addHistoryItem(context, single)
                    } else {
                        detectedPdfResults = results
                        showPdfMultipleQrDialog = true
                    }
                } catch (e: Exception) {
                    isScanningPdf = false
                    Toast.makeText(context, context.getString(R.string.error_reading_pdf), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    if (showPdfMultipleQrDialog) {
        MultipleQrCodesDialog(
            results = detectedPdfResults,
            onSelect = { selected ->
                showPdfMultipleQrDialog = false
                scannedResult = selected
                resolvedDestination = ""
                resolvedSource = ""
                ScanHistoryManager.addHistoryItem(context, selected)
            },
            onDismiss = { showPdfMultipleQrDialog = false }
        )
    }

    if (isScanningPdf) {
        Dialog(
            onDismissRequest = { /* prevent dismiss while scanning */ },
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(
                        color = softBlue,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = stringResource(R.string.scanning_pdf),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }

    val isResolved = resolvedDestination.isNotEmpty() && resolvedSource.isNotEmpty()

    if (showFullScreenScanner && cameraPermissionState.status.isGranted) {
        FullScreenScannerModal(
            onClose = { showFullScreenScanner = false },
            onQrCodeScanned = { result ->
                scannedResult = result
                resolvedDestination = ""
                resolvedSource = ""
                ScanHistoryManager.addHistoryItem(context, result)
            },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (scannedResult.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.QrCodeScanner,
                    contentDescription = null,
                    tint = softBlue,
                    modifier = Modifier.size(56.dp),
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Scan QR codes using camera or import from gallery",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (!cameraPermissionState.status.isGranted) {
                            cameraPermissionState.launchPermissionRequest()
                        }
                        showFullScreenScanner = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    Icon(
                        imageVector = CenterFocusWeak,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp).padding(end = 6.dp),
                    )
                    Text(text = stringResource(R.string.scan_now), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { galleryLauncher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Image,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp).padding(end = 6.dp),
                        )
                        Text(text = stringResource(R.string.image), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = { pdfLauncher.launch("application/pdf") },
                        enabled = true,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = PictureAsPdf,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp).padding(end = 6.dp),
                        )
                        Text(text = stringResource(R.string.pdf), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        } else {
            // Scanned Link Display
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                border = androidx.compose.foundation.BorderStroke(1.dp, borderGrey),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    val wifiInfo = remember(scannedResult) { parseWifiQr(scannedResult) }
                    val isWifi = wifiInfo != null
                    val isUrl = remember(scannedResult, isWifi) { if (isWifi) false else LinkResolver.isValidUrl(scannedResult) }
                    val isNumbers = remember(scannedResult, isWifi, isUrl) {
                        if (isWifi || isUrl) false
                        else {
                            val trimmed = scannedResult.trim()
                            trimmed.isNotEmpty() && trimmed.all { it.isDigit() || it == '+' || it == ' ' || it == '-' || it == '(' || it == ')' } && trimmed.any { it.isDigit() }
                        }
                    }
                    val contentTypeLabel = when {
                        isWifi -> "Wi-Fi"
                        isUrl -> "URL"
                        isNumbers -> "Numbers"
                        else -> "Text"
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = contentTypeLabel,
                            color = softBlue,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )

                        IconButton(
                            onClick = {
                                scannedResult = ""
                                resolvedDestination = ""
                                resolvedSource = ""
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Scan again",
                                tint = textMuted
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    if (isWifi && wifiInfo != null) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // SSID
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${stringResource(R.string.network_name)}: ",
                                    color = softBlue,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = wifiInfo.ssid,
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Password
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${stringResource(R.string.password)}: ",
                                    color = softBlue,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (wifiInfo.password.isNotEmpty()) wifiInfo.password else stringResource(R.string.none),
                                    color = Color.White,
                                    fontSize = 15.sp
                                )
                            }

                            // Security
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${stringResource(R.string.security_type)}: ",
                                    color = softBlue,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = wifiInfo.type,
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    } else {
                        Text(
                            text = scannedResult,
                            color = Color.White,
                            fontSize = 15.sp,
                            lineHeight = 22.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    if (isResolving) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp).padding(top = 8.dp),
                                color = softBlue,
                                strokeWidth = 2.dp
                            )
                        }
                    } else {
                        val buttonHeight = 38.dp

                        if (isWifi && wifiInfo != null) {
                            // Single Row for Wi-Fi: Connect (Capsule) + Copy (Circle) + Share (Circle)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Connect Capsule Button
                                Button(
                                    onClick = {
                                        connectToWifi(context, wifiInfo)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    ),
                                    shape = RoundedCornerShape(50),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                                    modifier = Modifier.height(buttonHeight)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Wifi,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = stringResource(R.string.connect),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                // Copy Button (Copies password)
                                FilledIconButton(
                                    onClick = {
                                        val textToCopy = if (wifiInfo.password.isNotEmpty()) wifiInfo.password else wifiInfo.ssid
                                        clipboardManager.setText(AnnotatedString(textToCopy))
                                        val toastMsg = if (wifiInfo.password.isNotEmpty()) {
                                            context.getString(R.string.password_copied)
                                        } else {
                                            context.getString(R.string.copied_to_clipboard)
                                        }
                                        Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
                                    },
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    ),
                                    shape = CircleShape,
                                    modifier = Modifier.size(buttonHeight)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = stringResource(R.string.copy),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                // Share Button
                                FilledIconButton(
                                    onClick = {
                                        try {
                                            val textToShare = if (wifiInfo.password.isNotEmpty()) {
                                                "Wi-Fi: ${wifiInfo.ssid}\nPassword: ${wifiInfo.password}\nSecurity: ${wifiInfo.type}"
                                            } else {
                                                "Wi-Fi: ${wifiInfo.ssid}\nSecurity: ${wifiInfo.type}"
                                            }
                                            val sendIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                                putExtra(android.content.Intent.EXTRA_TEXT, textToShare)
                                                type = "text/plain"
                                            }
                                            val shareIntent = android.content.Intent.createChooser(sendIntent, null)
                                            context.startActivity(shareIntent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Cannot share", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    ),
                                    shape = CircleShape,
                                    modifier = Modifier.size(buttonHeight)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Share,
                                        contentDescription = stringResource(R.string.share),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (isUrl) {
                                    // Upper Row: Check Security Capsule
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Check Security Button
                                        Button(
                                            onClick = {
                                                onNavigateToSecurity(scannedResult)
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                            ),
                                            shape = RoundedCornerShape(50),
                                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                                            modifier = Modifier.height(buttonHeight)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Security,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = stringResource(R.string.check_security),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }

                                // Lower Row: Copy & Share Circular Buttons + Open Capsule on the right
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Copy Button
                                    FilledIconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(scannedResult))
                                            Toast.makeText(context, context.getString(R.string.copied_to_clipboard), Toast.LENGTH_SHORT).show()
                                        },
                                        colors = IconButtonDefaults.filledIconButtonColors(
                                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                        ),
                                        shape = CircleShape,
                                        modifier = Modifier.size(buttonHeight)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = stringResource(R.string.copy),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    if (isUrl) {
                                        // Share Button
                                        FilledIconButton(
                                            onClick = {
                                                try {
                                                    val sendIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                                        putExtra(android.content.Intent.EXTRA_TEXT, scannedResult)
                                                        type = "text/plain"
                                                    }
                                                    val shareIntent = android.content.Intent.createChooser(sendIntent, null)
                                                    context.startActivity(shareIntent)
                                                } catch (e: Exception) {
                                                    Toast.makeText(context, "Cannot share", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            colors = IconButtonDefaults.filledIconButtonColors(
                                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                            ),
                                            shape = CircleShape,
                                            modifier = Modifier.size(buttonHeight)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Share,
                                                contentDescription = stringResource(R.string.share),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        // Open in Browser Button (on the right of Share)
                                        Button(
                                            onClick = {
                                                val formattedUrl = if (!scannedResult.startsWith("http://") && !scannedResult.startsWith("https://")) {
                                                    "https://$scannedResult"
                                                } else {
                                                    scannedResult
                                                }
                                                try {
                                                    uriHandler.openUri(formattedUrl)
                                                } catch (e: Exception) {
                                                    Toast.makeText(context, context.getString(R.string.cannot_resolve_domain), Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                            ),
                                            shape = RoundedCornerShape(50),
                                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                                            modifier = Modifier.height(buttonHeight)
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = stringResource(R.string.open),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (isResolved) {
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = borderGrey)
                        Spacer(modifier = Modifier.height(16.dp))

                        // Resolved Destination Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = null,
                                tint = softBlue,
                                modifier = Modifier.size(20.dp).padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.destination),
                                    color = softBlue,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = resolvedDestination,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp
                                )
                            }
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(resolvedDestination))
                                    Toast.makeText(context, "Destination copied", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Destination",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun saveBitmapToGallery(context: Context, bitmap: Bitmap) {
    try {
        val filename = "QR_${System.currentTimeMillis()}.png"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Links")
            }
        }
        val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        if (uri != null) {
            context.contentResolver.openOutputStream(uri)?.use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            }
            Toast.makeText(context, "Saved QR code to Pictures/Links", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Error saving image: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateContent(
    softBlue: Color,
    borderGrey: Color,
    textMuted: Color,
) {
    var qrInput by remember { mutableStateOf("") }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(qrInput) {
        val trimmed = qrInput.trim()
        qrBitmap = if (trimmed.isNotEmpty()) {
            QrGenerator.generate(trimmed, size = 512)
        } else {
            null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Text Input Field
        OutlinedTextField(
            value = qrInput,
            onValueChange = { qrInput = it },
            label = { Text(text = stringResource(R.string.generate_qr_for)) },
            placeholder = { Text(text = stringResource(R.string.enter_text_or_url)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = { keyboardController?.hide() },
            ),
            trailingIcon = {
                if (qrInput.isNotEmpty()) {
                    IconButton(onClick = { qrInput = "" }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = softBlue,
                        )
                    }
                } else {
                    IconButton(
                        onClick = {
                            val pasted = clipboardManager.getText()?.text
                            if (!pasted.isNullOrBlank()) qrInput = pasted
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContentPaste,
                            contentDescription = "Paste",
                            tint = softBlue,
                        )
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = softBlue,
                unfocusedBorderColor = borderGrey,
                focusedLabelColor = Color.White,
                unfocusedLabelColor = Color.White.copy(alpha = 0.8f),
                focusedPlaceholderColor = Color.White.copy(alpha = 0.6f),
                unfocusedPlaceholderColor = Color.White.copy(alpha = 0.6f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = softBlue,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
            ),
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Display QR Code Box
        Box(
            modifier = Modifier
                .size(260.dp)
                .background(if (qrBitmap != null) Color.White else MaterialTheme.colorScheme.surfaceContainer, shape = RoundedCornerShape(16.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            val bitmap = qrBitmap
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "QR Code",
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = QrCodeIcon,
                        contentDescription = null,
                        tint = softBlue,
                        modifier = Modifier.size(48.dp),
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.Start,
                    ) {
                        Text(
                            text = stringResource(R.string.enter_text_above),
                            color = Color.White.copy(alpha = 0.95f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 18.sp,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.to_generate_qr_code),
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                        )
                    }
                }
            }
        }

        // Save QR Code Button — always visible, disabled when no content
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = {
                if (qrBitmap != null) saveBitmapToGallery(context, qrBitmap!!)
            },
            enabled = qrBitmap != null,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                disabledContentColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.35f),
            ),
            shape = RoundedCornerShape(50),
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Icon(
                imageVector = Icons.Outlined.Download,
                contentDescription = null,
                modifier = Modifier.size(20.dp).padding(end = 6.dp),
            )
            Text(text = stringResource(R.string.save_qr_code), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
