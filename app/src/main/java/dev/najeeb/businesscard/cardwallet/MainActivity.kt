package dev.najeeb.businesscard.cardwallet

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import dev.najeeb.businesscard.cardwallet.ui.theme.BusinessCardTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.core.view.WindowCompat
import androidx.compose.ui.graphics.Brush
import dev.najeeb.businesscard.cardwallet.ui.theme.GradientEnd
import dev.najeeb.businesscard.cardwallet.ui.theme.GradientStart
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import dev.najeeb.businesscard.cardwallet.ui.theme.disabledColor
import dev.najeeb.businesscard.cardwallet.ui.theme.enabledColor
import dev.najeeb.businesscard.cardwallet.screens.BusinessCardApp
import dev.najeeb.businesscard.cardwallet.screens.BusinessCardScreen
import dev.najeeb.businesscard.cardwallet.screens.CreateCardRoute
import dev.najeeb.businesscard.cardwallet.screens.ListRoute
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.runtime.getValue
import dev.najeeb.businesscard.cardwallet.screens.HomeRoute

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
        val cardViewModel: CardViewModel by viewModels()
        handleIntent(intent, cardViewModel)
    }

}

@Composable
fun AppTopBar(
    navigator: NavController,
    onNavigateToMyCard: () -> Unit,
    onNavigateToList: () -> Unit,
    onEditCard: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(brush = Brush.verticalGradient(colors = listOf(GradientStart, GradientEnd)))
            .statusBarsPadding()
            .padding(0.dp, 15.dp, 0.dp, 0.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val navBackStackEntry by navigator.currentBackStackEntryAsState()
        val currentScreen = navBackStackEntry?.destination

        Button(
            onClick = onNavigateToMyCard,
            enabled = currentScreen?.hasRoute<HomeRoute>() == false,
            colors = ButtonDefaults.buttonColors(
                disabledContainerColor = disabledColor,
                containerColor = enabledColor,
            ),
            contentPadding = PaddingValues(15.dp, 0.dp, 15.dp, 0.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Text("My Card", fontSize = 10.sp)
        }

        Button(
            onClick = onNavigateToList,
            enabled = currentScreen?.hasRoute<ListRoute>() == false,
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
            enabled = currentScreen?.hasRoute<CreateCardRoute>()== false,
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

