package dev.najeeb.businesscard.cardwallet.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

val btnModifier = Modifier
    .fillMaxWidth()
    .background(color = enabledColor)
    .clip(RoundedCornerShape(20))
val detailTextStyle = Modifier
    .fillMaxWidth()
    .padding(horizontal = 24.dp)
