package com.appcoins.sdk.billing.helpers

import android.content.Context
import android.content.SharedPreferences
import com.appcoins.billing.AppcoinsBilling
import com.appcoins.sdk.billing.UriCommunicationAppcoinsBilling
import com.appcoins.sdk.billing.WalletBinderUtil
import com.appcoins.sdk.billing.webpayment.WebAppcoinsBilling
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Suppress("MaxLineLength")
class AppcoinsBillingStubHelperTest {

    private val mockkContext = mockk<Context>()

    @Test
    fun `should return WebAppcoinsBilling when BindType is BILLING_SERVICE_NOT_INSTALLED`() {
        mockkStatic(WalletBinderUtil::class)

        every { WalletBinderUtil.bindType } returns BindType.BILLING_SERVICE_NOT_INSTALLED

        val interfaceResult = AppcoinsBillingStubHelper.Stub.asInterface(IBinderWalletNotInstalled())

        verify(exactly = 2) {
            WalletBinderUtil.bindType
        }
        assertEquals(interfaceResult, WebAppcoinsBilling.instance)
    }

    @Test
    fun `should return AppcoinsBillingWrapper with UriCommunicationAppcoinsBilling billing service when BindType is URI_CONNECTION`() {
        mockkStatic(WalletUtils::class)
        mockkStatic(WalletBinderUtil::class)
        val mockkPackageName = "packageName"
        val mockkSharedPreferences = mockk<SharedPreferences>()
        val mockkWalletId = "walletId"
        WalletUtils.context = mockkContext

        every { WalletBinderUtil.bindType } returns BindType.URI_CONNECTION
        every { mockkContext.packageName } returns mockkPackageName
        every { mockkContext.getSharedPreferences(any(), any()) } returns mockkSharedPreferences
        every { mockkSharedPreferences.getString(WALLET_ID_KEY, any()) } returns mockkWalletId

        val interfaceResult = AppcoinsBillingStubHelper.Stub.asInterface(IBinderWalletNotInstalled())

        verify(exactly = 3) {
            WalletBinderUtil.bindType
        }
        assertTrue(interfaceResult is AppcoinsBillingWrapper)
        assertTrue(interfaceResult.appcoinsBilling is UriCommunicationAppcoinsBilling)
    }

    @Test
    fun `should return AppcoinsBillingWrapper with AppcoinsBilling when BindType is AIDL`() {
        mockkStatic(WalletUtils::class)
        mockkStatic(WalletBinderUtil::class)
        val mockkPackageName = "packageName"
        val mockkSharedPreferences = mockk<SharedPreferences>()
        val mockkWalletId = "walletId"
        WalletUtils.context = mockkContext
        val mockkAppcoinsBilling = mockk<AppcoinsBilling>()

        every { WalletBinderUtil.bindType } returns BindType.AIDL
        every { mockkContext.packageName } returns mockkPackageName
        every { mockkContext.getSharedPreferences(any(), any()) } returns mockkSharedPreferences
        every { mockkSharedPreferences.getString(WALLET_ID_KEY, any()) } returns mockkWalletId
        every { mockkAppcoinsBilling.asBinder() } returns IBinderWalletNotInstalled()

        val interfaceResult =
            AppcoinsBillingStubHelper.Stub.asInterface(mockkAppcoinsBilling.asBinder())

        verify(exactly = 3) {
            WalletBinderUtil.bindType
        }
        assertTrue(interfaceResult is AppcoinsBillingWrapper)
        assertTrue(interfaceResult.appcoinsBilling is AppcoinsBilling)
    }

    private companion object {
        const val WALLET_ID_KEY = "WALLET_ID"
    }
}
