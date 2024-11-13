package com.appcoins.sdk.billing

import com.appcoins.sdk.billing.exceptions.ServiceConnectionException
import com.appcoins.sdk.billing.listeners.ConsumeResponseListener
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ConsumeAsyncTest {

    private lateinit var consumeAsync: ConsumeAsync

    private val mockkRepository = mockk<Repository>()
    private val mockkConsumeResponseListener = mockk<ConsumeResponseListener>()

    @Test
    fun `should return DEVELOPER_ERROR when token is null`() {
        consumeAsync = ConsumeAsync(null, mockkConsumeResponseListener, mockkRepository)

        every {
            mockkConsumeResponseListener.onConsumeResponse(ResponseCode.DEVELOPER_ERROR.value, null)
        } just runs

        consumeAsync.run()

        verify(exactly = 1) {
            mockkConsumeResponseListener.onConsumeResponse(ResponseCode.DEVELOPER_ERROR.value, null)
        }
    }

    @Test
    fun `should return DEVELOPER_ERROR when token is empty`() {
        consumeAsync = ConsumeAsync("", mockkConsumeResponseListener, mockkRepository)

        every {
            mockkConsumeResponseListener.onConsumeResponse(ResponseCode.DEVELOPER_ERROR.value, null)
        } just runs

        consumeAsync.run()

        verify(exactly = 1) {
            mockkConsumeResponseListener.onConsumeResponse(ResponseCode.DEVELOPER_ERROR.value, null)
        }
    }

    @Test
    fun `should return repository response when repository handled correctly`() {
        val token = "token"
        consumeAsync = ConsumeAsync(token, mockkConsumeResponseListener, mockkRepository)

        every { mockkRepository.consumeAsync(token) } returns ResponseCode.OK.value
        every {
            mockkConsumeResponseListener.onConsumeResponse(ResponseCode.OK.value, token)
        } just runs

        consumeAsync.run()

        verify(exactly = 1) {
            mockkConsumeResponseListener.onConsumeResponse(ResponseCode.OK.value, token)
        }
    }

    @Test
    fun `should return SERVICE_UNAVAILABLE when repository throws ServiceConnectionException`() {
        val token = "token"
        consumeAsync = ConsumeAsync(token, mockkConsumeResponseListener, mockkRepository)

        every { mockkRepository.consumeAsync(token) } throws ServiceConnectionException()
        every {
            mockkConsumeResponseListener.onConsumeResponse(ResponseCode.SERVICE_UNAVAILABLE.value, null)
        } just runs

        consumeAsync.run()

        verify(exactly = 1) {
            mockkConsumeResponseListener.onConsumeResponse(ResponseCode.SERVICE_UNAVAILABLE.value, null)
        }
    }
}
