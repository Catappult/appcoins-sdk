package com.appcoins.communication.requester

import android.os.Bundle
import android.os.Looper
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.unmockkAll
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class IntentSyncIpcMessageSenderTest {

    private lateinit var intentSyncIpcMessageSender: IntentSyncIpcMessageSender

    private val mockkMessageSender = mockk<MessageRequesterSender>()
    private val mockkMessageResponseSynchronizer = mockk<MessageRequesterSynchronizer>()
    private val mockkIdGenerator = mockk<IdGenerator>()
    private val mockkMessageSenderSynchronizer = mockk<MessageSenderSynchronizer>()
    private val mockkTimeout = 0

    @Before
    fun setup() {
        intentSyncIpcMessageSender =
            IntentSyncIpcMessageSender(
                mockkMessageSender,
                mockkMessageResponseSynchronizer,
                mockkIdGenerator,
                mockkMessageSenderSynchronizer,
                mockkTimeout
            )
    }

    @Test(expected = MainThreadException::class)
    fun `should throw MainThreadException if sending message on MainThread`() {
        mockkStatic(Looper::class)
        every { Looper.myLooper() } returns Looper.getMainLooper()

        intentSyncIpcMessageSender.sendMessage(DEFAULT_METHOD_ID, DEFAULT_PARCELABLE)
        unmockkAll()
    }

    @Test
    fun `should start new executor to send message`() {
        mockkStatic(Looper::class)
        every { Looper.myLooper() } returns mockk<Looper>()
        every { mockkMessageSenderSynchronizer.addTaskToQueue(any()) } returns DEFAULT_PARCELABLE
        every { mockkIdGenerator.generateRequestCode() } returns DEFAULT_ID_GENERATED
        every { mockkMessageSender.sendMessage(DEFAULT_ID_GENERATED, DEFAULT_METHOD_ID, DEFAULT_PARCELABLE) } just runs
        every {
            mockkMessageResponseSynchronizer.waitMessage(DEFAULT_ID_GENERATED, mockkTimeout)
        } returns DEFAULT_PARCELABLE

        val result = intentSyncIpcMessageSender.sendMessage(DEFAULT_METHOD_ID, DEFAULT_PARCELABLE)

        assertEquals(DEFAULT_PARCELABLE, result)
        unmockkAll()
    }

    private companion object {
        const val DEFAULT_METHOD_ID = 0
        val DEFAULT_PARCELABLE = Bundle()

        const val DEFAULT_ID_GENERATED = 0L
    }
}
