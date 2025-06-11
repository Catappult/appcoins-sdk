package com.appcoins.sdk.billing

import com.appcoins.sdk.billing.exceptions.ServiceConnectionException
import com.appcoins.sdk.billing.listeners.ConsumeResponseListener
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.runs
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ConsumeAsyncTest {

    private lateinit var consumeAsync: ConsumeAsync

    private val mockkRepository = mockk<Repository>()
    private val mockkConsumeResponseListener = mockk<ConsumeResponseListener>()

    @Before
    fun setup() {
        mockkObject(BillingResult)
    }

    @Test
    fun `should return DEVELOPER_ERROR when token is null`() {
        consumeAsync = ConsumeAsync(null, mockkConsumeResponseListener, mockkRepository)

        val mockBillingResult = BillingResult.newBuilder()
            .setResponseCode(ResponseCode.DEVELOPER_ERROR.value)
            .setDebugMessage("Purchase token cannot be null or empty.")
            .build()

        every { mockkConsumeResponseListener.onConsumeResponse(mockBillingResult, null) } just runs
        every {
            BillingResult.newBuilder()
                .setResponseCode(ResponseCode.DEVELOPER_ERROR.value)
                .setDebugMessage("Purchase token cannot be null or empty.")
                .build()
        } returns mockBillingResult

        consumeAsync.run()

        verify(exactly = 1) {
            mockkConsumeResponseListener.onConsumeResponse(mockBillingResult, null)
        }
    }

    @Test
    fun `should return DEVELOPER_ERROR when token is empty`() {
        consumeAsync = ConsumeAsync("", mockkConsumeResponseListener, mockkRepository)

        val mockBillingResult = BillingResult.newBuilder()
            .setResponseCode(ResponseCode.DEVELOPER_ERROR.value)
            .setDebugMessage("Purchase token cannot be null or empty.")
            .build()

        every { mockkConsumeResponseListener.onConsumeResponse(mockBillingResult, null) } just runs
        every {
            BillingResult.newBuilder()
                .setResponseCode(ResponseCode.DEVELOPER_ERROR.value)
                .setDebugMessage("Purchase token cannot be null or empty.")
                .build()
        } returns mockBillingResult

        consumeAsync.run()

        verify(exactly = 1) {
            mockkConsumeResponseListener.onConsumeResponse(mockBillingResult, null)
        }
    }

    @Test
    fun `should return repository response when repository handled correctly`() {
        val token = "token"
        consumeAsync = ConsumeAsync(token, mockkConsumeResponseListener, mockkRepository)

        val mockBillingResult = BillingResult.newBuilder()
            .setResponseCode(ResponseCode.OK.value)
            .build()

        every { mockkRepository.consumeAsync(token) } returns mockBillingResult
        every { mockkConsumeResponseListener.onConsumeResponse(mockBillingResult, token) } just runs
        every {
            BillingResult.newBuilder()
                .setResponseCode(ResponseCode.OK.value)
                .build()
        } returns mockBillingResult

        consumeAsync.run()

        verify(exactly = 1) {
            mockkConsumeResponseListener.onConsumeResponse(mockBillingResult, token)
        }
    }

    @Test
    fun `should return SERVICE_UNAVAILABLE when repository throws ServiceConnectionException`() {
        val token = "token"
        consumeAsync = ConsumeAsync(token, mockkConsumeResponseListener, mockkRepository)

        val mockBillingResult = BillingResult.newBuilder()
            .setResponseCode(ResponseCode.SERVICE_UNAVAILABLE.value)
            .setDebugMessage("Service not available.")
            .build()

        every { mockkRepository.consumeAsync(token) } throws ServiceConnectionException()
        every { mockkConsumeResponseListener.onConsumeResponse(mockBillingResult, null) } just runs
        every {
            BillingResult.newBuilder()
                .setResponseCode(ResponseCode.SERVICE_UNAVAILABLE.value)
                .setDebugMessage("Service not available.")
                .build()
        } returns mockBillingResult

        consumeAsync.run()

        verify(exactly = 1) {
            mockkConsumeResponseListener.onConsumeResponse(mockBillingResult, null)
        }
    }
}
