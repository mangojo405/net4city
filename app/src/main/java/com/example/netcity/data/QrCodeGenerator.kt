package com.example.netcity.data

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.oned.Code128Writer
import com.google.zxing.qrcode.QRCodeWriter

object QrCodeGenerator {
    /**
     * Generates a scannable ISO/IEC 18004 QR code encoding ONLY the permanent object or character ID.
     */
    fun generateQrBitmap(
        content: String,
        sizePx: Int = 360,
        darkColor: Int = Color.parseColor("#0B1D3A")
    ): Bitmap? {
        if (content.isBlank()) return null
        return try {
            val hints = mapOf(
                EncodeHintType.MARGIN to 1,
                EncodeHintType.CHARACTER_SET to "UTF-8"
            )
            val matrix = QRCodeWriter().encode(content.trim(), BarcodeFormat.QR_CODE, sizePx, sizePx, hints)
            val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
            for (x in 0 until sizePx) {
                for (y in 0 until sizePx) {
                    bitmap.setPixel(x, y, if (matrix[x, y]) darkColor else Color.WHITE)
                }
            }
            bitmap
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Generates a high-contrast 1D Code-128 linear barcode bitmap for USB laser & optical scanners.
     */
    fun generateCode128Bitmap(
        content: String,
        widthPx: Int = 520,
        heightPx: Int = 140
    ): Bitmap? {
        val clean = content.trim()
        if (clean.isBlank()) return null
        return try {
            val hints = mapOf(
                EncodeHintType.MARGIN to 6
            )
            val matrix = Code128Writer().encode(clean, BarcodeFormat.CODE_128, widthPx, heightPx, hints)
            val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
            for (x in 0 until widthPx) {
                for (y in 0 until heightPx) {
                    bitmap.setPixel(x, y, if (matrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
        } catch (_: Exception) {
            null
        }
    }
}
