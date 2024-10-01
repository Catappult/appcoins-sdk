package com.appcoins.sdk.billing;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import static com.appcoins.sdk.core.logger.Logger.logError;

/**
 * Security-related methods. For a secure implementation, all of this code
 * should be implemented on a server that communicates with the
 * application on the device. For the sake of simplicity and clarity of this
 * example, this code is included here and is executed on the device. If you
 * must verify the purchases on the phone, you should obfuscate this code to
 * make it harder for an attacker to replace the code with stubs that treat all
 * purchases as verified.
 */
public class Security {
    private static final String KEY_FACTORY_ALGORITHM = "RSA";
    private static final String SIGNATURE_ALGORITHM = "SHA1withRSA";

    /**
     * Verifies that the data was signed with the given Signature, and returns
     * the verified purchase. The data is in JSON format and signed
     * with a private key.
     *
     * @param base64DecodedPublicKey the base64-decoded public key to use for verifying.
     * @param signedData the signed JSON string (signed, not encrypted)
     * @param decodedSignature the Signature for the data, signed with the private key
     */
    public static boolean verifyPurchase(byte[] base64DecodedPublicKey, String signedData,
        byte[] decodedSignature) {
        if (signedData.isEmpty()
            || base64DecodedPublicKey.length <= 0
            || decodedSignature.length <= 0) {
            return false;
        }

        PublicKey key = Security.generatePublicKey(base64DecodedPublicKey);

        if (key == null) {
            return false;
        }

        return Security.verify(key, signedData, decodedSignature);
    }

    /**
     * Generates a PublicKey instance from a string containing the
     * Base64-decoded public key.
     *
     * @param base64DecodedPublicKey Base64-decoded public key
     *
     * @throws IllegalArgumentException if decodedPublicKey is invalid
     */
    public static PublicKey generatePublicKey(byte[] base64DecodedPublicKey) {
        try {

            KeyFactory keyFactory = KeyFactory.getInstance(KEY_FACTORY_ALGORITHM);

            return keyFactory.generatePublic(new X509EncodedKeySpec(base64DecodedPublicKey));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            logError("Failed to generate public key: " + e);
            return null;
        }
    }

    /**
     * Verifies that the signature from the server matches the computed
     * signature on the data.  Returns true if the data is correctly signed.
     *
     * @param publicKey public key associated with the developer account
     * @param signedData signed data from server
     * @param decodedSignature server signature
     *
     * @return true if the data and signature match
     */
    public static boolean verify(PublicKey publicKey, String signedData, byte[] decodedSignature) {
        try {
            Signature sig = Signature.getInstance(SIGNATURE_ALGORITHM);
            sig.initVerify(publicKey);
            sig.update(signedData.getBytes());
            if (!sig.verify(decodedSignature)) {
                logError("Signature verification failed.");
                return false;
            }
            return true;
        } catch (NoSuchAlgorithmException e) {
            logError("NoSuchAlgorithmException.", e);
        } catch (InvalidKeyException e) {
            logError("Invalid key specification.", e);
        } catch (SignatureException e) {
            logError("Signature exception.", e);
        }
        return false;
    }
}
