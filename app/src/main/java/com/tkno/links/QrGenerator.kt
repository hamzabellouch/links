package com.tkno.links

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.util.EnumMap

enum class QrDotsStyle {
    SQUARE,
    ROUNDED,
    DOTS
}

enum class QrMarkerBorder {
    SQUARE,
    ROUNDED
}

enum class QrMarkerCenter {
    SQUARE,
    CIRCLE
}

enum class QrColor(val colorInt: Int, val label: String) {
    BLACK(0xFF000000.toInt(), "Black"),
    NAVY(0xFF1B437C.toInt(), "Navy Blue"),
    ORANGE(0xFFD65620.toInt(), "Orange"),
    RED(0xFFDC2626.toInt(), "Red"),
    GREEN(0xFF256631.toInt(), "Forest Green")
}

enum class QrExportFormat(val extension: String, val mimeType: String, val label: String) {
    PNG("png", "image/png", "PNG"),
    JPG("jpg", "image/jpeg", "JPG"),
    WEBP("webp", "image/webp", "WebP"),
    PDF("pdf", "application/pdf", "PDF")
}

object QrGenerator {

    /**
     * Generates a styled QR Code bitmap from the given text.
     * Supports custom colors, dot shapes, and finder pattern border & center styles.
     */
    fun generate(
        text: String,
        size: Int = 512,
        color: QrColor = QrColor.BLACK,
        dotsStyle: QrDotsStyle = QrDotsStyle.SQUARE,
        markerBorder: QrMarkerBorder = QrMarkerBorder.SQUARE,
        markerCenter: QrMarkerCenter = QrMarkerCenter.SQUARE
    ): Bitmap? {
        if (text.trim().isEmpty()) return null

        return try {
            val margin = 2
            val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java).apply {
                put(EncodeHintType.CHARACTER_SET, "UTF-8")
                put(EncodeHintType.MARGIN, margin)
                put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H)
            }
            val bitMatrix = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, 0, 0, hints)
            val matrixSize = bitMatrix.width

            val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            canvas.drawColor(Color.WHITE)

            val qrPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = color.colorInt
                style = Paint.Style.FILL
            }

            val whitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = Color.WHITE
                style = Paint.Style.FILL
            }

            val cellSize = size.toFloat() / matrixSize

            // Finder patterns bounds (7x7 modules each)
            val tlX = margin
            val tlY = margin
            val trX = matrixSize - margin - 7
            val trY = margin
            val blX = margin
            val blY = matrixSize - margin - 7

            fun isInFinderPattern(x: Int, y: Int): Boolean {
                val inTL = x >= tlX && x < tlX + 7 && y >= tlY && y < tlY + 7
                val inTR = x >= trX && x < trX + 7 && y >= trY && y < trY + 7
                val inBL = x >= blX && x < blX + 7 && y >= blY && y < blY + 7
                return inTL || inTR || inBL
            }

            // Draw Data Dots (modules outside the 3 finder patterns)
            for (y in 0 until matrixSize) {
                for (x in 0 until matrixSize) {
                    if (isInFinderPattern(x, y)) continue
                    if (bitMatrix.get(x, y)) {
                        val left = x * cellSize
                        val top = y * cellSize
                        val right = (x + 1) * cellSize
                        val bottom = (y + 1) * cellSize

                        when (dotsStyle) {
                            QrDotsStyle.SQUARE -> {
                                canvas.drawRect(left, top, right + 0.3f, bottom + 0.3f, qrPaint)
                            }
                            QrDotsStyle.ROUNDED -> {
                                val pad = cellSize * 0.06f
                                val corner = cellSize * 0.35f
                                canvas.drawRoundRect(
                                    left + pad,
                                    top + pad,
                                    right - pad,
                                    bottom - pad,
                                    corner,
                                    corner,
                                    qrPaint
                                )
                            }
                            QrDotsStyle.DOTS -> {
                                val cx = (x + 0.5f) * cellSize
                                val cy = (y + 0.5f) * cellSize
                                val radius = cellSize * 0.42f
                                canvas.drawCircle(cx, cy, radius, qrPaint)
                            }
                        }
                    }
                }
            }

            // Draw Finder Patterns (Top-Left, Top-Right, Bottom-Left)
            val finders = listOf(
                Pair(tlX, tlY),
                Pair(trX, trY),
                Pair(blX, blY)
            )

            for ((fx, fy) in finders) {
                val outerLeft = fx * cellSize
                val outerTop = fy * cellSize
                val outerRight = (fx + 7) * cellSize
                val outerBottom = (fy + 7) * cellSize

                val innerLeft = (fx + 1) * cellSize
                val innerTop = (fy + 1) * cellSize
                val innerRight = (fx + 6) * cellSize
                val innerBottom = (fy + 6) * cellSize

                // 1. Outer Border
                when (markerBorder) {
                    QrMarkerBorder.SQUARE -> {
                        canvas.drawRect(outerLeft, outerTop, outerRight, outerBottom, qrPaint)
                        canvas.drawRect(innerLeft, innerTop, innerRight, innerBottom, whitePaint)
                    }
                    QrMarkerBorder.ROUNDED -> {
                        val outerRadius = cellSize * 1.6f
                        val innerRadius = cellSize * 0.8f
                        canvas.drawRoundRect(outerLeft, outerTop, outerRight, outerBottom, outerRadius, outerRadius, qrPaint)
                        canvas.drawRoundRect(innerLeft, innerTop, innerRight, innerBottom, innerRadius, innerRadius, whitePaint)
                    }
                }

                // 2. Inner Center
                val centerLeft = (fx + 2) * cellSize
                val centerTop = (fy + 2) * cellSize
                val centerRight = (fx + 5) * cellSize
                val centerBottom = (fy + 5) * cellSize

                when (markerCenter) {
                    QrMarkerCenter.SQUARE -> {
                        canvas.drawRect(centerLeft, centerTop, centerRight, centerBottom, qrPaint)
                    }
                    QrMarkerCenter.CIRCLE -> {
                        val cx = (fx + 3.5f) * cellSize
                        val cy = (fy + 3.5f) * cellSize
                        val radius = cellSize * 1.45f
                        canvas.drawCircle(cx, cy, radius, qrPaint)
                    }
                }
            }

            bmp
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Saves a QR Code bitmap in the selected export format (PNG, JPG, WebP, or PDF).
     */
    fun saveQr(
        context: Context,
        bitmap: Bitmap,
        format: QrExportFormat
    ) {
        try {
            if (format == QrExportFormat.PDF) {
                val success = savePdfToStorage(context, bitmap)
                if (success) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.saved_qr_success, format.label, "Downloads/Links"),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(context, "Failed to save PDF", Toast.LENGTH_SHORT).show()
                }
                return
            }

            val filename = "QR_${System.currentTimeMillis()}.${format.extension}"
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, format.mimeType)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Links")
                }
            }

            val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    when (format) {
                        QrExportFormat.PNG -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                        QrExportFormat.JPG -> bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream)
                        QrExportFormat.WEBP -> {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSLESS, 100, stream)
                            } else {
                                @Suppress("DEPRECATION")
                                bitmap.compress(Bitmap.CompressFormat.WEBP, 100, stream)
                            }
                        }
                        QrExportFormat.PDF -> Unit
                    }
                }
                Toast.makeText(
                    context,
                    context.getString(R.string.saved_qr_success, format.label, "Pictures/Links"),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error saving: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun savePdfToStorage(context: Context, bitmap: Bitmap): Boolean {
        val pdfDocument = PdfDocument()
        return try {
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            canvas.drawColor(Color.WHITE)

            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.BLACK
                textSize = 24f
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText("QR Code", 595f / 2f, 130f, titlePaint)

            val qrDisplaySize = 360f
            val left = (595f - qrDisplaySize) / 2f
            val top = 200f
            val destRect = RectF(left, top, left + qrDisplaySize, top + qrDisplaySize)
            val filterPaint = Paint(Paint.FILTER_BITMAP_FLAG)
            canvas.drawBitmap(bitmap, null, destRect, filterPaint)

            val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.GRAY
                textSize = 12f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("Links - QR Code Generator", 595f / 2f, top + qrDisplaySize + 60f, footerPaint)

            pdfDocument.finishPage(page)

            val filename = "QR_${System.currentTimeMillis()}.pdf"
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Links")
                }
            }

            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            } else {
                context.contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
            }

            if (uri != null) {
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    pdfDocument.writeTo(stream)
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            pdfDocument.close()
        }
    }
}
