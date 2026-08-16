package com.example.employeeprofile.platform

import com.example.employeeprofile.data.model.ResumeDocument
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSMutableData
import platform.Foundation.NSURL
import platform.Foundation.dataWithLength
import platform.Foundation.writeToFile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Exercises what happens after UIKit hands over a picked document. The picker itself needs a
 * person to choose a file, but everything downstream of that — reading it, judging its size,
 * copying it into app storage — is what actually decides whether the resume attaches.
 */
@OptIn(ExperimentalForeignApi::class)
class DocumentPickerIosTest {

    /** Writes a file of [bytes] length into the app's own storage and returns its URL. */
    private fun fileOf(name: String, bytes: Int): NSURL {
        val path = documentsPath() + "/" + name
        val data = assertNotNull(NSMutableData.dataWithLength(bytes.toULong()))
        assertTrue(data.writeToFile(path, true), "couldn't stage the test file")
        return NSURL.fileURLWithPath(path)
    }

    @Test
    fun `a document within the limit is copied into app storage`() {
        var picked: PickedFile? = null
        var error: String? = null
        readPickedDocument(fileOf("cv.pdf", 2048), { picked = it }, { error = it })

        assertNull(error)
        val file = assertNotNull(picked)
        assertEquals("cv.pdf", file.name)
        assertEquals("application/pdf", file.mimeType)
        assertEquals(2048L, file.sizeBytes)
        assertTrue(MEDIA_DIRECTORY in file.path)
    }

    @Test
    fun `a document over five megabytes is refused`() {
        var picked: PickedFile? = null
        var error: String? = null
        val tooBig = (ResumeDocument.MAX_BYTES + 1).toInt()
        readPickedDocument(fileOf("huge.pdf", tooBig), { picked = it }, { error = it })

        assertNull(picked, "an oversized file should not attach")
        assertEquals(ResumeDocument.TOO_LARGE_MESSAGE, error)
    }

    @Test
    fun `a file exactly on the limit is allowed`() {
        var picked: PickedFile? = null
        readPickedDocument(
            fileOf("exact.pdf", ResumeDocument.MAX_BYTES.toInt()),
            { picked = it },
            {})
        assertNotNull(picked)
    }

    @Test
    fun `a url pointing at nothing reports rather than crashing`() {
        var error: String? = null
        readPickedDocument(
            NSURL.fileURLWithPath(documentsPath() + "/missing.pdf"),
            {},
            { error = it })
        assertEquals("Couldn't read that file", error)
    }

    @Test
    fun `mime types are mapped from the extension`() {
        assertEquals("application/pdf", mimeTypeFor("cv.pdf"))
        assertEquals("application/msword", mimeTypeFor("cv.doc"))
        assertEquals(
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            mimeTypeFor("cv.docx")
        )
        assertEquals("application/pdf", mimeTypeFor("CV.PDF"))
        assertEquals("", mimeTypeFor("notes.txt"))
    }

}
