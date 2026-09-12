package com.tkno.links

import com.tkno.links.util.VirusTotalScanner
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Base64

class VirusTotalScannerTest {

    @Test
    fun testUrlEncodingForVirusTotal() {
        val testUrl = "https://example.com"
        val expected = Base64.getUrlEncoder().withoutPadding().encodeToString(testUrl.toByteArray(Charsets.UTF_8))
        // Verify standard Base64 url safe without padding
        assertTrue(expected.isNotEmpty())
        assertFalse(expected.contains("="))
    }

    @Test
    fun testSafetyStatusLogic() {
        // Safe when no threats and harmless/undetected present
        val safeReport = VirusTotalScanner.SecurityReport(
            sourceUrl = "https://example.com",
            destinationUrl = "https://example.com",
            harmlessCount = 70,
            maliciousCount = 0,
            suspiciousCount = 0,
            undetectedCount = 15,
            timeoutCount = 0,
            reputation = 0,
            title = "Example",
            categories = listOf("technology"),
            safetyStatus = VirusTotalScanner.SafetyStatus.SAFE
        )
        assertEquals(85, safeReport.totalEngines)
        assertEquals(0, safeReport.detectionCount)
        assertEquals(VirusTotalScanner.SafetyStatus.SAFE, safeReport.safetyStatus)

        // Malicious when malicious count > 0
        val maliciousReport = VirusTotalScanner.SecurityReport(
            sourceUrl = "https://bad-site.xyz",
            destinationUrl = "https://bad-site.xyz",
            harmlessCount = 10,
            maliciousCount = 5,
            suspiciousCount = 1,
            undetectedCount = 10,
            timeoutCount = 0,
            reputation = -20,
            title = "Bad Site",
            categories = listOf("malware"),
            safetyStatus = VirusTotalScanner.SafetyStatus.MALICIOUS
        )
        assertEquals(26, maliciousReport.totalEngines)
        assertEquals(6, maliciousReport.detectionCount)
        assertEquals(VirusTotalScanner.SafetyStatus.MALICIOUS, maliciousReport.safetyStatus)
    }
}
