package com.appcoins.sdk.billing

import android.util.Base64
import com.appcoins.sdk.core.security.Security
import io.mockk.every
import io.mockk.just
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.unmockkStatic
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.security.InvalidKeyException
import java.security.KeyFactory
import java.security.NoSuchAlgorithmException
import java.security.PublicKey
import java.security.Signature
import java.security.SignatureException
import java.security.spec.InvalidKeySpecException
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Suppress("MaxLineLength")
class SecurityTest {

    @Before
    fun setup() {
        mockkStatic(Security::class)
    }

    @Test
    fun `verifyPurchase should return false if signedData is empty`() {
        val result = Security.verifyPurchase(BASE_64_DECODED_PUBLIC_KEY, "", BASE_64_DECODED_PUBLIC_KEY)

        assertFalse(result)
    }

    @Test
    fun `verifyPurchase should return false if base64DecodedPublicKey size is 0 or less`() {
        val invalidBase64DecodedPublicKey = Base64.decode("", Base64.DEFAULT)

        val result = Security.verifyPurchase(invalidBase64DecodedPublicKey, "sdsasa", BASE_64_DECODED_PUBLIC_KEY)

        assertFalse(result)
    }

    @Test
    fun `verifyPurchase should return false if decodedSignature size is 0 or less`() {
        val invalidBase64DecodedSignature = Base64.decode("", Base64.DEFAULT)

        val result = Security.verifyPurchase(BASE_64_DECODED_PUBLIC_KEY, "sdsasa", invalidBase64DecodedSignature)

        assertFalse(result)
    }

    @Test
    fun `verifyPurchase should return false if KeyFactory throws NoSuchAlgorithmException`() {
        mockkStatic(KeyFactory::class)
        every { KeyFactory.getInstance(any()).generatePublic(any()) } throws NoSuchAlgorithmException()

        val result = Security.verifyPurchase(BASE_64_DECODED_PUBLIC_KEY, "sdsasa", BASE_64_DECODED_PUBLIC_KEY)

        assertFalse(result)
        unmockkStatic(KeyFactory::class)
    }

    @Test
    fun `verifyPurchase should return false if KeyFactory throws InvalidKeySpecException`() {
        mockkStatic(KeyFactory::class)
        every { KeyFactory.getInstance(any()).generatePublic(any()) } throws InvalidKeySpecException()

        val result = Security.verifyPurchase(BASE_64_DECODED_PUBLIC_KEY, "sdsasa", BASE_64_DECODED_PUBLIC_KEY)

        assertFalse(result)
        unmockkStatic(KeyFactory::class)
    }

    @Test
    fun `verifyPurchase should return false if Signature fails to verify decodedSignature`() {
        mockkStatic(Signature::class)
        every { Signature.getInstance(SIGNATURE_ALGORITHM).initVerify(any<PublicKey>()) } just runs
        every { Signature.getInstance(SIGNATURE_ALGORITHM).update(any<ByteArray>()) } just runs
        every { Signature.getInstance(SIGNATURE_ALGORITHM).verify(any()) } returns false

        val result = Security.verifyPurchase(BASE_64_DECODED_PUBLIC_KEY, "sdsasa", BASE_64_DECODED_SIGNATURE_DATA)

        assertFalse(result)
        unmockkStatic(Signature::class)
    }

    @Test
    fun `verifyPurchase should return true if Signature verifies successfully decodedSignature`() {
        mockkStatic(Signature::class)
        every { Signature.getInstance(SIGNATURE_ALGORITHM).initVerify(any<PublicKey>()) } just runs
        every { Signature.getInstance(SIGNATURE_ALGORITHM).update(any<ByteArray>()) } just runs
        every { Signature.getInstance(SIGNATURE_ALGORITHM).verify(any()) } returns true

        val result = Security.verifyPurchase(BASE_64_DECODED_PUBLIC_KEY, "sdsasa", BASE_64_DECODED_SIGNATURE_DATA)

        assertTrue(result)
        unmockkStatic(Signature::class)
    }

