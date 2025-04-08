package com.appcoins.sdk.billing

import com.appcoins.sdk.billing.exceptions.ServiceConnectionException
import com.appcoins.sdk.billing.listeners.SkuDetailsResponseListener
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SkuDetailsAsyncTest {

    private lateinit var skuDetailsAsync: SkuDetailsAsync

    private val mockkRepository = mockk<Repository>()
    private val mockkSkuDetailsResponseListener = mockk<SkuDetailsResponseListener>()

    @Test
    fun `should return SkuDetails response`() {
        setup(SkuDetailsParams())

        every {
            mockkRepository.querySkuDetailsAsync(null, null)
        } returns SkuDetailsResult(SKU_DETAILS_LIST, ResponseCode.OK.value)
        every {
            mockkSkuDetailsResponseListener.onSkuDetailsResponse(ResponseCode.OK.value, SKU_DETAILS_LIST)
        } just runs

        skuDetailsAsync.run()

        verify(exactly = 1) {
            mockkRepository.querySkuDetailsAsync(null, null)
            mockkSkuDetailsResponseListener.onSkuDetailsResponse(ResponseCode.OK.value, SKU_DETAILS_LIST)
        }
    }

    @Test
    fun `should return emptyList when SkuDetails response list is empty`() {
        setup(SkuDetailsParams())

        every {
            mockkRepository.querySkuDetailsAsync(null, null)
        } returns SkuDetailsResult(emptyList(), ResponseCode.OK.value)
        every {
            mockkSkuDetailsResponseListener.onSkuDetailsResponse(ResponseCode.OK.value, emptyList())
        } just runs

        skuDetailsAsync.run()

        verify(exactly = 1) {
            mockkRepository.querySkuDetailsAsync(null, null)
            mockkSkuDetailsResponseListener.onSkuDetailsResponse(ResponseCode.OK.value, emptyList())
        }
    }

    @Test
    fun `should return SERVICE_UNAVAILABLE when getSkuDetails throws ServiceConnectionException`() {
        setup(SkuDetailsParams())

        every {
            mockkRepository.querySkuDetailsAsync(null, null)
        } throws ServiceConnectionException()
        every {
            mockkSkuDetailsResponseListener.onSkuDetailsResponse(ResponseCode.SERVICE_UNAVAILABLE.value, emptyList())
        } just runs

        skuDetailsAsync.run()

        verify(exactly = 1) {
            mockkRepository.querySkuDetailsAsync(null, null)
            mockkSkuDetailsResponseListener.onSkuDetailsResponse(ResponseCode.SERVICE_UNAVAILABLE.value, emptyList())
        }
    }

    private fun setup(skuDetailsParams: SkuDetailsParams) {
        skuDetailsAsync = SkuDetailsAsync(skuDetailsParams, mockkSkuDetailsResponseListener, mockkRepository)
    }

    private companion object {
        val SKU_DETAILS_LIST = listOf(
            SkuDetails(
                "itemType",
                "sku",
                "type",
                "price",
                0,
                "priceCurrencyCode",
                "appcPrice",
                0,
                "appcPriceCurrencyCode",
                "fiatPrice",
                0,
                "fiatPriceCurrencyCode",
                "title",
                "description",
                "trialPeriod",
                "period",
            )
        )
    }
}
