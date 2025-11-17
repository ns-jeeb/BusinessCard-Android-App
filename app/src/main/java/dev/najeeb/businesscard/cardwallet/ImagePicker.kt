package dev.najeeb.businesscard.cardwallet
import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import dev.najeeb.businesscard.cardwallet.ui.theme.disabledColor
import dev.najeeb.businesscard.cardwallet.ui.theme.enabledColor
import java.io.File
@Composable
fun ImagePicker(
    currentImageUri: Uri?,
    onImagePicked: (Uri?) -> Unit,
    modifier: Modifier = Modifier,
    application: Application
) {
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                // When an image is picked, grant permanent read permission
                val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
                application.contentResolver.takePersistableUriPermission(uri, flag)
            }
            onImagePicked(uri)
        }
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Display the selected image
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .border(2.dp, Color.Gray, CircleShape)
                .clickable {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            if (currentImageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(currentImageUri),
                    contentDescription = "Profile Picture",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text("Add Photo")
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(onClick = {
            photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }, colors = ButtonDefaults.buttonColors(
            disabledContainerColor = disabledColor,
            containerColor = enabledColor,
        ),) {
            Text("Change Photo")
        }
    }
}