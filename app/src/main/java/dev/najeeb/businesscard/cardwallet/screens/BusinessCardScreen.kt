package dev.najeeb.businesscard.cardwallet.screens

import android.util.Base64
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import dev.najeeb.businesscard.cardwallet.BusinessCard
import dev.najeeb.businesscard.cardwallet.R
import dev.najeeb.businesscard.cardwallet.generateQrCode
import dev.najeeb.businesscard.cardwallet.ui.theme.GradientEnd
import dev.najeeb.businesscard.cardwallet.ui.theme.Purple80
import dev.najeeb.businesscard.cardwallet.ui.theme.actionModifier

@Composable
fun BusinessCardScreen(
    navigator: NavController,
    myCard: BusinessCard,
    qrContent: String,
    onScanClicked: () -> Unit,
) {

    val qrCodeBitmap = remember(qrContent) {
        generateQrCode(qrContent)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.size(16.dp))
        Text(
            text = "Your Business Card",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
        )

        qrCodeBitmap?.let {
            Log.i("BusinessCardScreen", "QR Code Bitmap: $qrContent")
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
        Column(modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center){
            Row(modifier = Modifier.fillMaxWidth(),
                ) {
                AsyncImage(
                    model = myCard.profilePictureUri,
                    contentDescription = "Profile Picture",
                    modifier = Modifier.size(80.dp, 100.dp),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.business_card_icon),
                    error = painterResource(id = R.drawable.business_card_icon)
                )
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
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
            Row(modifier = actionModifier.background(color = GradientEnd.copy(alpha = 0.1f)),verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Call, contentDescription = "Call Icon",tint = Purple80, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = myCard.phone,
                    color = Purple80
                )
                Spacer(modifier = Modifier.width(8.dp))

            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = actionModifier.background(color = GradientEnd.copy(alpha = 0.1f)),
                verticalAlignment = Alignment.CenterVertically) {
               Icon(imageVector = Icons.Default.Email, contentDescription = "Email Icon",tint = Purple80, modifier = Modifier.size(24.dp))
                Text(
                    text = myCard.email,
                    color = Purple80
                )
            }
        }

    }
}

