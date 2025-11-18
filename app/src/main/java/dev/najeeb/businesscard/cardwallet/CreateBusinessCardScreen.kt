package dev.najeeb.businesscard.cardwallet

import android.app.Application
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import dev.najeeb.businesscard.cardwallet.ui.theme.disabledColor
import dev.najeeb.businesscard.cardwallet.ui.theme.enabledColor

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
            imageDataString = imageUri,
            onImagePicked = { newImageDataString ->
                imageUri = newImageDataString
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
                    profilePictureUri = imageUri.toString()
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
