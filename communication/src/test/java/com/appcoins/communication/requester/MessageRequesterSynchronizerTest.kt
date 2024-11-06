package com.appcoins.communication.requester

import android.os.Bundle
import io.mockk.every
import io.mockk.just
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class MessageRequesterSynchronizerTest {

    private lateinit var messageRequesterSynchronizer: MessageRequesterSynchronizer

    @Before
    fun setup() {
        mockkStatic(StaticMessageResponseSynchronizer::class)
        every { StaticMessageResponseSynchronizer.init() } just runs

        messageRequesterSynchronizer = MessageRequesterSynchronizer()
    }

    @Test(expected = InterruptedException::class)
    fun `should throw InterruptedException if URI is null`() {
        every {
            StaticMessageResponseSynchronizer.waitMessage(DEFAULT_REQUEST_CODE, DEFAULT_TIMEOUT)
        } throws InterruptedException()

        messageRequesterSynchronizer.waitMessage(DEFAULT_REQUEST_CODE, DEFAULT_TIMEOUT)
    }

    @Test
    fun `should start activity with values for message`() {
        every {
            StaticMessageResponseSynchronizer.waitMessage(DEFAULT_REQUEST_CODE, DEFAULT_TIMEOUT)
        } returns DEFAULT_PARCELABLE

        val result = messageRequesterSynchronizer.waitMessage(DEFAULT_REQUEST_CODE, DEFAULT_TIMEOUT)

        assertEquals(DEFAULT_PARCELABLE, result)
        verify(exactly = 1) {
            StaticMessageResponseSynchronizer.waitMessage(DEFAULT_REQUEST_CODE, DEFAULT_TIMEOUT)
        }
    }

    private companion object {
        const val DEFAULT_REQUEST_CODE = 0L
        const val DEFAULT_TIMEOUT = 0
        val DEFAULT_PARCELABLE = Bundle()
    }
}
