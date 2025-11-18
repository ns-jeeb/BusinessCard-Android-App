package dev.najeeb.businesscard.cardwallet.screens

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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import dev.najeeb.businesscard.cardwallet.AppBottomBar
import dev.najeeb.businesscard.cardwallet.AppTopBar
import dev.najeeb.businesscard.cardwallet.CardViewModel
import dev.najeeb.businesscard.cardwallet.generateCardDataString
import dev.najeeb.businesscard.cardwallet.handleIntent
import dev.najeeb.businesscard.cardwallet.ui.theme.GradientEnd
import dev.najeeb.businesscard.cardwallet.ui.theme.GradientStart

@Composable
fun BusinessCardApp(cardViewModel: CardViewModel, application: Application) {
    val navController = rememberNavController()
    val myCard by cardViewModel.userCard
    val collectedCards by cardViewModel.allCards.observeAsState(initial = emptyList())
    val cardDataString = myCard?.let { generateCardDataString(it) } ?: ""
    val appUrl = "https://play.google.com/store/apps/details?id=dev.najeeb.businesscard.cardwallet"

    var qrContent by remember(myCard) { mutableStateOf(cardDataString) }
    val startDestination: Any = if (myCard == null) CreateCardRoute else HomeRoute
    LaunchedEffect(cardDataString) {
        if (cardDataString.isNotEmpty()) {
            qrContent = cardDataString
        }
    }
    Scaffold(
        topBar = {
            AppTopBar(
                navigator = navController,
                onNavigateToMyCard = {
                    navController.navigate(HomeRoute)
                    qrContent != cardDataString
                },
                onNavigateToList = {navController.navigate(ListRoute) },
                onEditCard = { navController.navigate(CreateCardRoute) }
            )
        },
        bottomBar = {
            AppBottomBar(
                currentQrContent = qrContent,
                cardDataString = cardDataString,
                googlePlayUrl = appUrl,
                // When a button is clicked in the bar, this lambda updates the state here
                onQrContentChange = { newContent ->
                    qrContent = newContent
                },
                onScanClicked = { navController.navigate(ScannerRoute)}
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
                .background(brush = Brush.verticalGradient(colors = listOf(GradientEnd, GradientStart)))
        ) {

            NavHost(
                navController = navController,
                startDestination = startDestination){

                composable<HomeRoute> {
                    myCard?.let { card ->
                        BusinessCardScreen(
                            myCard = card,
                            qrContent = qrContent,
                            onScanClicked = {navController.navigate(ScannerRoute)}
                        )
                    }
                }
                composable<ListRoute> {
                    BusinessCardListScreen(
                        cards = collectedCards,
                        onItemClicked = { clickedCard ->
                            cardViewModel.deleteCollectedCard(clickedCard)
                        },
                        application
                    )
                }
                composable<CreateCardRoute> {
                    CreateCardScreen(
                        navigator = navController,
                        existingCard = myCard,
                        onCardSaved = { newCard -> cardViewModel.saveOrUpdateUserCard(newCard) },
                        application

                    )
                }
                composable<EditCardRoute> {
                    CreateCardScreen(
                        navigator = navController,
                        existingCard = myCard,
                        onCardSaved = { newCard -> cardViewModel.saveOrUpdateUserCard(newCard) },
                        application

                    )
                }
                composable<ScannerRoute> {
                    ScannerLauncher(
                        application = application,
                        onQrCodeScanned = { scannedData ->
                            val scannedUri = Uri.parse(scannedData)

                            if (scannedUri.scheme == "cardwallet" && scannedUri.host == "add") {
                                handleIntent(Intent(Intent.ACTION_VIEW, scannedUri), cardViewModel)
                            } else if (scannedUri.scheme == "https" && "play.google.com" in scannedUri.host.orEmpty()) {
                                application.startActivity(Intent(Intent.ACTION_VIEW, scannedUri))
                            }
                            navController.navigate(ListRoute) {
                                popUpTo(ScannerRoute) { inclusive = true }
                            }
                        }
                    )
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

