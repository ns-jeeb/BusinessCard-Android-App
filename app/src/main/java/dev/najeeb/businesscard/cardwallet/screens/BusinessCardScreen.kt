package dev.najeeb.businesscard.cardwallet.screens

import android.util.Base64
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import dev.najeeb.businesscard.cardwallet.BusinessCard
import dev.najeeb.businesscard.cardwallet.R
import dev.najeeb.businesscard.cardwallet.generateQrCode
import dev.najeeb.businesscard.cardwallet.ui.theme.GradientEnd
import dev.najeeb.businesscard.cardwallet.ui.theme.Purple80

@Composable
fun BusinessCardScreen(
    myCard: BusinessCard,
    qrContent: String,
    onScanClicked: () -> Unit,
) {

    val qrCodeBitmap = generateQrCode(qrContent)
    val imageDataString = myCard.profilePictureUri

    val imageBytes: ByteArray? = if (imageDataString.isNullOrEmpty()) {
        null
    } else {
        try {
            Base64.decode(imageDataString, Base64.DEFAULT)
        } catch (e: IllegalArgumentException) {
            Log.e("ImageDecode", "Failed to decode Base64 string", e)
            null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.size(16.dp))
        Text(
            text = "Your Business Card",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
        )

        qrCodeBitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "QR Code",
                modifier = Modifier
                    .size(150.dp)
                    .padding(8.dp),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            AsyncImage(
                model = myCard.profilePictureUri,
                contentDescription = "Profile Picture",
                modifier = Modifier.size(80.dp, 100.dp),
                contentScale = ContentScale.FillBounds,
                placeholder = painterResource(id = R.drawable.business_card_icon),
                error = painterResource(id = R.drawable.business_card_icon)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = myCard.name,
                    color = Purple80
                )
                Text(
                    text = myCard.title,
                    color = Purple80
                )
                Text(
                    text = myCard.address,
                    color = Purple80
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Column {
            Text(
                text = myCard.phone,
                color = Purple80
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = myCard.email,
                color = Purple80
            )
        }
    }
}

