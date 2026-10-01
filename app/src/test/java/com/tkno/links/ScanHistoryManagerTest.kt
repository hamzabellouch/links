package com.tkno.links

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScanHistoryManagerTest {

    @Test
    fun testExportToTxt() {
        val history = listOf(
            ScanHistoryItem(content = "https://google.com"),
            ScanHistoryItem(content = "https://github.com"),
            ScanHistoryItem(content = "Plain text QR")
        )
        val txt = ScanHistoryManager.exportToTxt(history)
        val lines = txt.lines()
        assertEquals(3, lines.size)
        assertEquals("https://google.com", lines[0])
        assertEquals("https://github.com", lines[1])
        assertEquals("Plain text QR", lines[2])
    }

    @Test
    fun testExportToCsv() {
        val history = listOf(
            ScanHistoryItem(content = "https://google.com", isFavorite = true),
            ScanHistoryItem(content = "Text with, comma and \"quotes\"", isFavorite = false)
        )
        val csv = ScanHistoryManager.exportToCsv(history)
        val lines = csv.lines().filter { it.isNotBlank() }
        assertEquals(3, lines.size) // header + 2 items
        assertEquals("Index,Type,Date,Content,Favorite", lines[0])
        assertTrue(lines[1].contains("URL"))
        assertTrue(lines[1].contains("\"https://google.com\""))
        assertTrue(lines[1].endsWith(",Yes"))
        assertTrue(lines[2].contains("Text"))
        assertTrue(lines[2].contains("\"Text with, comma and \"\"quotes\"\"\""))
        assertTrue(lines[2].endsWith(",No"))
    }

    @Test
    fun testExportToJson() {
        val history = listOf(
            ScanHistoryItem(content = "https://google.com", isFavorite = true),
            ScanHistoryItem(content = "https://github.com", isFavorite = false),
            ScanHistoryItem(content = "Special \"quote\" and \n newline", isFavorite = false)
        )
        val json = ScanHistoryManager.exportToJson(history)
        val parsed = ScanHistoryManager.parseJsonString(json)
        assertEquals(3, parsed.size)
        assertEquals("https://google.com", parsed[0])
        assertEquals("https://github.com", parsed[1])
        assertEquals("Special \"quote\" and \n newline", parsed[2])
    }

    @Test
    fun testExportAndParseXlsx() {
        val history = listOf(
            ScanHistoryItem(content = "https://google.com", isFavorite = true),
            ScanHistoryItem(content = "Special chars: <>&\"' / Test", isFavorite = false),
            ScanHistoryItem(content = "Item 3", isFavorite = true)
        )
        val xlsxBytes = ScanHistoryManager.exportToXlsx(history)
        assertTrue(xlsxBytes.isNotEmpty())

        val parsed = ScanHistoryManager.parseXlsxBytes(xlsxBytes)
        assertEquals(3, parsed.size)
        assertEquals("https://google.com", parsed[0])
        assertEquals("Special chars: <>&\"' / Test", parsed[1])
        assertEquals("Item 3", parsed[2])
    }
}
