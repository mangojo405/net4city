package com.example.netcity.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import com.example.netcity.model.DioramaModule
import java.io.FileOutputStream

/**
 * Generates and prints A4 foldable 3D papercraft diorama nets equipped with
 * both scannable 2D QR Codes and 1D Code-128 Barcodes via Android PrintManager.
 */
object DioramaPapercraftPrinter {

    private const val PAGE_WIDTH_PT = 595 // A4 portrait width in PostScript points (210mm)
    private const val PAGE_HEIGHT_PT = 842 // A4 portrait height in PostScript points (297mm)

    fun printDioramaModules(
        context: Context,
        modules: List<DioramaModule>,
        jobTitle: String = "NET4CITY_Diorama_Kertas_3D"
    ): Boolean {
        if (modules.isEmpty()) return false
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return false
        val adapter = DioramaPrintDocumentAdapter(modules, jobTitle)
        val attributes = PrintAttributes.Builder()
            .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
            .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
            .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
            .build()
        printManager.print(jobTitle, adapter, attributes)
        return true
    }

    /**
     * Renders a crisp preview bitmap of the exact A4 papercraft diorama sheet for in-app preview.
     */
    fun renderPreviewBitmap(
        module: DioramaModule,
        scale: Float = 1.6f
    ): Bitmap {
        val widthPx = (PAGE_WIDTH_PT * scale).toInt()
        val heightPx = (PAGE_HEIGHT_PT * scale).toInt()
        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.scale(scale, scale)
        drawPapercraftSheet(canvas, module, pageIndex = 1, totalPages = 1)
        return bitmap
    }

    private class DioramaPrintDocumentAdapter(
        private val modules: List<DioramaModule>,
        private val docName: String
    ) : PrintDocumentAdapter() {

        override fun onLayout(
            oldAttributes: PrintAttributes?,
            newAttributes: PrintAttributes,
            cancellationSignal: CancellationSignal?,
            callback: LayoutResultCallback,
            extras: Bundle?
        ) {
            if (cancellationSignal?.isCanceled == true) {
                callback.onLayoutCancelled()
                return
            }
            val info = PrintDocumentInfo.Builder("$docName.pdf")
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(modules.size)
                .build()
            callback.onLayoutFinished(info, true)
        }

        override fun onWrite(
            pages: Array<out PageRange>,
            destination: ParcelFileDescriptor,
            cancellationSignal: CancellationSignal?,
            callback: WriteResultCallback
        ) {
            val pdfDocument = PdfDocument()
            try {
                val writtenRanges = mutableListOf<PageRange>()
                modules.forEachIndexed { index, module ->
                    if (cancellationSignal?.isCanceled == true) {
                        callback.onWriteCancelled()
                        return
                    }
                    if (isPageInRanges(index, pages)) {
                        val pageInfo = PdfDocument.PageInfo.Builder(
                            PAGE_WIDTH_PT,
                            PAGE_HEIGHT_PT,
                            index + 1
                        ).create()
                        val page = pdfDocument.startPage(pageInfo)
                        drawPapercraftSheet(
                            canvas = page.canvas,
                            module = module,
                            pageIndex = index + 1,
                            totalPages = modules.size
                        )
                        pdfDocument.finishPage(page)
                        writtenRanges.add(PageRange(index, index))
                    }
                }
                FileOutputStream(destination.fileDescriptor).use { outputStream ->
                    pdfDocument.writeTo(outputStream)
                }
                callback.onWriteFinished(
                    if (writtenRanges.isEmpty()) arrayOf(PageRange.ALL_PAGES)
                    else writtenRanges.toTypedArray()
                )
            } catch (e: Exception) {
                callback.onWriteFailed(e.message ?: "Gagal membuat dokumen cetak diorama.")
            } finally {
                pdfDocument.close()
            }
        }

        private fun isPageInRanges(pageIndex: Int, ranges: Array<out PageRange>): Boolean {
            return ranges.any { range ->
                pageIndex >= range.start && pageIndex <= range.end
            }
        }
    }

    private data class ModulePalette(
        val primary: Int,
        val roof: Int,
        val wallTint: Int,
        val accent: Int,
        val badgeText: String
    )

