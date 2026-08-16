package com.example.employeeprofile.platform

import androidx.compose.runtime.Composable
import com.example.employeeprofile.data.model.Employee
import com.example.employeeprofile.view.formatDate
import com.example.employeeprofile.view.formatSalary

/** Writes the roster to a PDF and hands it to the platform's share sheet. */
interface PdfExporter {
    fun export(employees: List<Employee>)
}

/**
 * The platform's PDF writer. A composable for the same reason the picker is one: Android needs
 * a context, and iOS needs a view controller to present the share sheet from.
 *
 * [onResult] carries a sentence for the snackbar, whether it worked or not.
 */
@Composable
expect fun rememberPdfExporter(onResult: (String) -> Unit): PdfExporter

/**
 * Page geometry and column positions, shared so the document comes out the same on both
 * platforms. Points at 72dpi — the unit both `PdfDocument` and `UIGraphicsPDFContext` use —
 * which makes these A4.
 */
internal object PdfLayout {
    const val PAGE_WIDTH = 595
    const val PAGE_HEIGHT = 842
    const val MARGIN = 40f
    const val TITLE_SIZE = 18f
    const val HEADER_SIZE = 10f
    const val BODY_SIZE = 9f
    const val ROW_HEIGHT = 20f
    const val TITLE_GAP = 34f

    /** Column offsets from the left margin. */
    val COLUMNS = listOf(0f, 175f, 285f, 355f, 445f)
    val HEADERS = listOf("Name", "Department", "Type", "Joined", "Salary")

    /** The last baseline that still fits above the bottom margin. */
    const val LAST_BASELINE = PAGE_HEIGHT - MARGIN

    const val FILE_NAME = "employees.pdf"
}

/** One line per employee, in the same order as [PdfLayout.HEADERS]. */
internal fun employeeRow(employee: Employee): List<String> = listOf(
    employee.fullName + if (employee.isActive) "" else "  (inactive)",
    employee.department.label,
    employee.employmentType.label,
    formatDate(employee.joiningDate),
    formatSalary(employee.salary)
)

internal fun pdfTitle(count: Int): String =
    if (count == 1) "Employees (1 record)" else "Employees ($count records)"
