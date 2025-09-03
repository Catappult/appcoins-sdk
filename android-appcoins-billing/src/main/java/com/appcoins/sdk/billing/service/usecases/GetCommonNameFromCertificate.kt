package com.appcoins.sdk.billing.service.usecases

import com.appcoins.sdk.billing.usecases.UseCase
import java.security.cert.X509Certificate
import javax.security.auth.x500.X500Principal

/**
 * Helper to extract the Common Name (CN) from an X509Certificate's Subject DN.
 */
object GetCommonNameFromCertificate : UseCase() {
    operator fun invoke(cert: X509Certificate): String? {
        val dn = cert.subjectX500Principal
            .getName(X500Principal.RFC2253)
        val cnPrefix = "CN="
        val cnIndex = dn.indexOf(cnPrefix)
        if (cnIndex != -1) {
            var endIndex = dn.indexOf(",", cnIndex)
            if (endIndex == -1) {
                endIndex = dn.length
            }
            return dn.substring(cnIndex + cnPrefix.length, endIndex)
        }
        return null
    }
}
