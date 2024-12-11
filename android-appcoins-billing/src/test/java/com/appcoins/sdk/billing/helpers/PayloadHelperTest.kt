package com.appcoins.sdk.billing.helpers

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@Suppress("MaxLineLength")
class PayloadHelperTest {

    @Test
    fun `buildIntentPayload should build intent with all information`() {
        val result = PayloadHelper.buildIntentPayload(ORDER_REFERENCE, DEVELOPER_PAYLOAD, ORIGIN)

        assertEquals(result, FULL_INTENT_PAYLOAD)
    }

    @Test
    fun `buildIntentPayload should build intent with empty information if parameters are null`() {
        val result = PayloadHelper.buildIntentPayload(null, null, null)

        assertEquals(result, EMPTY_INTENT_PAYLOAD)
    }

    @Test
    fun `buildIntentPayload should build intent with empty information if parameters are empty`() {
        val result = PayloadHelper.buildIntentPayload("", "", "")

        assertEquals(result, EMPTY_INTENT_PAYLOAD)
    }

    @Test
    fun `getPayload should return null when value in null`() {
        val result = PayloadHelper.getPayload(null)

        assertEquals(result, null)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `getPayload should throw IllegalArgumentException when scheme is not valid`() {
        PayloadHelper.getPayload("invalidScheme://")
    }

    @Test
    fun `getPayload should return null when value is not present in the URI`() {
        val result = PayloadHelper.getPayload(EMPTY_INTENT_PAYLOAD)

        assertEquals(result, null)
    }

    @Test
    fun `getPayload should return PAYLOAD when value is present in the URI`() {
        val result = PayloadHelper.getPayload(FULL_INTENT_PAYLOAD)

        assertEquals(result, DEVELOPER_PAYLOAD)
    }

    @Test
    fun `getOrderReference should return null when value in null`() {
        val result = PayloadHelper.getOrderReference(null)

        assertEquals(result, null)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `getOrderReference should throw IllegalArgumentException when scheme is not valid`() {
        PayloadHelper.getOrderReference("invalidScheme://")
    }

    @Test
    fun `getOrderReference should return null when value is not present in the URI`() {
        val result = PayloadHelper.getOrderReference(EMPTY_INTENT_PAYLOAD)

        assertEquals(result, null)
    }

    @Test
    fun `getOrderReference should return ORDER_REFERENCE when value is present in the URI`() {
        val result = PayloadHelper.getOrderReference(FULL_INTENT_PAYLOAD)

        assertEquals(result, ORDER_REFERENCE)
    }

    @Test
    fun `getOrigin should return null when value in null`() {
        val result = PayloadHelper.getOrigin(null)

        assertEquals(result, null)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `getOrigin should throw IllegalArgumentException when scheme is not valid`() {
        PayloadHelper.getOrigin("invalidScheme://")
    }

    @Test
    fun `getOrigin should return null when value is not present in the URI`() {
        val result = PayloadHelper.getOrigin(EMPTY_INTENT_PAYLOAD)

        assertEquals(result, null)
    }

    @Test
    fun `getOrigin should return ORIGIN when value is present in the URI`() {
        val result = PayloadHelper.getOrigin(FULL_INTENT_PAYLOAD)

        assertEquals(result, ORIGIN)
    }

    private companion object {
        const val SCHEME = "appcoins"
        const val DEVELOPER_PAYLOAD = "developerPayload"
        const val ORDER_REFERENCE = "orderReference"
        const val ORIGIN = "origin"

        const val FULL_INTENT_PAYLOAD =
            "$SCHEME://appcoins.io?payload=$DEVELOPER_PAYLOAD&order_reference=$ORDER_REFERENCE&origin=$ORIGIN"

        const val EMPTY_INTENT_PAYLOAD =
            "$SCHEME://appcoins.io"
    }
}
