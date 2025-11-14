package dev.najeeb.businesscard.cardwallet

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil.compose.rememberAsyncImagePainter
import dev.najeeb.businesscard.cardwallet.ui.theme.Pink40
import dev.najeeb.businesscard.cardwallet.ui.theme.Purple80
import dev.najeeb.businesscard.cardwallet.ui.theme.btnModifier

class ListCardScreen {
    @Composable
    fun CardListScreen(
        cards: List<BusinessCard>,
        onBackClicked: () -> Unit
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(16.dp)
                .statusBarsPadding()
                .wrapContentHeight()
        ) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(cards) { card ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth() // Items can still fill width within the LazyColumn
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        BusinessCardItem(card = card)
                    }

                }
            }
        }

    }

    @Composable
    fun BusinessCardItem(card: BusinessCard) {
        val context = LocalContext.current
        val imageUri = card.profilePictureUri?.let { Uri.parse(it) }
        val patternBrush = rememberPatternBrush(resource = R.drawable.patterngenerated)

        Card(
            modifier = Modifier
                .fillMaxWidth().border(3.dp, Purple80, RoundedCornerShape(10.dp)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        ) {
            Row() {
                Column(
                    modifier = Modifier
                        .background(patternBrush)
                        .padding(15.dp),

                ) {

                    if (imageUri != null) {

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp, 100.dp)
                            ) {
                                Image(
                                    modifier = Modifier
                                        .size(80.dp, 100.dp)
                                        .background(Pink40),
                                    painter = rememberAsyncImagePainter(imageUri),
                                    contentDescription = "Profile Picture",
                                    contentScale = ContentScale.FillBounds
                                )

                            }
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.padding(2.dp)) {

                                // --- INFO SECTION ---
                                Text(
                                    text = card.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = Purple80
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = card.title,
                                    fontSize = 16.sp,
                                    color = Purple80
                                )


                            }
                        }

                    } else {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.background),
                        )
                    }

                    Column(modifier = Modifier.padding(6.dp, 0.dp, 0.dp, 0.dp)) {
                        Row(modifier = Modifier.fillMaxWidth()){
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                modifier = Modifier.weight(1f),
                                text = card.address,
                                fontSize = 14.sp,
                                color = Purple80.copy(alpha = 0.7f)
                            )
                            Image(
                                painter = painterResource(id = R.drawable.outline_add_home_24),
                                contentDescription = "Call Icon",
                                modifier = Modifier.size(24.dp)
                            )
                        }//5196529106
                        Row(
                            modifier = btnModifier
                                .clickable {
                                    val intent = Intent(Intent.ACTION_DIAL).apply {
                                        data = "tel:${card.phone}".toUri()
                                    }
                                    context.startActivity(intent)
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Text for the phone number
                            Text(
                                text = card.phone,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                color = Purple80,
                                fontSize = 16.sp
                            )
                            Image(
                                painter = painterResource(id = R.drawable.outline_add_call_24),
                                contentDescription = "Call Icon",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        // --- EMAIL ACTION ROW ---
                        Row(
                            modifier = btnModifier
                                .clickable {
                                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = "mailto:${card.email}".toUri()
                                    }
                                    context.startActivity(intent)
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = card.email,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                color = Purple80,
                                fontSize = 16.sp
                            )
                            // Icon for email
                            Image(
                                painter = painterResource(id = R.drawable.outline_alternate_email_24),
                                contentDescription = "Email Icon",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                }

            }
        }
    }
}
@SuppressLint("LocalContextResourcesRead")
@Composable
fun rememberPatternBrush(resource: Int): ShaderBrush{
    val context = LocalContext.current
    val imageBitmap = ImageBitmap.imageResource(context.resources,resource)
    return remember (imageBitmap){
        ShaderBrush(
            shader = ImageShader(
            image =   imageBitmap,
            tileModeX = TileMode.Repeated,
            tileModeY = TileMode.Repeated))
    }
}