    private fun paletteFor(module: DioramaModule): ModulePalette {
        val text = "${module.name} ${module.category}".lowercase()
        return when {
            text.contains("rumah sakit") || text.contains("hospital") ->
                ModulePalette(Color.parseColor("#E11D48"), Color.parseColor("#FB7185"), Color.parseColor("#FFF1F2"), Color.parseColor("#F59E0B"), "KESEHATAN")
            text.contains("server") || text.contains("data center") ->
                ModulePalette(Color.parseColor("#4F46E5"), Color.parseColor("#6366F1"), Color.parseColor("#EEF2FF"), Color.parseColor("#06B6D4"), "DATA CENTER")
            text.contains("isp") || text.contains("pop") || text.contains("fiber") || text.contains("odc") || text.contains("odp") ->
                ModulePalette(Color.parseColor("#0284C7"), Color.parseColor("#0EA5E9"), Color.parseColor("#E0F2FE"), Color.parseColor("#F59E0B"), "FIBER / ISP")
            text.contains("sekolah") || text.contains("lab") ->
                ModulePalette(Color.parseColor("#0D9488"), Color.parseColor("#14B8A6"), Color.parseColor("#CCFBF1"), Color.parseColor("#F59E0B"), "EDUKASI")
            text.contains("kantor") || text.contains("toko") || text.contains("niaga") ->
                ModulePalette(Color.parseColor("#D97706"), Color.parseColor("#F59E0B"), Color.parseColor("#FEF3C7"), Color.parseColor("#0284C7"), "KOMERSIAL")
            else ->
                ModulePalette(Color.parseColor("#059669"), Color.parseColor("#10B981"), Color.parseColor("#ECFDF5"), Color.parseColor("#0284C7"), "MODUL KOTA")
        }
    }

