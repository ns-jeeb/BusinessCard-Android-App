package dev.najeeb.businesscard.cardwallet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.najeeb.businesscard.cardwallet.ui.theme.enabledColor


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