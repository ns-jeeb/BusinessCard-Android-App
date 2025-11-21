package dev.najeeb.businesscard.cardwallet.screens

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.util.Log
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
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
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
import dev.najeeb.businesscard.cardwallet.generateUniversalLink

@Composable
fun BusinessCardApp(application: Application) {
    val navController = rememberNavController()
    val cardViewModel: CardViewModel = hiltViewModel()
    val myCard by cardViewModel.userCard
    val collectedCards by cardViewModel.allCards.observeAsState(initial = emptyList())
    val cardDataString = myCard?.let { generateCardDataString(it) } ?: ""

    val appUrl = myCard?.let { generateUniversalLink(it) }
        ?: "https://play.google.com/store/apps/details?id=dev.najeeb.businesscard.cardwallet"
    val context = LocalContext.current
    var qrContent by remember(cardDataString, appUrl) {
        mutableStateOf(if (cardDataString.isNotEmpty()) cardDataString else appUrl)
    }

    val startDestination: Any = if (myCard == null) CreateCardRoute else HomeRoute

    Scaffold(
        topBar = {
            AppTopBar(
                navigator = navController,
                onNavigateToMyCard = {
                    navController.navigate(HomeRoute)
                    qrContent = appUrl
                },
                onNavigateToList = {navController.navigate(ListRoute) },
                onEditCard = { navController.navigate(CreateCardRoute) }
            )
        },
        bottomBar = {
            AppBottomBar(
                navigator = navController,
                currentQrContent = qrContent,
                cardDataString = cardDataString,
                googlePlayUrl = appUrl,
                onQrContentChange = { newContent ->
                    qrContent = newContent
                },
                onScanClicked = { navController.navigate(ScannerRoute) }
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
                            navigator = navController,
                            myCard = card,
                            qrContent = qrContent,
                            onScanClicked = {navController.navigate(ScannerRoute)}
                        )
                    }
                }
                composable<ListRoute> {
                    BusinessCardListScreen(
                        navController = navController,
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

                    )
                }
                composable<EditCardRoute> {
                    CreateCardScreen(
                        navigator = navController,
                        existingCard = myCard,
                        onCardSaved = { newCard -> cardViewModel.saveOrUpdateUserCard(newCard) },
                        )
                }
                composable<BrochureRoute> {
                    BrochureScreen(
                        navController = navController,
                        viewModel = cardViewModel
                    )
                }
                composable<ScannerRoute> {
                    ScannerLauncher(
                        application = application,
                        onQrCodeScanned = { scannedData ->
                            if (scannedData.isEmpty()) {
                                navController.popBackStack()
                                return@ScannerLauncher
                            }

                            val scannedUri = Uri.parse(scannedData)
                            val scheme = scannedUri.scheme?.lowercase()
                            val host = scannedUri.host?.lowercase()

                            // CASE A: Raw Scheme (cardwallet://add)
                            if (scheme == "cardwallet" && host == "add") {
                                handleIntent(Intent(Intent.ACTION_VIEW, scannedUri), cardViewModel)
                                navController.navigate(ListRoute) {
                                    popUpTo(ScannerRoute) { inclusive = true }
                                }
                            }
                            // CASE B: Play Store Link... BUT CHECK FOR DATA FIRST!
                            else if (scheme == "https" && host != null && host.endsWith("play.google.com")) {
                                // Does it have our hidden data?
                                if (scannedUri.getQueryParameter("data") != null) {
                                    // Yes! Convert it to an intent that handleIntent() understands
                                    // We mock a "cardwallet" intent so we can reuse the parser logic

                                    val dataParam = scannedUri.getQueryParameter("data")
                                        ?: scannedUri.getQueryParameter("referrer")
                                    Log.d("BusinessCard", "Data Param: $dataParam")

                                    val mockUri = Uri.parse("cardwallet://add?data=$dataParam")
                                    handleIntent(Intent(Intent.ACTION_VIEW, mockUri), cardViewModel)

                                    navController.navigate(ListRoute) {
                                        popUpTo(ScannerRoute) { inclusive = true }
                                    }
                                } else {
                                    // No data, it's just a regular download link -> Open Browser
                                    val intent = Intent(Intent.ACTION_VIEW, scannedUri)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(intent)
                                    navController.popBackStack()
                                }
                            }
                            // CASE C: Unknown
                            else {
                                navController.popBackStack()
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

