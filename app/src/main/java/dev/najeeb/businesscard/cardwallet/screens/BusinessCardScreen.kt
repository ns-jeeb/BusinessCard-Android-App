package dev.najeeb.businesscard.cardwallet.screens

import android.util.Base64
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
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
import org.w3c.dom.Text

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
            .padding(horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.size(16.dp))
        Text(
            text = "Your Business Card",
            color = Purple80,
            fontSize = 20.sp,
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Purple80, RoundedCornerShape(8.dp)),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(10.dp, 0.dp, 0.dp, 0.dp),
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
                Column(modifier = Modifier.weight(1f).padding(0.dp, 10.dp, 0.dp, 0.dp)) {
                    Text(
                        text = myCard.name,
                        color = Purple80,
                        fontSize = 20.sp
                    )
                    Text(
                        text = myCard.title,
                        color = Purple80,
                        fontSize = 20.sp
                    )

                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.background(color = GradientEnd.copy(alpha = 0.1f)).padding(5.dp,0.dp,0.dp,0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Address",
                    tint = Purple80,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text (
                    text = myCard.address,
                    color = Purple80,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.background(color = GradientEnd.copy(alpha = 0.1f)).padding(5.dp,0.dp,0.dp,0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Call Icon",
                    tint = Purple80,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = myCard.phone,
                    color = Purple80,
                    fontSize = 11.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.background(color = GradientEnd.copy(alpha = 0.1f)).padding(5.dp,0.dp,0.dp,0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Email Icon",
                    tint = Purple80,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = myCard.email,
                    color = Purple80,
                    fontSize = 11.sp
                )
            }
        }

    }
}

