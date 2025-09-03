package com.appcoins.sdk.billing.service.usecases

import com.appcoins.sdk.billing.usecases.UseCase
import com.appcoins.sdk.core.logger.Logger.logError
import java.security.cert.CertificateParsingException
import java.security.cert.X509Certificate

/**
 * Helper to extract Subject Alternative Names (SANs) from an X509Certificate.
 * Only extracts DNS names (type 2).
 */
object GetSubjectAlternativeNames : UseCase() {
    operator fun invoke(x509Certificate: X509Certificate): Set<String> {
        val subjectAlternativeNames: MutableSet<String> = HashSet()
        try {
            for (name in x509Certificate.subjectAlternativeNames) {
                getName(name)?.let {
                    subjectAlternativeNames.add(it)
                }
            }
        } catch (e: CertificateParsingException) {
            logError("Failed to get Subject Alternative Names: ${e.message}", e)
            return emptySet()
        } catch (e: Exception) {
            logError("Failed to get Subject Alternative Names: ${e.message}", e)
            return emptySet()
        }
        return subjectAlternativeNames
    }

    private fun getName(name: MutableList<*>): String? {
        if (name.size >= 2) {
            val type = name[0] as Int
            // Type 2 is DNS name
            if (type == 2) {
                val dnsName = name[1] as String
                return dnsName
            }
        }
        return null
    }
}
