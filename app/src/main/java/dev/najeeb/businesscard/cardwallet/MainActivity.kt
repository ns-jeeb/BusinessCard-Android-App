package dev.najeeb.businesscard.cardwallet

import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
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
import androidx.core.net.toUri
import dev.najeeb.businesscard.cardwallet.ui.theme.BusinessCardTheme
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.core.view.WindowCompat
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.toArgb
import dev.najeeb.businesscard.cardwallet.ui.theme.GradientEnd
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import dev.najeeb.businesscard.cardwallet.ui.theme.GradientStart
import dev.najeeb.businesscard.cardwallet.ui.theme.Purple80
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import dev.najeeb.businesscard.cardwallet.ui.theme.disabledColor
import dev.najeeb.businesscard.cardwallet.ui.theme.enabledColor


enum class Screen {
    SCANNER,
    CREATION,
    MY_CARD,
    EDIT,
    CARD_LIST,
    CARD_DETAIL
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val application = requireNotNull(this).application
        WindowCompat.setDecorFitsSystemWindows(window, true)
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.show(WindowInsetsCompat.Type.systemBars())
        val cardViewModel: CardViewModel by viewModels {
            CardViewModelFactory(application)
        }
        WindowCompat.enableEdgeToEdge(window)
        handleIntent(intent, cardViewModel)
        setContent {
            BusinessCardTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = GradientEnd),
                ) {
                    BusinessCardApp(cardViewModel = cardViewModel, application)
                }
            }

        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Handle deep link if the app is already open and receives a new one
        val cardViewModel: CardViewModel by viewModels()
        handleIntent(intent, cardViewModel)
    }

}

@Composable
fun AppTopBar(
    currentScreen: Screen,
    onNavigateToMyCard: () -> Unit,
    onNavigateToList: () -> Unit,
    onEditCard: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(brush = Brush.verticalGradient(colors = listOf(GradientEnd,GradientEnd)))
            .statusBarsPadding().padding(0.dp,15.dp, 0.dp, 0.dp),
        horizontalArrangement = Arrangement.SpaceEvenly, // Distribute buttons evenly
        verticalAlignment = Alignment.CenterVertically
    ) {

        Button(
            onClick = onNavigateToMyCard,
            // Disable the button if we are already on that screen
            enabled = currentScreen != Screen.MY_CARD,
            colors = ButtonDefaults.buttonColors(
                disabledContainerColor = disabledColor,
                containerColor = enabledColor,
            ),
            // 2. Remove the default padding so our gradient can fill the space
            contentPadding = PaddingValues(15.dp, 0.dp, 15.dp, 0.dp),
            // Optional: Add an elevation for a shadow effect
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Text("My Card", fontSize = 10.sp)
        }

        // Button to go to the "Collected Cards" list
        Button(
            onClick = onNavigateToList,
            enabled = currentScreen != Screen.CARD_LIST,
            colors = ButtonDefaults.buttonColors(
                disabledContainerColor = disabledColor,
                containerColor = enabledColor,
            ),

            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Text("Collected Cards", fontSize = 10.sp)
        }

        Button(
            onClick = onEditCard,
            enabled = currentScreen != Screen.EDIT,
            colors = ButtonDefaults.buttonColors(
                disabledContainerColor = disabledColor,
                containerColor = enabledColor,
            ),

            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Text("Edit Card", fontSize = 10.sp)
        }
    }

}


private fun handleIntent(intent: Intent?, viewModel: CardViewModel) {
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
        // Clear the intent so it's not processed again on rotation
        intent.data = null
    }
}


