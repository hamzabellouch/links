package com.tkno.links.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.multi.qrcode.QRCodeMultiReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.EnumMap

data class PdfQrResult(
    val text: String,
    val pageNumber: Int
)

object PdfQrDecoder {

    /**
     * Decodes QR codes from a given Bitmap. Returns all unique QR codes found in the bitmap.
     */
    fun decodeQrsFromBitmap(bitmap: Bitmap): List<String> {
        val results = mutableListOf<String>()
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val source = RGBLuminanceSource(width, height, pixels)
        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

        val hints = EnumMap<DecodeHintType, Any>(DecodeHintType::class.java).apply {
            put(DecodeHintType.POSSIBLE_FORMATS, listOf(BarcodeFormat.QR_CODE))
            put(DecodeHintType.TRY_HARDER, java.lang.Boolean.TRUE)
            put(DecodeHintType.CHARACTER_SET, "UTF-8")
        }

        // 1. Try multi-reader first to capture all QR codes if there are multiple on the page
        try {
            val multiReader = QRCodeMultiReader()
            val multiResults = multiReader.decodeMultiple(binaryBitmap, hints)
            for (res in multiResults) {
                val text = res.text?.trim()
                if (!text.isNullOrEmpty() && !results.contains(text)) {
                    results.add(text)
                }
            }
        } catch (_: Exception) {}

        // 2. If multi-reader found nothing, try standard single reader fallback
        if (results.isEmpty()) {
            try {
                val singleReader = MultiFormatReader().apply { setHints(hints) }
                val singleResult = singleReader.decode(binaryBitmap)
                val text = singleResult.text?.trim()
                if (!text.isNullOrEmpty()) {
                    results.add(text)
                }
            } catch (_: Exception) {}
        }

        return results
    }

    /**
     * Reads a PDF document from a content URI, renders each page in a background thread,
     * and extracts all QR codes with their respective page numbers.
     */
    suspend fun decodeQrFromPdf(
        context: Context,
        uri: Uri,
        maxPages: Int = 50
    ): List<PdfQrResult> = withContext(Dispatchers.IO) {
        val results = mutableListOf<PdfQrResult>()
        var pfd: ParcelFileDescriptor? = null
        var renderer: PdfRenderer? = null

        try {
            pfd = context.contentResolver.openFileDescriptor(uri, "r") ?: return@withContext emptyList()
            renderer = PdfRenderer(pfd)
            val totalPages = renderer.pageCount
            val pagesToScan = minOf(totalPages, maxPages)

            for (pageIndex in 0 until pagesToScan) {
                var page: PdfRenderer.Page? = null
                var bitmap: Bitmap? = null
                try {
                    page = renderer.openPage(pageIndex)

                    // Render with 2x scaling for high QR barcode recognition accuracy
                    val scaleFactor = 2f
                    val renderWidth = (page.width * scaleFactor).toInt().coerceIn(600, 2400)
                    val renderHeight = (page.height * scaleFactor).toInt().coerceIn(600, 2400)

                    bitmap = Bitmap.createBitmap(renderWidth, renderHeight, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bitmap)
                    canvas.drawColor(Color.WHITE) // Ensure non-transparent white background

                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                    val qrCodes = decodeQrsFromBitmap(bitmap)
                    for (qr in qrCodes) {
                        if (results.none { it.text == qr }) {
                            results.add(PdfQrResult(text = qr, pageNumber = pageIndex + 1))
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    try { bitmap?.recycle() } catch (_: Exception) {}
                    try { page?.close() } catch (_: Exception) {}
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try { renderer?.close() } catch (_: Exception) {}
            try { pfd?.close() } catch (_: Exception) {}
        }

        results
    }

    /**
     * Decodes a QR code from an Image URI.
     */
    suspend fun decodeQrFromImageUri(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val bitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            } ?: return@withContext null

            val codes = decodeQrsFromBitmap(bitmap)
            try { bitmap.recycle() } catch (_: Exception) {}
            codes.firstOrNull()
        } catch (e: Exception) {
            null
        }
    }
}
