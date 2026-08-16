package com.example.employeeprofile.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.example.employeeprofile.data.model.Employee
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGPointMake
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.UIKit.NSFontAttributeName
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIFont
import platform.UIKit.UIGraphicsBeginPDFContextToFile
import platform.UIKit.UIGraphicsBeginPDFPage
import platform.UIKit.UIGraphicsEndPDFContext
import platform.UIKit.drawAtPoint

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberPdfExporter(onResult: (String) -> Unit): PdfExporter =
    remember(onResult) {
        object : PdfExporter {
            override fun export(employees: List<Employee>) {
                if (employees.isEmpty()) {
                    onResult("There's nothing to export yet")
                    return
                }
                val path = writePdf(employees)
                if (path == null) {
                    onResult("Couldn't write the PDF")
                    return
                }
                share(path)
                onResult("Exported ${employees.size} employees")
            }
        }
    }

/** Lays the roster out over as many pages as it takes and writes the file. */
@OptIn(ExperimentalForeignApi::class)
private fun writePdf(employees: List<Employee>): String? {
    val directory = documentsPath() + "/" + EXPORT_DIRECTORY
    NSFileManager.defaultManager.createDirectoryAtPath(directory, true, null, null)
    val path = "$directory/${PdfLayout.FILE_NAME}"

    val title = mapOf<Any?, Any?>(
        NSFontAttributeName to UIFont.boldSystemFontOfSize(PdfLayout.TITLE_SIZE.toDouble())
    )
    val header = mapOf<Any?, Any?>(
        NSFontAttributeName to UIFont.boldSystemFontOfSize(PdfLayout.HEADER_SIZE.toDouble())
    )
    val body = mapOf<Any?, Any?>(
        NSFontAttributeName to UIFont.systemFontOfSize(PdfLayout.BODY_SIZE.toDouble())
    )

    val bounds = CGRectMake(
        x = 0.0,
        y = 0.0,
        width = PdfLayout.PAGE_WIDTH.toDouble(),
        height = PdfLayout.PAGE_HEIGHT.toDouble()
    )
    UIGraphicsBeginPDFContextToFile(path, bounds, null)
    UIGraphicsBeginPDFPage()

    var y = PdfLayout.MARGIN
    (pdfTitle(employees.size) as NSString)
        .drawAtPoint(CGPointMake(PdfLayout.MARGIN.toDouble(), y.toDouble()), title)
    y += PdfLayout.TITLE_GAP
    PdfLayout.HEADERS.forEachIndexed { column, text ->
        (text as NSString).drawAtPoint(columnPoint(column, y), header)
    }
    y += PdfLayout.ROW_HEIGHT

    for (employee in employees) {
        if (y > PdfLayout.LAST_BASELINE) {
            UIGraphicsBeginPDFPage()
            y = PdfLayout.MARGIN
        }
        employeeRow(employee).forEachIndexed { column, text ->
            (text as NSString).drawAtPoint(columnPoint(column, y), body)
        }
        y += PdfLayout.ROW_HEIGHT
    }
    UIGraphicsEndPDFContext()

    return if (NSFileManager.defaultManager.fileExistsAtPath(path)) path else null
}

@OptIn(ExperimentalForeignApi::class)
private fun columnPoint(column: Int, y: Float) = CGPointMake(
    x = (PdfLayout.MARGIN + PdfLayout.COLUMNS[column]).toDouble(),
    y = y.toDouble()
)

/** Offers the file through the system share sheet. */
private fun share(path: String) {
    val controller = UIActivityViewController(
        activityItems = listOf(NSURL.fileURLWithPath(path)),
        applicationActivities = null
    )
    rootViewController()?.presentViewController(controller, animated = true, completion = null)
}