@Composable
fun BusinessCardApp(cardViewModel: CardViewModel, application: Application) {
    val myCard by cardViewModel.userCard
    val collectedCards by cardViewModel.allCards.observeAsState(initial = emptyList())
    var selectedCard by remember { mutableStateOf<BusinessCard?>(null) }

    val initialScreen = if (myCard == null) Screen.CREATION else Screen.MY_CARD
    var currentScreen by remember(initialScreen) { mutableStateOf(initialScreen) }


    // ** 1. Hoist the QR Code state here! **
    // The strings for the QR codes
    val cardDataString = myCard?.let { generateCardDataString(it) } ?: ""
    val appUrl = "https://play.google.com/store/apps/details?id=dev.najeeb.businesscard.cardwallet"
    // The state that holds the *currently active* QR content
    var qrContent by remember(myCard) { mutableStateOf(cardDataString) }

    // Make sure the default QR code is reset if the user's card is created/updated
    LaunchedEffect(cardDataString) {
        if (cardDataString.isNotEmpty()) {
            qrContent = cardDataString
        }
    }
    Scaffold(
        topBar = {
            AppTopBar(
                currentScreen = currentScreen,
                onNavigateToMyCard = { currentScreen = Screen.MY_CARD },
                onNavigateToList = { currentScreen = Screen.CARD_LIST },
                onEditCard = { currentScreen = Screen.EDIT }
                )
        },
        bottomBar = {
            if (currentScreen == Screen.MY_CARD) {
                AppBottomBar(
                    currentQrContent = qrContent,
                    cardDataString = cardDataString,
                    googlePlayUrl = appUrl,
                    // When a button is clicked in the bar, this lambda updates the state here
                    onQrContentChange = { newContent ->
                        qrContent = newContent
                    },
                    onScanClicked = { currentScreen = Screen.SCANNER }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
                .background(brush = Brush.verticalGradient(colors = listOf(GradientEnd, GradientStart)))
        ) {
            when (currentScreen) {
                Screen.CREATION -> CreateCardScreen(
                    existingCard = null,
                    onCardSaved = { newCard -> cardViewModel.saveOrUpdateUserCard(newCard)},
                    application
                )
                Screen.EDIT -> CreateCardScreen(
                    existingCard = myCard,
                    onCardSaved = { updatedCard ->
                        cardViewModel.saveOrUpdateUserCard(updatedCard)
                        currentScreen = Screen.MY_CARD
                    },
                    application
                )
                Screen.MY_CARD -> myCard?.let { userCard ->
                    BusinessCardScreen(
                        myCard = userCard,
                        qrContent = qrContent,
                        onScanClicked = { currentScreen = Screen.SCANNER }
                    )
                }
                Screen.CARD_LIST -> ListCardScreen().CardListScreen(
                    cards = collectedCards,
                    onItemClicked = { clickedCard ->
                        if (currentScreen == Screen.CARD_LIST) {
                            selectedCard = clickedCard
                            cardViewModel.deleteCollectedCard(collectedCards[collectedCards.indexOf(clickedCard)])
                            currentScreen = Screen.CARD_LIST
                        }
                    },
                    application = application
                )
                Screen.SCANNER -> ScannerLauncher(
                    onQrCodeScanned = { scannedData ->
                        val scannedUri = Uri.parse(scannedData)
                        if (scannedUri.scheme == "cardwallet" && scannedUri.host == "add") {
                            handleIntent(Intent(Intent.ACTION_VIEW, scannedUri), cardViewModel)
                        } else if (scannedUri.scheme == "https" && "play.google.com" in scannedUri.host.orEmpty()) {
                            application.startActivity(Intent(Intent.ACTION_VIEW, scannedUri))
                        }else{
                            currentScreen = Screen.MY_CARD
                        }
                        // After scan, always return to the list
                        currentScreen = Screen.CARD_LIST
                    }
                )
                Screen.CARD_DETAIL -> {
                    // 4. Display the detail screen for the selected card
                    selectedCard?.let { card ->
                        // You would create a new Composable for this
                        CardDetailScreen(card = card, onBack = { currentScreen = Screen.CARD_DETAIL })
                    }
                }
            }
        }
    }

}

@Composable
fun CardDetailScreen(card: BusinessCard, onBack: () -> Unit) {
    Box(modifier = Modifier
        .fillMaxSize()
        .background(enabledColor)) {
        Button(onClick = {}) {
            Text("this a button")
        }
    }
}

private fun generateCardDataString(myCard: BusinessCard): String {
    val encodedName = Uri.encode(myCard.name)
    val encodedTitle = Uri.encode(myCard.title)
    val encodedPhone = Uri.encode(myCard.phone)
    val encodedEmail = Uri.encode(myCard.email)
    val encodedWebsite = Uri.encode(myCard.website)
    val encodedAddress = Uri.encode(myCard.address)
    val encodedImageUri = Uri.encode(myCard.profilePictureUri ?: "")

    return "cardwallet://add?name=$encodedName&title=$encodedTitle&phone=$encodedPhone&email=$encodedEmail&website=$encodedWebsite&address=$encodedAddress&imageUri=$encodedImageUri"
}

@Composable
fun AppBottomBar(
    currentQrContent: String,
    // It receives the strings it needs to switch between
    cardDataString: String,
    googlePlayUrl: String,
    // It reports when the user wants to change the QR code
    onQrContentChange: (newContent: String) -> Unit,
    onScanClicked: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(brush = Brush.verticalGradient(colors = listOf(GradientStart, GradientEnd)))
            // This handles the space for the system navigation bar (the gesture bar at the bottom)
            .navigationBarsPadding()
            .padding(vertical = 12.dp),

        ) {
        // Button to show Contact Info QR Code
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(Modifier.weight(1f))

            Button(
                onClick = { onQrContentChange(cardDataString) },
                // Disable the button if we are already on that screen
                enabled = currentQrContent != cardDataString,
                colors = ButtonDefaults.buttonColors(
                    disabledContainerColor = disabledColor,
                    containerColor = enabledColor,
                ),
            ) {
                Icon(
                    imageVector = Icons.Default.ContactPhone,
                    contentDescription = "Contact Info",
                )
            }
            Spacer(modifier = Modifier.size(16.dp))

            Icon(
                modifier = Modifier
                    .size(40.dp)
                    .clickable(onClick = { onScanClicked() }),
                imageVector = Icons.Default.QrCodeScanner,
                contentDescription = "QR Code Scanner Icon",
            )
            Spacer(Modifier.weight(1f))

            Button(
                onClick = { onQrContentChange(googlePlayUrl) },
                enabled = currentQrContent != googlePlayUrl,
                colors = ButtonDefaults.buttonColors(
                    disabledContainerColor = disabledColor,
                    containerColor = enabledColor
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "App Download"
                )
            }
            Spacer(Modifier.weight(1f))
        }
    }
}


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CreateCardScreen(
    existingCard: BusinessCard?,
    onCardSaved: (BusinessCard) -> Unit,
    application: Application
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
            .padding(24.dp)
            .statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            if (existingCard != null) "Update Your Card" else "Create Your Business Card",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))

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

        ImagePicker(
            currentImageUri = imageUri,
            onImagePicked = { uri ->
                imageUri = uri
            },
            modifier = Modifier.padding(vertical = 0.dp),
            application = application
        )
        Spacer(modifier = Modifier.height(10.dp))

        Button(
            colors = ButtonDefaults.buttonColors(
                disabledContainerColor = disabledColor,
                containerColor = enabledColor,
            ),
            onClick = {
                val newCard = BusinessCard(
                    id = existingCard?.id ?: 0, // Preserve ID if editing
                    name = name,
                    title = title,
                    phone = phone,
                    email = email,
                    website = website,
                    address = address,
                    profilePictureUri = imageUri?.toString()
                )
                onCardSaved(newCard)
                keyboardController?.hide()
                focusManager.clearFocus()
            }

        ) {
            Text(
                if (existingCard != null) "Update Card" else "Save Card",
                fontSize = 18.sp
            )
        }
    }
}

