package com.i69app.billing

import android.util.Base64
import com.i69app.data.config.Constants.BASE_64_ENCODED_PUBLIC_KEY
import timber.log.Timber
import java.io.IOException
import java.security.*
import java.security.spec.InvalidKeySpecException
import java.security.spec.X509EncodedKeySpec

private const val TAG = "Billing"
private const val KEY_FACTORY_ALGORITHM = "RSA"
private const val SIGNATURE_ALGORITHM = "SHA1withRSA"

object Security {

    fun verifyPurchase(signedData: String, signature: String): Boolean {
        if (signedData.isEmpty() || BASE_64_ENCODED_PUBLIC_KEY.isEmpty() || signature.isEmpty()) {
            Timber.tag(TAG).e("Purchase verification failed: missing data.")
            return false
        }

        return try {
            val key = generatePublicKey(BASE_64_ENCODED_PUBLIC_KEY)
            verify(key, signedData, signature)

        } catch (e: IOException) {
            Timber.tag(TAG).e("Error generating PublicKey from encoded key: ${e.message}")
            false
        }
    }

    @Throws(IOException::class)
    fun generatePublicKey(encodedPublicKey: String): PublicKey {
        try {
            val decodedKey = Base64.decode(encodedPublicKey, Base64.DEFAULT)
            val keyFactory = KeyFactory.getInstance(KEY_FACTORY_ALGORITHM)
            return keyFactory.generatePublic(X509EncodedKeySpec(decodedKey))

        } catch (e: NoSuchAlgorithmException) {
            // "RSA" is guaranteed to be available.
            throw RuntimeException(e)

        } catch (e: InvalidKeySpecException) {
            val error = "Invalid Key Spec Exception: $e"
            Timber.tag(TAG).e(error)
            throw IOException(error)
        }
    }

    private fun verify(publicKey: PublicKey, signedData: String, signature: String): Boolean {
        val signatureBytes: ByteArray = try {
            Base64.decode(signature, Base64.DEFAULT)
        } catch (e: IllegalArgumentException) {
            Timber.tag(TAG).e("Base64 decoding failed.")
            return false
        }

        try {
            val signatureAlgorithm: Signature = Signature.getInstance(SIGNATURE_ALGORITHM)
            signatureAlgorithm.initVerify(publicKey)
            signatureAlgorithm.update(signedData.toByteArray())
            if (!signatureAlgorithm.verify(signatureBytes)) {
                Timber.tag(TAG).e("Signature verification failed...")
                return false
            }
            return true

        } catch (e: NoSuchAlgorithmException) {
            // "RSA" is guaranteed to be available.
            throw RuntimeException(e)

        } catch (e: InvalidKeyException) {
            Timber.tag(TAG).e("Invalid key specification.")

        } catch (e: SignatureException) {
            Timber.tag(TAG).e("Signature exception.")
        }
        return false
    }

}