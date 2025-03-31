package com.appcoins.communication.requester

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
class MessageRequesterSenderTest {

    private lateinit var messageRequesterSender: MessageRequesterSender

    private val mockkContext = mockk<Context>()

    @Test(expected = NullPointerException::class)
    fun `should throw NullPointerException if URI is null`() {
        setup("", null, "")

        messageRequesterSender.sendMessage(DEFAULT_REQUEST_CODE, DEFAULT_METHOD_ID, DEFAULT_PARCELABLE)
    }

    @Test
    fun `should start activity with values for message`() {
        setup("", "", "")

        every { mockkContext.packageName } returns DEFAULT_PACKAGE_NAME
        every { mockkContext.startActivity(any()) } just runs

        messageRequesterSender.sendMessage(DEFAULT_REQUEST_CODE, DEFAULT_METHOD_ID, DEFAULT_PARCELABLE)

        verify(exactly = 1) {
            mockkContext.packageName
            mockkContext.startActivity(any())
        }
    }

    private fun setup(targetPackage: String?, targetUri: String?, requesterActivityUri: String?) {
        messageRequesterSender =
            MessageRequesterSender(
                mockkContext,
                targetPackage,
                targetUri,
                requesterActivityUri
            )
    }

    private companion object {
        const val DEFAULT_REQUEST_CODE = 0L
        const val DEFAULT_METHOD_ID = 0
        val DEFAULT_PARCELABLE = Bundle()

        const val DEFAULT_PACKAGE_NAME = "PACKAGE_NAME"
    }
}
