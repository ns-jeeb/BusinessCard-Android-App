package dev.najeeb.businesscard.cardwallet

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import androidx.compose.foundation.lazy.items
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlin.collections.first
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.core.view.WindowCompat
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.core.net.toUri
import dev.najeeb.businesscard.cardwallet.ui.theme.BusinessCardTheme
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import dev.najeeb.businesscard.cardwallet.ui.theme.GradientEnd
import dev.najeeb.businesscard.cardwallet.ui.theme.GradientStart
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import dev.najeeb.businesscard.cardwallet.ui.theme.CardBackgroundColor
import dev.najeeb.businesscard.cardwallet.ui.theme.CardBlack
import dev.najeeb.businesscard.cardwallet.ui.theme.CardWhite

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val application = requireNotNull(this).application

        // Create the ViewModel using the factory
        val cardViewModel: CardViewModel by viewModels {
            CardViewModelFactory(application)
        }

        setContent {
            BusinessCardTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent // Make the Surface transparent
                ) {
                    BusinessCardApp(cardViewModel = cardViewModel)
                    val matrix = arrayOf(
                        intArrayOf(1, 2, 3),
                        intArrayOf(4, 5, 6),
                        intArrayOf(7, 8, 9)
                    )
                }
            }
        }
    }
}
@Composable
fun BusinessCardApp(cardViewModel: CardViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(CardBackgroundColor, GradientEnd)
                )
            )
    ){val cards by cardViewModel.allCards.collectAsState(initial = emptyList())

        // This state will control which screen we are on
        var showMyCardScreen by remember { mutableStateOf(true) }

        if (cards.isEmpty()) {
            // If there are no cards at all, force user to create one
            CreateCardScreen { card ->
                cardViewModel.insertCard(card)
            }
        } else {
            val myCard = cards.first()

            if (showMyCardScreen) {
                // Show the user's personal card with QR code
                BusinessCardScreen(myCard = myCard, onShowListClicked = { showMyCardScreen = false })
            } else {
                // Show the list of all collected cards
                CardListScreen(cards = cards, onBackClicked = { showMyCardScreen = true })
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CreateCardScreen(onCardSaved: (BusinessCard) -> Unit) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    var name by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var website by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                keyboardController?.hide()
                focusManager.clearFocus()
            }
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        // Since it's scrollable, we align content to the top, not center it.
        verticalArrangement = Arrangement.Top
    ) {
        Text("Create Your Business Card",textAlign = TextAlign.Center,
            fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))

        TextField(value = name, onValueChange = { name = it }, label = { Text("Your Name") })
        Spacer(modifier = Modifier.height(16.dp))
        TextField(value = title, onValueChange = { title = it }, label = { Text("Your Title") })
        Spacer(modifier = Modifier.height(16.dp))
        TextField(value = phone, onValueChange = { phone = it }, label = { Text("Your Phone") })
        Spacer(modifier = Modifier.height(16.dp))
        TextField(value = email, onValueChange = { email = it }, label = { Text("Your Email") })
        Spacer(modifier = Modifier.height(16.dp))
        TextField(value = website, onValueChange = { website = it }, label = { Text("Your Website") })
        Spacer(modifier = Modifier.height(16.dp))
        TextField(value = address, onValueChange = { address = it }, label = { Text("Your Address") })
        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = {
            val newCard = BusinessCard(name = name, title = title, phone = phone, email = email, website = website, address = address)
            onCardSaved(newCard)
            keyboardController?.hide()
        }) {
            Text("Save Card")
        }
    }
}


@Composable
fun BusinessCardScreen(myCard: BusinessCard, onShowListClicked: () -> Unit) {
    val cardDataString = "businesscard:${myCard.name}|${myCard.title}|${myCard.phone}|${myCard.email}|${myCard.website}|${myCard.address}"
    val appUrl = "https://play.google.com/store/apps/details?id=dev.najeeb.businesscard.cardwallet"
    var qrContent by remember { mutableStateOf(cardDataString) } // Default to showing contact info
    val qrCodeBitmap = generateQrCode(qrContent)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Scan to add my card",
            fontSize = 16.sp
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
//            Image(
//                painter = painterResource(id = R.drawable.ic_launcher_background),
//                contentDescription = "My Picture",
//                modifier = Modifier
//                    .weight(1f)
//                    .padding(8.dp),
//                contentScale = ContentScale.Fit
//            )

            // 4. THE QR CODE
            qrCodeBitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "QR Code",
                    modifier = Modifier
                        .size(250.dp)
                        .padding(8.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)){
            Button(onClick = { qrContent = cardDataString }) {
                Image(
                    painter = painterResource(id = R.drawable.person_add),
                    contentDescription = "Call Icon" ,
                    modifier = Modifier.size(24.dp)
                )
            }

            Button(onClick = { qrContent = appUrl }) {
                Image(
                    painter = painterResource(id = R.drawable.outline_download_24),
                    contentDescription = "Call Icon" ,
                    modifier = Modifier.size(24.dp)
                )
            }

        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = myCard.name,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = myCard.title,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.primary
        )


        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = myCard.phone,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = myCard.email,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = myCard.address,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onShowListClicked) {
            Text("View Collected Cards")
        }
    }
}
private fun generateQrCode(content: String): Bitmap? {
    val writer = QRCodeWriter()
    return try {
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bmp = createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bmp[x, y] = if (bitMatrix[x, y]) CardBlack.toArgb() else CardWhite.toArgb()
            }
        }
        bmp
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Composable
fun CardListScreen(cards: List<BusinessCard>, onBackClicked: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(60.dp))
        Button(onClick = onBackClicked, modifier = Modifier.padding(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent, // Set the background to transparent
                contentColor = MaterialTheme.colorScheme.primary // Set the text color
            )
            ) {
            Text("Back to My Card")
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(cards) { card ->
                BusinessCardItem(card = card)
            }
        }
    }
}


@Composable
fun BusinessCardItem(card: BusinessCard) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        // The main container arranges everything vertically
        Column(modifier = Modifier
            .background(CardBackgroundColor)
            .padding(16.dp)) {

            // --- INFO SECTION ---
            Text(
                text = card.name,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = card.title,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = card.address,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- PHONE ACTION ROW ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = "tel:${card.phone}".toUri()
                        }
                        context.startActivity(intent)
                    }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Text for the phone number
                Text(
                    text = card.phone,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontSize = 16.sp
                )
                Image(
                    painter = painterResource(id = R.drawable.outline_add_call_24), // TODO: Replace with a phone icon
                    contentDescription = "Call Icon",
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- EMAIL ACTION ROW ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .clickable {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = "mailto:${card.email}".toUri()
                        }
                        context.startActivity(intent)
                    }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = card.email,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
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

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    val prf: SharedPreferences = MainActivity().getSharedPreferences("BusinessCardApp", MODE_PRIVATE)
    BusinessCardTheme {
//        BusinessCardScreen()
    }
}