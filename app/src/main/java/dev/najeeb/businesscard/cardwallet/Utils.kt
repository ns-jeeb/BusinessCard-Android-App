package dev.najeeb.businesscard.cardwallet

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlin.io.encoding.ExperimentalEncodingApi
import android.util.Base64
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.net.URLEncoder
import java.util.EnumMap
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream
import java.util.zip.GZIPInputStream

// Compress string to Base64
fun compressString(data: String): String {
    val bos = ByteArrayOutputStream()
    GZIPOutputStream(bos).bufferedWriter(Charsets.UTF_8).use { it.write(data) }
    return Base64.encodeToString(bos.toByteArray(), Base64.URL_SAFE or Base64.NO_WRAP)
}

// Decompress Base64 to string
fun decompressString(compressedData: String): String {
    val bytes = Base64.decode(compressedData, Base64.URL_SAFE or Base64.NO_WRAP)
    return GZIPInputStream(bytes.inputStream()).bufferedReader(Charsets.UTF_8).use { it.readText() }
}
internal fun generateCardDataString(myCard: BusinessCard): String {
    // We MUST still clear profilePictureUri because the other phone cannot access
    // "content://media/..." on your phone. It is physically impossible.
    // However, we can now keep 'about' and 'services'.
    val qrFriendlyCard = myCard.copy(profilePictureUri = "")

    val gson = Gson()
    val json = gson.toJson(qrFriendlyCard)

    // Compress the JSON to make the QR code much smaller (less dense)
    val compressedJson = compressString(json)

    // We don't need URLEncoder here because Base64.URL_SAFE handles it
    return "cardwallet://add?data=$compressedJson"
}

fun generateQrCode(content: String): Bitmap? {
    val writer = QRCodeWriter()
    val blackColor = Color.Black.toArgb()
    val whiteColor = Color.White.toArgb()

    // Add hints to help scanners read the code better
    val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java).apply {
        put(EncodeHintType.CHARACTER_SET, "UTF-8")
        // 'L' (Low) error correction creates a less dense QR code, which is easier to scan on screens
        put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L)
        put(EncodeHintType.MARGIN, 2) // Add a white border
    }

    return try {
        Log.i("GenerateQRCode", "Content for QR : $content")
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512, hints)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bmp = createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bmp[x, y] = if (bitMatrix[x, y]) blackColor else whiteColor
            }
        }
        bmp
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}


internal fun generateUniversalLink(myCard: BusinessCard): String {
    val qrFriendlyCard = myCard.copy(profilePictureUri = "")
    val json = Gson().toJson(qrFriendlyCard)
    val compressedJson = compressString(json)

    // Google Play requires URL encoding even for Base64 sometimes to be safe
    val safeData = URLEncoder.encode(compressedJson, "UTF-8")
    return "https://play.google.com/store/apps/details?id=dev.najeeb.businesscard.cardwallet&referrer=$safeData"
}



fun handleIntent(intent: Intent?, viewModel: CardViewModel) {
    if (intent?.action != Intent.ACTION_VIEW || intent.data == null) return

    val uri = intent.data
    val isPlayStoreLink = uri?.host?.equals("play.google.com") == true
    val isCardScheme = uri?.scheme?.equals("cardwallet") == true

    if (isCardScheme || isPlayStoreLink) {
        val rawDataParam = uri.getQueryParameter("data") ?: uri.getQueryParameter("referrer")

        val newCard: BusinessCard? = try {
            if (!rawDataParam.isNullOrEmpty()) {
                Log.d("BusinessCard", "Received JSON: $rawDataParam")
                val jsonString = if (rawDataParam.startsWith("{") || rawDataParam.startsWith("%7B")) {
                    rawDataParam
                } else {
                    try {
                        decompressString(rawDataParam)
                    } catch (e: Exception) {
                        rawDataParam
                    }
                }
                Gson().fromJson(jsonString, BusinessCard::class.java)
            } else{
                null
            }

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
        if (newCard != null) {
            val existingCards = viewModel.allCards.value ?: emptyList()
            val cleanNewPhone = newCard.phone.replace(Regex("[^0-9]"), "")
            val isDuplicate = existingCards.any {
                it.phone.replace(Regex("[^0-9]"), "") == cleanNewPhone
            }
            if (!isDuplicate) {
                viewModel.collectNewCard(newCard.copy(id = 0))
            }
        }

        // Clear intent to prevent re-processing
        intent.data = null
    }
}