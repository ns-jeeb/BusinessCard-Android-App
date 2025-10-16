package dev.najeeb.businesscard.cardwallet
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.lazy.items
import android.os.Bundle
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import dev.najeeb.businesscard.cardwallet.ui.theme.BusinessCardTheme
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import android.net.Uri
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.text.style.TextAlign
import androidx.core.view.WindowCompat
import coil.compose.rememberAsyncImagePainter
import dev.najeeb.businesscard.cardwallet.ui.theme.Pink40
import dev.najeeb.businesscard.cardwallet.ui.theme.btnModifier
import dev.najeeb.businesscard.cardwallet.ui.theme.detailTextStyle
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.graphics.Brush
import dev.najeeb.businesscard.cardwallet.ui.theme.CardBackgroundColor
import dev.najeeb.businesscard.cardwallet.ui.theme.GradientEnd

private enum class Screen {
    CREATION, // For creating the user's own card for the first time
    MY_CARD,  // For displaying the user's own card
    EDIT,     // For editing the user's own card
    CARD_LIST // For showing the collected cards from the database
}
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
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
                    color = MaterialTheme.colorScheme.background
                ) {
                    BusinessCardApp(cardViewModel = cardViewModel)
                }
            }
        }
    }
}
@Composable
fun BusinessCardApp(cardViewModel: CardViewModel) {
    // Get the user's card from the ViewModel's SharedPreferences state
    val myCard by cardViewModel.userCard

    // Get the list of collected cards from the ViewModel's database state
    val collectedCards by cardViewModel.allCards.observeAsState(initial = emptyList())
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(CardBackgroundColor, GradientEnd)
                )
            )
    ) {

        var currentScreen by remember { mutableStateOf(Screen.CREATION) }

        LaunchedEffect(myCard) {
            // This effect runs whenever 'myCard' changes.
            // If myCard exists, go to the MY_CARD screen. Otherwise, stay on CREATION.
            currentScreen = if (myCard == null) Screen.CREATION else Screen.MY_CARD
        }

        when (currentScreen) {
            Screen.CREATION -> {
                CreateCardScreen(
                    existingCard = null,
                    onCardSaved = { newCard ->
                        // This now calls the dedicated function to save to SharedPreferences
                        cardViewModel.saveOrUpdateUserCard(newCard)
                    }
                )
            }

            Screen.EDIT -> {
                CreateCardScreen(
                    // We pass the user's card to be edited
                    existingCard = myCard,
                    onCardSaved = { updatedCard ->
                        // This calls the SAME function, which just overwrites the entry in SharedPreferences
                        cardViewModel.saveOrUpdateUserCard(updatedCard)
                        // After saving, go back to the main card screen
                        currentScreen = Screen.MY_CARD
                    }
                )
            }

            Screen.MY_CARD -> {
                myCard?.let { userCard ->
                    BusinessCardScreen(
                        myCard = userCard,
                        onShowListClicked = { currentScreen = Screen.CARD_LIST },
                        onEditClicked = { currentScreen = Screen.EDIT }
                    )
                }
            }

            Screen.CARD_LIST -> {
                CardListScreen(
                    // The list from the database is now clean, no need to .drop(1)
                    cards = collectedCards,
                    onBackClicked = { currentScreen = Screen.MY_CARD }
                )
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CreateCardScreen(
    existingCard: BusinessCard?,
    onCardSaved: (BusinessCard) -> Unit,
) {

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var name by remember(existingCard) { mutableStateOf(existingCard?.name ?: "") }
    var title by remember(existingCard) { mutableStateOf(existingCard?.title ?: "") }
    var phone by remember(existingCard) { mutableStateOf(existingCard?.phone ?: "") }
    var email by remember(existingCard) { mutableStateOf(existingCard?.email ?: "") }
    var website by remember(existingCard) { mutableStateOf(existingCard?.website ?: "") }
    var address by remember(existingCard) { mutableStateOf(existingCard?.address ?: "") }
    var imageUri by remember(existingCard) { mutableStateOf(existingCard?.profilePictureUri?.toUri()) }

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
        Text(
            if (existingCard != null) "Update Your Card" else "Create Your Business Card",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))

        TextField(value = name, onValueChange = { name = it }, label = { Text("Your Name") })
        Spacer(modifier = detailTextStyle.height(16.dp))
        TextField(value = title, onValueChange = { title = it }, label = { Text("Your Title") })
        Spacer(modifier = detailTextStyle.height(16.dp))
        TextField(value = phone, onValueChange = { phone = it }, label = { Text("Your Phone") })
        Spacer(modifier = detailTextStyle.height(16.dp))
        TextField(value = email, onValueChange = { email = it }, label = { Text("Your Email") })
        Spacer(modifier = detailTextStyle.height(16.dp))
        TextField(value = website, onValueChange = { website = it }, label = { Text("Your Website") })
        Spacer(modifier = detailTextStyle.height(16.dp))
        TextField(value = address, onValueChange = { address = it }, label = { Text("Your Address") })
        Spacer(modifier = detailTextStyle.height(32.dp))

        ImagePicker(
            currentImageUri = imageUri,
            onImagePicked = { uri ->
                imageUri = uri
            },
            modifier = Modifier.padding(vertical = 0.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Button(onClick = {
            val updateCard = BusinessCard(
                id = existingCard?.id ?: 0,
                name = name,
                title = title,
                phone = phone,
                email = email,
                website = website,
                address = address,
                profilePictureUri = imageUri?.toString())
            onCardSaved(updateCard)
            keyboardController?.hide()
        }) {
            Text(if (existingCard != null) "Update Card" else "Save Card")
        }
    }
}


@Composable
fun BusinessCardScreen(
    myCard: BusinessCard,
    onShowListClicked: () -> Unit,
    onEditClicked: () -> Unit) {
    val cardDataString = "businesscard:${myCard.name}|${myCard.title}|${myCard.phone}|${myCard.email}|${myCard.website}|${myCard.address} | ${myCard.profilePictureUri}"
    val appUrl = "https://play.google.com/store/apps/details?id=dev.najeeb.businesscard.cardwallet"
    var qrContent by remember { mutableStateOf(cardDataString) } // Default to showing contact info
    val qrCodeBitmap = generateQrCode(qrContent)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            modifier = Modifier.padding(vertical = 20.dp),
            text = "Scan to add my card",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
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
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onEditClicked) { // <-- ADD THIS BUTTON
            Text("Edit My Card")
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
                bmp[x, y] = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
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
        Spacer(modifier = Modifier.height(30.dp))
        Button(onClick = onBackClicked, modifier = Modifier.padding(8.dp)) {
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
    val imageUri = card.profilePictureUri?.let { Uri.parse(it) }
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(10.dp, 10.dp, 10.dp)) {
            Column(
                modifier = Modifier
                    .background(CardBackgroundColor)
                    .padding(16.dp)
            ) {

                if (imageUri != null) {
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

                } else {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.background),
                    )
                }
                Column(modifier = Modifier.padding(6.dp, 0.dp, 0.dp, 0.dp)) {//5196529106
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
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.padding(16.dp)) {

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

            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    BusinessCardTheme {
        BusinessCardScreen(
            BusinessCard(
                id = 0,
                name = "Najeeb Sakhizada",
                title = "Android Developer",
                phone = "416",
                email = "",
                website = "",
                address = "",
                profilePictureUri = ""
            ),
            onShowListClicked = { false },
            onEditClicked = { false }
        )
    }
}