@Composable
fun BusinessCardScreen(
    myCard: BusinessCard,
    qrContent: String,
    onScanClicked: () -> Unit
) {

    val qrCodeBitmap = generateQrCode(qrContent)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center // Center the content
    ) {
        Spacer(modifier = Modifier.size(16.dp))
        // Top buttons for navigation
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
//            Image(
//                modifier = Modifier.size(150.dp, 100.dp), painter = painterResource(id = R.drawable.business_card_icon),
//                contentDescription = "google play",
//            )
            Image(
                modifier = Modifier
                    .size(80.dp, 100.dp),
                painter = rememberAsyncImagePainter(myCard.profilePictureUri),

                contentDescription = "Profile Picture",
                contentScale = ContentScale.FillBounds
            )
            Log.d("imageString @ 558", "${myCard.profilePictureUri}")
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

private fun generateQrCode(content: String): Bitmap? {
    val writer = QRCodeWriter()
    val blackColor = Color.Black.toArgb() // Convert to Int
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

@Composable
fun ScannerLauncher(onQrCodeScanned: (String) -> Unit) {
    val scanLauncher = rememberLauncherForActivityResult(
        contract = ScanContract(),
        onResult = { result ->
            result.contents?.let { qrData ->
                onQrCodeScanned(qrData)
            }
        }
    )

    // This launches the scanner as soon as this composable enters the screen
    LaunchedEffect(key1 = true) {
        val options = ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            setPrompt("Scan a Business Card")
            setCameraId(0) // Use rear camera
            setBeepEnabled(false)
            setOrientationLocked(false)
        }
        scanLauncher.launch(options)
    }

    // Placeholder UI while the scanner activity is launching
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Launching Scanner...")
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
            "",
            onScanClicked = { false }
        )
    }
}

