package com.example.employeeprofile.platform

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.employeeprofile.data.model.Employee
import java.io.File

@Composable
actual fun rememberPdfExporter(onResult: (String) -> Unit): PdfExporter {
    val context = LocalContext.current
    return remember(context, onResult) {
        object : PdfExporter {
            override fun export(employees: List<Employee>) {
                if (employees.isEmpty()) {
                    onResult("There's nothing to export yet")
                    return
                }
                val file = runCatching { context.writePdf(employees) }.getOrNull()
                if (file == null) {
                    onResult("Couldn't write the PDF")
                    return
                }
                context.share(file)
                onResult("Exported ${employees.size} employees")
            }
        }
    }
}

/** Lays the roster out over as many pages as it takes and writes the file. */
private fun Context.writePdf(employees: List<Employee>): File {
    val document = PdfDocument()
    val title = Paint().apply {
        textSize = PdfLayout.TITLE_SIZE
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    val header = Paint().apply {
        textSize = PdfLayout.HEADER_SIZE
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    val body = Paint().apply { textSize = PdfLayout.BODY_SIZE }

    var pageNumber = 1
    var page = document.startPage(newPageInfo(pageNumber))
    var y = PdfLayout.MARGIN + PdfLayout.TITLE_SIZE

    page.canvas.drawText(pdfTitle(employees.size), PdfLayout.MARGIN, y, title)
    y += PdfLayout.TITLE_GAP
    PdfLayout.HEADERS.forEachIndexed { column, text ->
        page.canvas.drawText(text, PdfLayout.MARGIN + PdfLayout.COLUMNS[column], y, header)
    }
    y += PdfLayout.ROW_HEIGHT

    for (employee in employees) {
        if (y > PdfLayout.LAST_BASELINE) {
            document.finishPage(page)
            pageNumber++
            page = document.startPage(newPageInfo(pageNumber))
            y = PdfLayout.MARGIN + PdfLayout.ROW_HEIGHT
        }
        employeeRow(employee).forEachIndexed { column, text ->
            page.canvas.drawText(text, PdfLayout.MARGIN + PdfLayout.COLUMNS[column], y, body)
        }
        y += PdfLayout.ROW_HEIGHT
    }
    document.finishPage(page)

    // Not stamped with a timestamp: each export replaces the last rather than piling up.
    val file = appFile(EXPORT_DIRECTORY, PdfLayout.FILE_NAME, unique = false)
    file.outputStream().use(document::writeTo)
    document.close()
    return file
}

private fun newPageInfo(pageNumber: Int) =
    PdfDocument.PageInfo.Builder(PdfLayout.PAGE_WIDTH, PdfLayout.PAGE_HEIGHT, pageNumber).create()

/** Hands the file to whatever the user has installed, through the same provider the camera uses. */
private fun Context.share(file: File) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, sharableUri(file))
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    startActivity(
        Intent.createChooser(send, "Share employee list")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}
