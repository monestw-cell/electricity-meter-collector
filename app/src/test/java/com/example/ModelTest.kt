package com.example

import com.example.data.model.Settings
import com.example.data.model.Subscriber
import com.example.data.model.InvoiceHistory
import org.junit.Assert.assertEquals
import org.junit.Test

class ModelTest {
    @Test
    fun testSettingsDefaultValues() {
        val settings = Settings()
        assertEquals(1, settings.id)
        assertEquals(0.5, settings.pricePerKw, 0.0)
    }
}
