package com.tkno.links

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

object QrGenerator {
    
    /**
     * Generates a QR Code bitmap from the given text.
     * Uses custom colors that match the application theme:
     * - Background: dark green-charcoal (#0C100D)
     * - Pixels: bright neon-green (#8DE0B0)
     */
    fun generate(text: String, size: Int = 512): Bitmap? {
        if (text.trim().isEmpty()) return null
        
        return try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, size, size)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            
            val backColor = Color.WHITE
            val qrColor = Color.BLACK
            
            val pixels = IntArray(width * height)
            var index = 0
            for (y in 0 until height) {
                for (x in 0 until width) {
                    pixels[index++] = if (bitMatrix.get(x, y)) qrColor else backColor
                }
            }
            bmp.setPixels(pixels, 0, width, 0, 0, width, height)
            bmp
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
