package dev.najeeb.businesscard.cardwallet.screens

import android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import android.util.Base64
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import dev.najeeb.businesscard.cardwallet.BusinessCard
import dev.najeeb.businesscard.cardwallet.R
import dev.najeeb.businesscard.cardwallet.ui.theme.GradientEnd
import dev.najeeb.businesscard.cardwallet.ui.theme.GradientStart
import dev.najeeb.businesscard.cardwallet.ui.theme.Purple80

    @Composable
    fun BusinessCardListScreen(
        cards: List<BusinessCard>,
        onItemClicked: (BusinessCard) -> Unit,
        application: Application,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .statusBarsPadding()
        ) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(cards) { card ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        BusinessCardItem(
                            card = card,
                            onBusinessCardClick = { onItemClicked(card) },
                            application = application
                        )

                    }

                }
            }
        }

    }

    @Composable
    fun BusinessCardItem(
        card: BusinessCard,
        onBusinessCardClick: () -> Unit,
        application: Application
    ) {
        val patternBrush = rememberPatternBrush(resource = R.drawable.patterngenerated)
        val imageModel: Any? = if (card.profilePictureUri.isNullOrEmpty()) {
            Log.d("ImageDebug", "Card ID ${card.id}: cardUri is null.")
            null
        }else{
            try {
                Base64.decode(card.profilePictureUri, Base64.DEFAULT)
            } catch (e: Exception) {
                Log.e("ImageDebug", "Card ID ${card.id}: Base64 decoding failed.", e)
                null
            }
        }


        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Purple80, RoundedCornerShape(10.dp)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        ) {

            Column(
                modifier = Modifier
                    .background(patternBrush)
                    .padding(15.dp)
            ) {
                // --- TOP ROW: IMAGE + NAME/TITLE ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (imageModel != null) {
                        AsyncImage(
                            model = imageModel,
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .size(80.dp, 100.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(id = R.drawable.business_card_icon),
                            error = painterResource(id = R.drawable.business_card_icon)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(width = 80.dp, height = 100.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.business_card_icon), // A placeholder icon
                                contentDescription = "No Image",
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }

                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                modifier = Modifier.weight(1f),
                                text = card.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Purple80
                            )
                            Image(
                                modifier = Modifier
                                    .clickable(onClick = onBusinessCardClick)
                                    .size(24.dp),
                                painter = painterResource(id = R.drawable.outline_delete_forever_24),
                                contentDescription = "Delete Card",
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = card.title,
                            fontSize = 16.sp,
                            color = Purple80
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Column {

                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.outline_add_home_24),
                            contentDescription = "Address Icon",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = card.address,
                            fontSize = 14.sp,
                            color = Purple80.copy(alpha = 0.7f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth().clickable {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = "tel:${card.phone}".toUri()
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            application.startActivity(intent)
                        },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.outline_add_call_24),
                            contentDescription = "Call Icon",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = card.phone,
                            color = Purple80,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth().clickable {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = "mailto:${card.email}".toUri()
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            application.startActivity(intent)
                        },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.outline_alternate_email_24),
                            contentDescription = "Email Icon",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = card.email,
                            color = Purple80,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }


@SuppressLint("LocalContextResourcesRead")
@Composable
fun rememberPatternBrush(resource: Int): ShaderBrush {
    val context = LocalContext.current
    val imageBitmap = ImageBitmap.imageResource(context.resources, resource)
    return remember(imageBitmap) {
        ShaderBrush(
            shader = ImageShader(
                image = imageBitmap,
                tileModeX = TileMode.Repeated,
                tileModeY = TileMode.Repeated
            )
        )
    }
}