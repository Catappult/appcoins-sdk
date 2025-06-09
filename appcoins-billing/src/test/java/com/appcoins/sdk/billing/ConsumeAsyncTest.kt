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
            mockkConsumeResponseListener.onConsumeResponse(
                BillingResult.newBuilder().setResponseCode(ResponseCode.DEVELOPER_ERROR.value).build(),
                null
            )
        } just runs

        consumeAsync.run()

        verify(exactly = 1) {
            mockkConsumeResponseListener.onConsumeResponse(
                BillingResult.newBuilder().setResponseCode(ResponseCode.DEVELOPER_ERROR.value).build(), null
            )
        }
    }

    @Test
    fun `should return DEVELOPER_ERROR when token is empty`() {
        consumeAsync = ConsumeAsync("", mockkConsumeResponseListener, mockkRepository)

        every {
            mockkConsumeResponseListener.onConsumeResponse(
                BillingResult.newBuilder().setResponseCode(ResponseCode.DEVELOPER_ERROR.value).build(), null
            )
        } just runs

        consumeAsync.run()

        verify(exactly = 1) {
            mockkConsumeResponseListener.onConsumeResponse(
                BillingResult.newBuilder().setResponseCode(ResponseCode.DEVELOPER_ERROR.value).build(), null
            )
        }
    }

    @Test
    fun `should return repository response when repository handled correctly`() {
        val token = "token"
        consumeAsync = ConsumeAsync(token, mockkConsumeResponseListener, mockkRepository)

        every { mockkRepository.consumeAsync(token) } returns BillingResult.newBuilder()
            .setResponseCode(ResponseCode.OK.value).build()
        every {
            mockkConsumeResponseListener.onConsumeResponse(
                BillingResult.newBuilder().setResponseCode(ResponseCode.OK.value).build(), token
            )
        } just runs

        consumeAsync.run()

        verify(exactly = 1) {
            mockkConsumeResponseListener.onConsumeResponse(
                BillingResult.newBuilder().setResponseCode(ResponseCode.OK.value).build(), token
            )
        }
    }

    @Test
    fun `should return SERVICE_UNAVAILABLE when repository throws ServiceConnectionException`() {
        val token = "token"
        consumeAsync = ConsumeAsync(token, mockkConsumeResponseListener, mockkRepository)

        every { mockkRepository.consumeAsync(token) } throws ServiceConnectionException()
        every {
            mockkConsumeResponseListener.onConsumeResponse(
                BillingResult.newBuilder().setResponseCode(ResponseCode.SERVICE_UNAVAILABLE.value).build(), null
            )
        } just runs

        consumeAsync.run()

        verify(exactly = 1) {
            mockkConsumeResponseListener.onConsumeResponse(
                BillingResult.newBuilder().setResponseCode(ResponseCode.SERVICE_UNAVAILABLE.value).build(), null
            )
        }
    }
}
