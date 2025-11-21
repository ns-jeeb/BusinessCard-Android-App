package dev.najeeb.businesscard.cardwallet.screens

import androidx.compose.animation.core.copy
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import dev.najeeb.businesscard.cardwallet.CardViewModel
import dev.najeeb.businesscard.cardwallet.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrochureScreen
            (navController: NavController,
                   viewModel: CardViewModel) {
    val scrollState = rememberScrollState()
    val myCard by viewModel.userCard // Observe the card
    var isSaved = false
    var isEditable = false

    // Local state for editing
    var servicesText by remember { mutableStateOf(myCard?.services ?: "") }
    var aboutText by remember { mutableStateOf(myCard?.about ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                // 1. Title is a Composable block (usually a Text composable)
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Services", // Second Label
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )

                        Spacer(Modifier
                            .width(8.dp)
                            .weight(1f)) // Space between titles

                        // Use a subtle divider or secondary text style for the second label
                        Text(
                            modifier = Modifier.padding(end = 15.dp),
                            text = "About", // Second Label
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                },

                // 2. Navigation Icon (e.g., Back Button)
                navigationIcon = {
                    // Check if navigation back is possible
//                    if (navController.previousBackStackEntry != null) {
//                        IconButton(onClick = { navController.popBackStack() }) {
//                            Icon(
//                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
//                                contentDescription = "Back"
//                            )
//                        }
//                    }
                },

                // 3. Action Icons (e.g., Save, Settings, etc.)
                actions = {
//                    IconButton(onClick = {
//                        // Example navigation action
//                        navController.navigate(HomeRoute)
//                    }) {
//                        Icon(
//                            imageVector = Icons.Filled.Settings,
//                            contentDescription = "Settings"
//                        )
//                    }
                }
            )
        },floatingActionButton = {
            myCard?.let { card ->
                if (card.about.isNotEmpty()) {
                    FloatingActionButton(onClick = {

                    }){Icon(Icons.Default.Edit, "Edit")}
                    isEditable = false
                } else {
                    isEditable = true
                    FloatingActionButton(onClick = {
                        viewModel.saveOrUpdateUserCard(
                            card.copy(services = servicesText, about = aboutText)
                        )
                    }){Icon(Icons.Default.Save, "Save")}
                }
            }

        }
    ) { paddingValues ->
        Column(modifier = Modifier
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())) {

            // Service Section
            Text("My Services", style = MaterialTheme.typography.titleLarge)
            if (isEditable) {
                OutlinedTextField(
                    value = servicesText,
                    onValueChange = { servicesText = it },
                    label = { Text("List your services...") },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(servicesText) // Read-only for scanned cards
            }

            Spacer(modifier = Modifier.height(16.dp))

            // About Section
            Text("About Me", style = MaterialTheme.typography.titleLarge)
            if (isEditable) {
                OutlinedTextField(
                    value = aboutText,
                    onValueChange = { aboutText = it },
                    label = { Text("Tell your story...") },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(aboutText)
            }
        }
    }
}
@Composable
fun AboutScreen(isEditable: Boolean, servicesTextP: String, aboutTextP: String){
    var servicesText = servicesTextP
    var aboutText = aboutTextP
    Column(modifier = Modifier
        .padding(5.dp)
        .verticalScroll(rememberScrollState())) {

        // Service Section
        Text("My Services", style = MaterialTheme.typography.titleLarge)
        if (isEditable) {
            OutlinedTextField(
                value = servicesText,
                onValueChange = { servicesText = it },
                label = { Text("List your services...") },
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Text(servicesText) // Read-only for scanned cards
        }

        Spacer(modifier = Modifier.height(16.dp))

        // About Section
        Text("About Me", style = MaterialTheme.typography.titleLarge)
        if (isEditable) {
            OutlinedTextField(
                value = aboutText,
                onValueChange = { aboutText = it },
                label = { Text("Tell your story...") },
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Text(aboutText)
        }
    }
}