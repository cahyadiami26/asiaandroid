package com.example

import com.example.core.format.Formatters
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTestSuite {

    @Test
    fun `currency formatting works correctly`() {
        val formatted = Formatters.formatRupiah(15830000)
        assertEquals("Rp 15.830.000", formatted)

        val compact = Formatters.formatRupiahCompact(15830000)
        assertTrue(compact.contains("15,8 jt") || compact.contains("15.8 jt"))
    }

    @Test
    fun `date and days formatting works correctly`() {
        val days = Formatters.formatDays(3.5)
        assertEquals("3,5 hari", days)

        val wholeDays = Formatters.formatDays(4.0)
        assertEquals("4 hari", wholeDays)
    }

    @Test
    fun `time formatting works correctly`() {
        val time = Formatters.formatTimeOnly("08:45")
        assertEquals("08:45", time)
    }
}
