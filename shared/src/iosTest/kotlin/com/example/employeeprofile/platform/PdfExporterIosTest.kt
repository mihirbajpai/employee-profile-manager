package com.example.employeeprofile.platform

import com.example.employeeprofile.employee
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSData
import platform.Foundation.NSASCIIStringEncoding
import platform.Foundation.NSFileManager
import platform.Foundation.NSMakeRange
import platform.Foundation.NSString
import platform.Foundation.dataUsingEncoding
import platform.Foundation.isEqualToData
import platform.Foundation.subdataWithRange
import platform.Foundation.dataWithContentsOfFile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Runs the iOS PDF writer for real on the simulator. The UIKit drawing calls compile whatever
 * they're handed, so the only way to know the document is actually produced — and is a PDF —
 * is to generate one and read the bytes back.
 */
@OptIn(ExperimentalForeignApi::class)
class PdfExporterIosTest {

    private fun bytesAt(path: String): NSData? = NSData.dataWithContentsOfFile(path)

    @Test
    fun `writes a file to the exports directory`() {
        val path = writePdf(listOf(employee(id = 1)))
        assertNotNull(path, "writePdf returned no path")
        assertTrue(NSFileManager.defaultManager.fileExistsAtPath(path))
        assertTrue(path.endsWith("/${PdfLayout.FILE_NAME}"))
        assertTrue(EXPORT_DIRECTORY in path)
    }

    /** A PDF begins with the %PDF- header; anything else means UIKit wrote us a dud. */
    @Test
    fun `the file it writes really is a pdf`() {
        val path = assertNotNull(writePdf(listOf(employee(id = 1))))
        val data = assertNotNull(bytesAt(path))
        assertTrue(data.length.toInt() > 0, "the PDF is empty")

        val expected = ("%PDF-" as NSString).dataUsingEncoding(NSASCIIStringEncoding)
        val actual = data.subdataWithRange(NSMakeRange(0uL, 5uL))
        assertTrue(actual.isEqualToData(assertNotNull(expected)), "missing the %PDF- header")
    }

    @Test
    fun `a longer roster produces a larger document`() {
        val small = assertNotNull(writePdf(listOf(employee(id = 1))))
        val smallSize = assertNotNull(bytesAt(small)).length.toInt()

        val many = (1..80).map { employee(id = it.toLong(), fullName = "Person $it") }
        val large = assertNotNull(writePdf(many))
        val largeSize = assertNotNull(bytesAt(large)).length.toInt()

        assertTrue(largeSize > smallSize, "80 rows ($largeSize) should outweigh one ($smallSize)")
    }

    @Test
    fun `saveToAppStorage writes the bytes it is given and reports them`() {
        val source = assertNotNull(writePdf(listOf(employee(id = 1))))
        val data = assertNotNull(bytesAt(source))

        val saved = assertNotNull(saveToAppStorage(data, "resume.pdf", "application/pdf"))
        assertEquals("resume.pdf", saved.name)
        assertEquals("application/pdf", saved.mimeType)
        assertEquals(data.length.toLong(), saved.sizeBytes)
        assertTrue(NSFileManager.defaultManager.fileExistsAtPath(saved.path))
        assertTrue(MEDIA_DIRECTORY in saved.path)
    }

    @Test
    fun `documents directory resolves`() {
        assertTrue(documentsPath().isNotEmpty())
    }
}
