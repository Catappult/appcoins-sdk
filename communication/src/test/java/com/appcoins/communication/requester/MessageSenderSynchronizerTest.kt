package com.appcoins.communication.requester

import android.os.Bundle
import android.os.Parcelable
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.Callable
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class MessageSenderSynchronizerTest {

    private lateinit var messageSenderSynchronizer: MessageSenderSynchronizer

    @Test
    fun `should return null in Timeout case`() {
        setup(SMALL_TIMEOUT)

        val callableTimeout = Callable<Parcelable> {
            Thread.currentThread().interrupt()
            Thread.sleep(5000)
            return@Callable Bundle()
        }

        val result = messageSenderSynchronizer.addTaskToQueue(callableTimeout)

        assertEquals(null, result)
    }

    @Test
    fun `should return Bundle in success case`() {
        setup(DEFAULT_TIMEOUT)

        val bundle = Bundle()

        val callableTimeout = Callable<Parcelable> {
            return@Callable bundle
        }

        val result = messageSenderSynchronizer.addTaskToQueue(callableTimeout)

        assertEquals(bundle, result)
    }

    private fun setup(timeout: Int) {
        messageSenderSynchronizer = MessageSenderSynchronizer(timeout)
    }

    private companion object {
        const val DEFAULT_TIMEOUT = 3000
        const val SMALL_TIMEOUT = 1000
        val DEFAULT_PARCELABLE = Bundle()
    }
}