    private fun drawPapercraftSheet(
        canvas: Canvas,
        module: DioramaModule,
        pageIndex: Int,
        totalPages: Int
    ) {
        val palette = paletteFor(module)
        val barcodeCode = module.barcodeCode.ifBlank { module.moduleCode.ifBlank { "NET4CITY-MOD" } }
        val qrBitmap = QrCodeGenerator.generateQrBitmap(barcodeCode, sizePx = 280, darkColor = Color.BLACK)
        val bar1DBitmap = QrCodeGenerator.generateCode128Bitmap(barcodeCode, widthPx = 420, heightPx = 110)

        // Clean white A4 background
        canvas.drawColor(Color.WHITE)

        // Common Paints
        val solidCutPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            style = Paint.Style.STROKE
            strokeWidth = 1.6f
        }
        val dashedFoldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#475569")
            style = Paint.Style.STROKE
            strokeWidth = 1.1f
            pathEffect = DashPathEffect(floatArrayOf(5f, 4f), 0f)
        }
        val tabFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#F1F5F9")
            style = Paint.Style.FILL
        }
        val tabHatchPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#CBD5E1")
            style = Paint.Style.STROKE
            strokeWidth = 0.7f
        }
        val tabTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#64748B")
            textSize = 7.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        // 1. HEADER BANNER (y: 24..96)
        val headerRect = RectF(26f, 24f, PAGE_WIDTH_PT - 26f, 96f)
        val headerBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = palette.primary
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(headerRect, 14f, 14f, headerBgPaint)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 16f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        val subTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E0F2FE")
            textSize = 9.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }
        val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FDE047")
            style = Paint.Style.FILL
        }
        val pillTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            textSize = 8.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        canvas.drawText("NET4CITY • POLA DIORAMA KERTAS 3D", 40f, 46f, subTitlePaint)
        canvas.drawText("${module.moduleCode} — ${module.name}", 40f, 67f, titlePaint)
        val levelsText = if (module.requiredLevels.isEmpty()) "Semua Level"
        else module.requiredLevels.joinToString(", ") { "Lv.$it" }
        canvas.drawText("Kode Barcode Permanen: $barcodeCode  •  $levelsText", 40f, 84f, subTitlePaint)

        val badgeRect = RectF(PAGE_WIDTH_PT - 140f, 38f, PAGE_WIDTH_PT - 38f, 62f)
        canvas.drawRoundRect(badgeRect, 12f, 12f, pillPaint)
        canvas.drawText(palette.badgeText, badgeRect.centerX(), badgeRect.centerY() + 3f, pillTextPaint)

        // Legend strip under header (y: 102..118)
        drawLegendBar(canvas, 28f, 112f, solidCutPaint, dashedFoldPaint)

        // 2. 3D PAPERCRAFT CUBE NET (Jaring-Jaring Balok 3D Siap Gunting & Lipat)
        // 4 horizontal faces (KIRI, DEPAN, KANAN, BELAKANG) + ATAP above DEPAN + ALAS below DEPAN
        val s = 120f // 120 pt (~4.2 cm cube face)
        val tabDepth = 18f
        val startX = 62f // leaves space for outer margin; 62 + 4*120 + 18 = 560 <= 595
        val topY = 148f // leaves 18pt top glue tab starting at y=130

        val rectAtap = RectF(startX + s, topY, startX + 2 * s, topY + s)
        val rectKiri = RectF(startX, topY + s, startX + s, topY + 2 * s)
        val rectDepan = RectF(startX + s, topY + s, startX + 2 * s, topY + 2 * s)
        val rectKanan = RectF(startX + 2 * s, topY + s, startX + 3 * s, topY + 2 * s)
        val rectBelakang = RectF(startX + 3 * s, topY + s, startX + 4 * s, topY + 2 * s)
        val rectAlas = RectF(startX + s, topY + 2 * s, startX + 2 * s, topY + 3 * s)

        // Draw 7 angled Glue Tabs ("LEM") BEFORE face borders
        // 3 tabs on ATAP (top, left, right)
        drawGlueTab(canvas, rectAtap.left, rectAtap.top, rectAtap.right, rectAtap.top, 0f, -tabDepth, tabFillPaint, tabHatchPaint, solidCutPaint, tabTextPaint)
        drawGlueTab(canvas, rectAtap.left, rectAtap.top, rectAtap.left, rectAtap.bottom, -tabDepth, 0f, tabFillPaint, tabHatchPaint, solidCutPaint, tabTextPaint)
        drawGlueTab(canvas, rectAtap.right, rectAtap.top, rectAtap.right, rectAtap.bottom, tabDepth, 0f, tabFillPaint, tabHatchPaint, solidCutPaint, tabTextPaint)

        // 1 closure tab on right edge of BELAKANG (glues inside KIRI)
        drawGlueTab(canvas, rectBelakang.right, rectBelakang.top, rectBelakang.right, rectBelakang.bottom, tabDepth, 0f, tabFillPaint, tabHatchPaint, solidCutPaint, tabTextPaint)

        // 3 tabs on ALAS (bottom, left, right)
        drawGlueTab(canvas, rectAlas.left, rectAlas.bottom, rectAlas.right, rectAlas.bottom, 0f, tabDepth, tabFillPaint, tabHatchPaint, solidCutPaint, tabTextPaint)
        drawGlueTab(canvas, rectAlas.left, rectAlas.top, rectAlas.left, rectAlas.bottom, -tabDepth, 0f, tabFillPaint, tabHatchPaint, solidCutPaint, tabTextPaint)
        drawGlueTab(canvas, rectAlas.right, rectAlas.top, rectAlas.right, rectAlas.bottom, tabDepth, 0f, tabFillPaint, tabHatchPaint, solidCutPaint, tabTextPaint)

        // Fill & decorate each of the 6 building faces
        drawAtapFace(canvas, rectAtap, module, barcodeCode, palette, qrBitmap)
        drawSideWallFace(canvas, rectKiri, "SISI KIRI", module, palette, isLeft = true)
        drawDepanFace(canvas, rectDepan, module, barcodeCode, palette, bar1DBitmap)
        drawSideWallFace(canvas, rectKanan, "SISI KANAN", module, palette, isLeft = false)
        drawBelakangFace(canvas, rectBelakang, module, barcodeCode, palette, qrBitmap)
        drawAlasFace(canvas, rectAlas, module, barcodeCode, palette)

        // Draw INTERNAL dashed fold lines (every shared edge between faces or face & glue tab)
        // Between ATAP & DEPAN, and ATAP's 3 glue tabs
        canvas.drawLine(rectAtap.left, rectAtap.bottom, rectAtap.right, rectAtap.bottom, dashedFoldPaint)
        canvas.drawLine(rectAtap.left, rectAtap.top, rectAtap.right, rectAtap.top, dashedFoldPaint)
        canvas.drawLine(rectAtap.left, rectAtap.top, rectAtap.left, rectAtap.bottom, dashedFoldPaint)
        canvas.drawLine(rectAtap.right, rectAtap.top, rectAtap.right, rectAtap.bottom, dashedFoldPaint)

        // Vertical folds between KIRI | DEPAN | KANAN | BELAKANG | tab
        canvas.drawLine(rectDepan.left, rectDepan.top, rectDepan.left, rectDepan.bottom, dashedFoldPaint)
        canvas.drawLine(rectKanan.left, rectKanan.top, rectKanan.left, rectKanan.bottom, dashedFoldPaint)
        canvas.drawLine(rectBelakang.left, rectBelakang.top, rectBelakang.left, rectBelakang.bottom, dashedFoldPaint)
        canvas.drawLine(rectBelakang.right, rectBelakang.top, rectBelakang.right, rectBelakang.bottom, dashedFoldPaint)

        // Between DEPAN & ALAS, and ALAS's 3 glue tabs
        canvas.drawLine(rectAlas.left, rectAlas.top, rectAlas.right, rectAlas.top, dashedFoldPaint)
        canvas.drawLine(rectAlas.left, rectAlas.bottom, rectAlas.right, rectAlas.bottom, dashedFoldPaint)
        canvas.drawLine(rectAlas.left, rectAlas.top, rectAlas.left, rectAlas.bottom, dashedFoldPaint)
        canvas.drawLine(rectAlas.right, rectAlas.top, rectAlas.right, rectAlas.bottom, dashedFoldPaint)

        // Draw OUTER solid cut lines for the faces that have no glue tab attached
        // KIRI: top, left, bottom
        canvas.drawLine(rectKiri.left, rectKiri.top, rectKiri.right, rectKiri.top, solidCutPaint)
        canvas.drawLine(rectKiri.left, rectKiri.top, rectKiri.left, rectKiri.bottom, solidCutPaint)
        canvas.drawLine(rectKiri.left, rectKiri.bottom, rectKiri.right, rectKiri.bottom, solidCutPaint)
        // KANAN: top, bottom
        canvas.drawLine(rectKanan.left, rectKanan.top, rectKanan.right, rectKanan.top, solidCutPaint)
        canvas.drawLine(rectKanan.left, rectKanan.bottom, rectKanan.right, rectKanan.bottom, solidCutPaint)
        // BELAKANG: top, bottom
        canvas.drawLine(rectBelakang.left, rectBelakang.top, rectBelakang.right, rectBelakang.top, solidCutPaint)
        canvas.drawLine(rectBelakang.left, rectBelakang.bottom, rectBelakang.right, rectBelakang.bottom, solidCutPaint)

        // 3. CUT-OUT DESK BARCODE CARD & ASSEMBLY GUIDE (y: 556..774)
        drawCutOutCardAndInstructions(
            canvas = canvas,
            topY = 558f,
            module = module,
            barcodeCode = barcodeCode,
            palette = palette,
            qrBitmap = qrBitmap,
            bar1DBitmap = bar1DBitmap
        )

        // 4. FOOTER (y: 790..822)
        val footerLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#CBD5E1")
            strokeWidth = 1f
        }
        canvas.drawLine(26f, 796f, PAGE_WIDTH_PT - 26f, 796f, footerLinePaint)
        val footerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#64748B")
            textSize = 8.5f
        }
        canvas.drawText(
            "NET4CITY • Game Edukasi Pembelajaran Jaringan • Cetak di kertas HVS / Karton 160-210 gsm",
            28f,
            814f,
            footerTextPaint
        )
        val pageNumPaint = Paint(footerTextPaint).apply {
            textAlign = Paint.Align.RIGHT
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        canvas.drawText("Halaman $pageIndex / $totalPages", PAGE_WIDTH_PT - 28f, 814f, pageNumPaint)

        qrBitmap?.recycle()
        bar1DBitmap?.recycle()
    }

    private fun drawLegendBar(
        canvas: Canvas,
        startX: Float,
        y: Float,
        solidPaint: Paint,
        dashedPaint: Paint
    ) {
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#334155")
            textSize = 8.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        // Cut line sample
        canvas.drawLine(startX, y - 3f, startX + 26f, y - 3f, solidPaint)
        canvas.drawText("Garis Solid = GUNTING", startX + 32f, y, textPaint)

        // Fold line sample
        val x2 = startX + 165f
        canvas.drawLine(x2, y - 3f, x2 + 26f, y - 3f, dashedPaint)
        canvas.drawText("Garis Putus = LIPAT 90°", x2 + 32f, y, textPaint)

        // Glue tab sample
        val x3 = startX + 335f
        val box = RectF(x3, y - 10f, x3 + 22f, y + 1f)
        val fill = Paint().apply { color = Color.parseColor("#E2E8F0") }
        canvas.drawRect(box, fill)
        canvas.drawRect(box, solidPaint)
        canvas.drawText("Area LEM = Oles Lem & Rekatkan", x3 + 28f, y, textPaint)
    }

    private fun drawGlueTab(
        canvas: Canvas,
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        dx: Float,
        dy: Float,
        fillPaint: Paint,
        hatchPaint: Paint,
        borderPaint: Paint,
        textPaint: Paint
    ) {
        // Taper the trapezoid ends by 12pt so adjacent glue tabs never collide when folded
        val taper = 12f
        val isHorizontal = y1 == y2
        val p1x = if (isHorizontal) x1 + taper else x1
        val p1y = if (isHorizontal) y1 else y1 + taper
        val p2x = if (isHorizontal) x2 - taper else x2
        val p2y = if (isHorizontal) y2 else y2 - taper

        val path = Path().apply {
            moveTo(x1, y1)
            lineTo(p1x + dx, p1y + dy)
            lineTo(p2x + dx, p2y + dy)
            lineTo(x2, y2)
            close()
        }
        canvas.drawPath(path, fillPaint)

        // Draw subtle diagonal hatch marks inside tab
        val steps = 6
        for (i in 1 until steps) {
            val t = i / steps.toFloat()
            val bx = x1 + (x2 - x1) * t
            val by = y1 + (y2 - y1) * t
            val tx = (p1x + dx) + ((p2x + dx) - (p1x + dx)) * t
            val ty = (p1y + dy) + ((p2y + dy) - (p1y + dy)) * t
            canvas.drawLine(bx, by, tx, ty, hatchPaint)
        }

        // Draw only the 3 outer cut edges of the trapezoid
        val outerPath = Path().apply {
            moveTo(x1, y1)
            lineTo(p1x + dx, p1y + dy)
            lineTo(p2x + dx, p2y + dy)
            lineTo(x2, y2)
        }
        canvas.drawPath(outerPath, borderPaint)

        // Centered "LEM" text
        val cx = (x1 + x2) / 2f + dx * 0.52f
        val cy = (y1 + y2) / 2f + dy * 0.52f
        canvas.save()
        if (!isHorizontal) {
            canvas.rotate(if (dx < 0) -90f else 90f, cx, cy)
        }
        canvas.drawText("LEM", cx, cy + 2.5f, textPaint)
        canvas.restore()
    }

    private fun drawAtapFace(
        canvas: Canvas,
        rect: RectF,
        module: DioramaModule,
        barcodeCode: String,
        palette: ModulePalette,
        qrBitmap: Bitmap?
    ) {
        val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = palette.roof }
        canvas.drawRect(rect, bg)

        val inner = RectF(rect.left + 8f, rect.top + 8f, rect.right - 8f, rect.bottom - 8f)
        val innerBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        canvas.drawRoundRect(inner, 8f, 8f, innerBg)

        val headerText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = palette.primary
            textSize = 7.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("ATAP • ${module.moduleCode}", rect.centerX(), rect.top + 19f, headerText)

        if (qrBitmap != null) {
            val qrSize = 68f
            val qrRect = RectF(
                rect.centerX() - qrSize / 2f,
                rect.top + 23f,
                rect.centerX() + qrSize / 2f,
                rect.top + 23f + qrSize
            )
            canvas.drawBitmap(qrBitmap, null, qrRect, null)
        }

        val codePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            textSize = 7.5f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(barcodeCode, rect.centerX(), rect.bottom - 12f, codePaint)
    }

    private fun drawDepanFace(
        canvas: Canvas,
        rect: RectF,
        module: DioramaModule,
        barcodeCode: String,
        palette: ModulePalette,
        bar1DBitmap: Bitmap?
    ) {
        val wallPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = palette.wallTint }
        canvas.drawRect(rect, wallPaint)

        // Top fascia header strip
        val fascia = RectF(rect.left, rect.top, rect.right, rect.top + 24f)
        val fasciaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = palette.primary }
        canvas.drawRect(fascia, fasciaPaint)

        val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 7.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val shortName = if (module.name.length > 20) module.name.take(19) + "…" else module.name
        canvas.drawText(shortName.uppercase(), rect.centerX(), rect.top + 15f, namePaint)

        // Stylized cartoon building windows & entrance door
        val winPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#BAE6FD") }
        val winBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = palette.primary
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        val doorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = palette.accent }

        for (col in 0..2) {
            val wx = rect.left + 14f + col * 33f
            val wy = rect.top + 32f
            val wRect = RectF(wx, wy, wx + 24f, wy + 18f)
            canvas.drawRoundRect(wRect, 3f, 3f, winPaint)
            canvas.drawRoundRect(wRect, 3f, 3f, winBorder)
        }

        // Entrance door + mini label
        val doorRect = RectF(rect.centerX() - 12f, rect.top + 56f, rect.centerX() + 12f, rect.top + 74f)
        canvas.drawRoundRect(doorRect, 3f, 3f, doorPaint)

        // White sticker panel for scannable 1D Code-128 Barcode on lower facade
        val barBox = RectF(rect.left + 7f, rect.bottom - 42f, rect.right - 7f, rect.bottom - 6f)
        val whitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        canvas.drawRoundRect(barBox, 4f, 4f, whitePaint)
        canvas.drawRoundRect(barBox, 4f, 4f, winBorder)

        if (bar1DBitmap != null) {
            val dest = RectF(barBox.left + 4f, barBox.top + 3f, barBox.right - 4f, barBox.bottom - 12f)
            canvas.drawBitmap(bar1DBitmap, null, dest, null)
        }
        val codeText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 7f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(barcodeCode, barBox.centerX(), barBox.bottom - 3f, codeText)
    }

    private fun drawSideWallFace(
        canvas: Canvas,
        rect: RectF,
        sideLabel: String,
        module: DioramaModule,
        palette: ModulePalette,
        isLeft: Boolean
    ) {
        val wallPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = palette.wallTint }
        canvas.drawRect(rect, wallPaint)

        // Roof trim stripe
        val trim = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = palette.roof }
        canvas.drawRect(rect.left, rect.top, rect.right, rect.top + 14f, trim)

        val trimText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 7f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(sideLabel, rect.centerX(), rect.top + 10f, trimText)

        // Architectural side windows (2x2)
        val winPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#E0F2FE") }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = palette.primary
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        for (row in 0..1) {
            for (col in 0..1) {
                val wx = rect.left + 18f + col * 48f
                val wy = rect.top + 24f + row * 28f
                val wr = RectF(wx, wy, wx + 34f, wy + 20f)
                canvas.drawRoundRect(wr, 3f, 3f, winPaint)
                canvas.drawRoundRect(wr, 3f, 3f, borderPaint)
            }
        }

        // Bottom network cable port diagram
        val portBox = RectF(rect.left + 12f, rect.bottom - 30f, rect.right - 12f, rect.bottom - 8f)
        val portBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        canvas.drawRoundRect(portBox, 5f, 5f, portBg)
        canvas.drawRoundRect(portBox, 5f, 5f, borderPaint)

        val infoText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = palette.primary
            textSize = 6.8f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val caption = if (isLeft) "PORT KABEL UTP / FIBER" else "NET4CITY • ${module.moduleCode}"
        canvas.drawText(caption, portBox.centerX(), portBox.centerY() + 2.5f, infoText)
    }

    private fun drawBelakangFace(
        canvas: Canvas,
        rect: RectF,
        module: DioramaModule,
        barcodeCode: String,
        palette: ModulePalette,
        qrBitmap: Bitmap?
    ) {
        val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        canvas.drawRect(rect, bg)

        val strip = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = palette.primary }
        canvas.drawRect(rect.left, rect.top, rect.right, rect.top + 18f, strip)

        val header = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 7.2f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("BELAKANG • SCAN QR", rect.centerX(), rect.top + 12f, header)

        if (qrBitmap != null) {
            val qrSize = 66f
            val qrRect = RectF(
                rect.centerX() - qrSize / 2f,
                rect.top + 24f,
                rect.centerX() + qrSize / 2f,
                rect.top + 24f + qrSize
            )
            canvas.drawBitmap(qrBitmap, null, qrRect, null)
        }

        val mono = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 7.5f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(barcodeCode, rect.centerX(), rect.bottom - 18f, mono)

        val loc = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#475569")
            textSize = 6.5f
            textAlign = Paint.Align.CENTER
        }
        val shortLoc = if (module.storageLocation.length > 22) module.storageLocation.take(21) + "…" else module.storageLocation
        canvas.drawText(shortLoc, rect.centerX(), rect.bottom - 7f, loc)
    }

    private fun drawAlasFace(
        canvas: Canvas,
        rect: RectF,
        module: DioramaModule,
        barcodeCode: String,
        palette: ModulePalette
    ) {
        val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#F8FAFC") }
        canvas.drawRect(rect, bg)

        val title = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = palette.primary
            textSize = 8.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val body = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#475569")
            textSize = 7.2f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("↑ SISI DEPAN ↑", rect.centerX(), rect.top + 22f, title)
        canvas.drawText("ALAS DIORAMA", rect.centerX(), rect.centerY() - 6f, title)
        canvas.drawText(module.moduleCode, rect.centerX(), rect.centerY() + 8f, body)
        canvas.drawText("QR: $barcodeCode", rect.centerX(), rect.centerY() + 20f, body)
        canvas.drawText("Tempel pemberat/karton", rect.centerX(), rect.bottom - 16f, body)
        canvas.drawText("agar bangunan kokoh", rect.centerX(), rect.bottom - 7f, body)
    }

    private fun drawCutOutCardAndInstructions(
        canvas: Canvas,
        topY: Float,
        module: DioramaModule,
        barcodeCode: String,
        palette: ModulePalette,
        qrBitmap: Bitmap?,
        bar1DBitmap: Bitmap?
    ) {
        val cutBoxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            style = Paint.Style.STROKE
            strokeWidth = 1.2f
            pathEffect = DashPathEffect(floatArrayOf(6f, 4f), 0f)
        }
        val sectionHeader = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = palette.primary
            textSize = 10f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        canvas.drawText("KARTU BARCODE MEJA DIORAMA (OPSIONAL — GUNTING & TEMPEL DI PAPAN)", 28f, topY - 6f, sectionHeader)

        // Left Card: Cut-Out Table Barcode Label (QR + 1D Barcode)
        val cardLeft = RectF(28f, topY + 4f, 305f, topY + 216f)
        val cardFill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = palette.wallTint }
        canvas.drawRoundRect(cardLeft, 10f, 10f, cardFill)
        canvas.drawRoundRect(cardLeft, 10f, 10f, cutBoxPaint)

        // Top accent bar inside cut-out card
        val barTop = RectF(cardLeft.left + 8f, cardLeft.top + 8f, cardLeft.right - 8f, cardLeft.top + 36f)
        val barTopPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = palette.primary }
        canvas.drawRoundRect(barTop, 6f, 6f, barTopPaint)

        val cardTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 9.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        canvas.drawText("${module.moduleCode} • ${module.name}", barTop.left + 8f, barTop.top + 18f, cardTitlePaint)

        // QR Code on left side of cut-out card
        val qrBg = RectF(cardLeft.left + 12f, cardLeft.top + 46f, cardLeft.left + 104f, cardLeft.top + 138f)
        val whitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        canvas.drawRoundRect(qrBg, 6f, 6f, whitePaint)
        if (qrBitmap != null) {
            val dest = Rect(
                (qrBg.left + 5).toInt(),
                (qrBg.top + 5).toInt(),
                (qrBg.right - 5).toInt(),
                (qrBg.bottom - 5).toInt()
            )
            canvas.drawBitmap(qrBitmap, null, dest, null)
        }

        // Metadata next to QR
        val metaBold = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            textSize = 8.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        val metaSmall = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#334155")
            textSize = 7.8f
        }
        val mx = qrBg.right + 10f
        canvas.drawText("ID Barcode Permanen:", mx, cardLeft.top + 58f, metaSmall)
        canvas.drawText(barcodeCode, mx, cardLeft.top + 72f, metaBold)
        canvas.drawText("Kategori: ${module.category.ifBlank { "Diorama" }}", mx, cardLeft.top + 90f, metaSmall)
        canvas.drawText("Lokasi: ${module.storageLocation.take(22)}", mx, cardLeft.top + 104f, metaSmall)
        canvas.drawText("Scan via USB Scanner PID", mx, cardLeft.top + 122f, metaBold)
        canvas.drawText("untuk aktifkan modul virtual", mx, cardLeft.top + 134f, metaSmall)

        // Large 1D Code-128 Barcode along bottom of cut-out card
        val bar1DBox = RectF(cardLeft.left + 12f, cardLeft.top + 148f, cardLeft.right - 12f, cardLeft.bottom - 10f)
        canvas.drawRoundRect(bar1DBox, 6f, 6f, whitePaint)
        if (bar1DBitmap != null) {
            val dest = RectF(bar1DBox.left + 10f, bar1DBox.top + 5f, bar1DBox.right - 10f, bar1DBox.bottom - 16f)
            canvas.drawBitmap(bar1DBitmap, null, dest, null)
        }
        val barCaption = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 9f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(barcodeCode, bar1DBox.centerX(), bar1DBox.bottom - 4f, barCaption)

        // Right Card: Step-by-step folding guide + Student Group Stamp
        val cardRight = RectF(318f, topY + 4f, PAGE_WIDTH_PT - 28f, topY + 216f)
        val rightFill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#F8FAFC") }
        val rightStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#CBD5E1")
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        canvas.drawRoundRect(cardRight, 10f, 10f, rightFill)
        canvas.drawRoundRect(cardRight, 10f, 10f, rightStroke)

        val rx = cardRight.left + 14f
        canvas.drawText("PANDUAN MERAKIT DIORAMA KERTAS 3D", rx, cardRight.top + 22f, sectionHeader)
        val steps = listOf(
            "1. Cetak pola di kertas HVS tebal / karton 160-210 gsm.",
            "2. Gunting rapi mengikuti seluruh GARIS SOLID terluar.",
            "3. Lipat 90° ke dalam pada semua GARIS PUTUS-PUTUS.",
            "4. Oleskan lem kertas pada 7 lidah miring berlabel LEM.",
            "5. Rangkai jadi kubus bangunan 3D & tempel di papan diorama.",
            "6. Pindai QR atau Barcode 1D dengan USB Scanner di Mode PID."
        )
        var sy = cardRight.top + 42f
        steps.forEach { step ->
            canvas.drawText(step, rx, sy, metaSmall)
            sy += 17f
        }

        // Student/Group note lines
        val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#CBD5E1")
            strokeWidth = 0.9f
        }
        canvas.drawLine(rx, cardRight.bottom - 52f, cardRight.right - 14f, cardRight.bottom - 52f, dividerPaint)
        canvas.drawText("Kelompok : __________________________________", rx, cardRight.bottom - 34f, metaSmall)
        canvas.drawText("Kelas    : ___________________  Tgl: ___/___/___", rx, cardRight.bottom - 16f, metaSmall)
    }
}
