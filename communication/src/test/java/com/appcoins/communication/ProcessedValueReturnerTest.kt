package com.appcoins.communication

import android.content.Context
import android.os.Bundle
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ProcessedValueReturnerTest {

    private lateinit var processedValueReturner: ProcessedValueReturner

    private val mockkContext = mockk<Context>()

    @Test(expected = NullPointerException::class)
    fun `should throw NullPointerException if URI is null`() {
        setup(null)

        processedValueReturner.returnValue(DEFAULT_PACKAGE_NAME, DEFAULT_REQUEST_CODE, DEFAULT_PARCELABLE)
    }

    @Test
    fun `should start activity with values for message`() {
        setup("")

        every { mockkContext.startActivity(any()) } just runs

        processedValueReturner.returnValue(DEFAULT_PACKAGE_NAME, DEFAULT_REQUEST_CODE, DEFAULT_PARCELABLE)

        verify(exactly = 1) {
            mockkContext.startActivity(any())
        }
    }

    private fun setup(senderUri: String?) {
        processedValueReturner = ProcessedValueReturner(mockkContext, senderUri)
    }

    private companion object {
        const val DEFAULT_REQUEST_CODE = 0L
        val DEFAULT_PARCELABLE = Bundle()

        const val DEFAULT_PACKAGE_NAME = "PACKAGE_NAME"
    }
}
