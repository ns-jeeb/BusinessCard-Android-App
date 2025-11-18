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
import coil.compose.rememberAsyncImagePainter
import dev.najeeb.businesscard.cardwallet.ui.theme.disabledColor
import dev.najeeb.businesscard.cardwallet.ui.theme.enabledColor

import android.util.Base64
import androidx.compose.ui.res.painterResource
import androidx.core.net.toUri

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
            .background(brush = Brush.verticalGradient(colors = listOf(GradientEnd, GradientEnd)))
            .statusBarsPadding()
            .padding(0.dp, 15.dp, 0.dp, 0.dp),
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



@Composable
fun AppBottomBar(
    currentQrContent: String,
    cardDataString: String,
    googlePlayUrl: String,
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

