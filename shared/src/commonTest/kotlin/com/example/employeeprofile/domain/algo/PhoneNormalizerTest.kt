package com.example.employeeprofile.domain.algo

import kotlin.test.Test
import kotlin.test.assertEquals

class PhoneNormalizerTest {

    @Test
    fun `strips spaces dashes brackets and the leading plus`() {
        assertEquals("9876543210", normalizePhone("+91 (98765) 432-10"))
    }

    @Test
    fun `leaves an already clean local number alone`() {
        assertEquals("9876543210", normalizePhone("9876543210"))
    }

    @Test
    fun `removes the country code when one is present`() {
        assertEquals("9876543210", normalizePhone("919876543210"))
    }

    @Test
    fun `removes a trunk prefix when one is present`() {
        assertEquals("9876543210", normalizePhone("09876543210"))
    }

    /** The spec's own snippet strips "91" unconditionally, which breaks this number. */
    @Test
    fun `keeps a local number that happens to start with the country code`() {
        assertEquals("9187654321", normalizePhone("9187654321"))
    }

    @Test
    fun `empty input stays empty`() {
        assertEquals("", normalizePhone(""))
    }
}
