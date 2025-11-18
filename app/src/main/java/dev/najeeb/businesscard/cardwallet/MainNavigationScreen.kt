package dev.najeeb.businesscard.cardwallet

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import dev.najeeb.businesscard.cardwallet.ui.theme.GradientEnd
import dev.najeeb.businesscard.cardwallet.ui.theme.GradientStart

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
                    onCardSaved = { newCard -> cardViewModel.saveOrUpdateUserCard(newCard) },
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
                        } else {
                            currentScreen = Screen.MY_CARD
                        }
                        // After scan, always return to the list
                        currentScreen = Screen.CARD_LIST
                    },
                    application
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
fun ScannerLauncher(onQrCodeScanned: (String) -> Unit, application: Application) {
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

