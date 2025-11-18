package dev.najeeb.businesscard.cardwallet

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

fun generateQrCode(content: String): Bitmap? {
    val writer = QRCodeWriter()
    val blackColor = Color.Black.toArgb()
    val whiteColor = Color.White.toArgb()
    return try {
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512)
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


internal fun generateCardDataString(myCard: BusinessCard): String {
    val encodedName = Uri.encode(myCard.name)
    val encodedTitle = Uri.encode(myCard.title)
    val encodedPhone = Uri.encode(myCard.phone)
    val encodedEmail = Uri.encode(myCard.email)
    val encodedWebsite = Uri.encode(myCard.website)
    val encodedAddress = Uri.encode(myCard.address)

    return "cardwallet://add?name=$encodedName&title=$encodedTitle&phone=$encodedPhone&email=$encodedEmail&website=$encodedWebsite&address=$encodedAddress"
}

fun handleIntent(intent: Intent?, viewModel: CardViewModel) {
    if (intent?.action != Intent.ACTION_VIEW || intent.data == null) return

    val data = intent.data
    if (data?.scheme == "cardwallet" && data.host == "add") {
        val card = BusinessCard(
            id = 0, // 0 for a new card to be inserted by Room
            name = data.getQueryParameter("name") ?: "",
            title = data.getQueryParameter("title") ?: "",
            phone = data.getQueryParameter("phone") ?: "",
            email = data.getQueryParameter("email") ?: "",
            website = data.getQueryParameter("website") ?: "",
            address = data.getQueryParameter("address") ?: "",
            profilePictureUri = data.getQueryParameter("imageUri")
        )
        viewModel.insert(card)
        intent.data = null
    }
}