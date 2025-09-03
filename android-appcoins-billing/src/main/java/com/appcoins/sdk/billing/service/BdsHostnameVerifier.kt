package com.appcoins.sdk.billing.service

import com.appcoins.sdk.billing.service.usecases.GetCommonNameFromCertificate
import com.appcoins.sdk.billing.service.usecases.GetSubjectAlternativeNames
import com.appcoins.sdk.core.logger.Logger.logDebug
import com.appcoins.sdk.core.logger.Logger.logError
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLPeerUnverifiedException
import javax.net.ssl.SSLSession

class BdsHostnameVerifier(private val originalHostForVerification: String) : HostnameVerifier {

    @Suppress("NestedBlockDepth", "ReturnCount")
    override fun verify(hostnameConnectedTo: String, session: SSLSession): Boolean {
        try {
            val certificates = session.peerCertificates
            if (certificates.isNullOrEmpty()) {
                logError("No peer certificates found for $hostnameConnectedTo")
                return false
            }

            // Cast to X509Certificate to access certificate details
            val x509Cert = certificates[0] as X509Certificate
            val commonName = GetCommonNameFromCertificate(x509Cert)
            val subjectAlternativeNamesList = GetSubjectAlternativeNames(x509Cert)

            logDebug(
                "Connected to IP: $hostnameConnectedTo, Expected Domain: $originalHostForVerification"
            )
            logDebug("Certificate CN: $commonName, SANs: $subjectAlternativeNamesList")

            // Check if the original domain matches the certificate's Common Name
            if (originalHostForVerification.equals(commonName, ignoreCase = true)) {
                logDebug("Original domain matched certificate CN.")
                return true
            }
            // Check if the original domain matches any Subject Alternative Name
            for (subjectAlternativeName in subjectAlternativeNamesList) {
                if (subjectAlternativeName.startsWith("*.")) {
                    val baseDomain = subjectAlternativeName.substring(2)
                    if (originalHostForVerification.endsWith(".$baseDomain") &&
                        originalHostForVerification.length > (baseDomain.length + 1)
                    ) {
                        logDebug(
                            "Original domain matched wildcard SAN: $subjectAlternativeName (Permissive)"
                        )
                        return true
                    }
                } else if (originalHostForVerification.equals(subjectAlternativeName, ignoreCase = true)) {
                    logDebug("Original domain matched exact SAN: $subjectAlternativeName")
                    return true
                }
            }
            logError(
                "Verification failed. Certificate does not match expected domain $originalHostForVerification"
            )
            return false
        } catch (e: SSLPeerUnverifiedException) {
            logError("SSLPeerUnverifiedException during verification: " + e.message)
            return false
        } catch (e: Exception) {
            logError("General error during verification: " + e.message)
            return false
        }
    }
}