    @Test
    fun `verifyPurchase should return false if Signature throws NoSuchAlgorithmException`() {
        mockkStatic(Signature::class)
        every { Signature.getInstance(SIGNATURE_ALGORITHM) } throws NoSuchAlgorithmException()

        val result = Security.verifyPurchase(BASE_64_DECODED_PUBLIC_KEY, "sdsasa", BASE_64_DECODED_SIGNATURE_DATA)

        assertFalse(result)
        unmockkStatic(Signature::class)
    }

    @Test
    fun `verifyPurchase should return false if Signature throws InvalidKeyException`() {
        mockkStatic(Signature::class)
        every { Signature.getInstance(SIGNATURE_ALGORITHM).initVerify(any<PublicKey>()) } just runs
        every { Signature.getInstance(SIGNATURE_ALGORITHM).update(any<ByteArray>()) } throws InvalidKeyException()

        val result = Security.verifyPurchase(BASE_64_DECODED_PUBLIC_KEY, "sdsasa", BASE_64_DECODED_SIGNATURE_DATA)

        assertFalse(result)
        unmockkStatic(Signature::class)
    }

    @Test
    fun `verifyPurchase should return false if Signature throws SignatureException`() {
        mockkStatic(Signature::class)
        every { Signature.getInstance(SIGNATURE_ALGORITHM).initVerify(any<PublicKey>()) } just runs
        every { Signature.getInstance(SIGNATURE_ALGORITHM).update(any<ByteArray>()) } just runs
        every { Signature.getInstance(SIGNATURE_ALGORITHM).verify(any()) } throws SignatureException()

        val result = Security.verifyPurchase(BASE_64_DECODED_PUBLIC_KEY, "sdsasa", BASE_64_DECODED_SIGNATURE_DATA)

        assertFalse(result)
        unmockkStatic(Signature::class)
    }

    private companion object {
        const val SIGNATURE_ALGORITHM = "SHA1withRSA"

        const val BASE_64_ENCODED_PUBLIC_KEY =
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyEt94j9rt0UvpkZ2jPMZZ16yUrBOtjpIQ" +
                "CWi/F3HN0+iwSAeEJyDw7xIKfNTEc0msm+m6ud1kJpLK3oCsK61syZ8bYQlNZkUxTaWNof1nMnbw3Xu5nuYMuowmzDqNMWg5jNo" +
                "oy6oxwIgVcdvbyGi5RIlxqbo2vSAwpbAAZE2HbUrysKhLME7IOrdRR8MQbSbKEy/9MtfKz0uZCJGi9h+dQb0b69H7Yo+/BN/ayBSJ" +
                "zOPlaqmiHK5lZsnZhK+ixpB883fr+PgSczU7qGoktqoe6Fs+nhk9bLElljCs5ZIl9/NmOSteipkbplhqLY7KwapDmhrtBgrTetmnW9" +
                "PU/eCWQIDAQAB"
        val BASE_64_DECODED_PUBLIC_KEY: ByteArray = Base64.decode(BASE_64_ENCODED_PUBLIC_KEY, Base64.DEFAULT)

        const val BASE_64_ENCODED_SIGNATURE_DATA =
            "YIMocm8mR5fdHbc8C8h6oe45T3wZlWiM70k4xN8MIhKR6Glalv8CqRmppKtsv/N0VTDxY6SEHOtylpuTTD9GDDV/sem/GikMA0WwEMRsNb44OEpsAFjt8yiXPdN66ybSveItKG9Ys5w4NXSv9qOK85VWJOADtzsF5iesP/nWlcRCfareDhxHTNX+2dA7e43HoWO+1YRchtjb0L+rjjqH60K/4WPQtS9cs+d1A9kvt4WfozHM+UMRbkYpa0JZAQt6LNEaYUGHMC/IAqE4lp/rrdW/NRsmJ7zWE5MTO2eT3OW19YxzH58zesbMy/Ot/6WAHfF4BMqQEVISMsAUT10ElqJOxaU031l/plGwE83Tyos0yK//o19ZAZl/QGkNYHpjn5bUXP6sF4FHduPH1BQn8faI68t7kok="
        val BASE_64_DECODED_SIGNATURE_DATA: ByteArray = Base64.decode(BASE_64_ENCODED_SIGNATURE_DATA, Base64.DEFAULT)
    }
}
