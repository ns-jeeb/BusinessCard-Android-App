package dev.najeeb.businesscard.cardwallet.ui.theme

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri

val btnModifier = Modifier
    .fillMaxWidth()
    .background(color = enabledColor)
    .clip(RoundedCornerShape(20))
val detailTextStyle = Modifier
    .fillMaxWidth()
    .padding(horizontal = 24.dp)
val actionModifier = Modifier.fillMaxWidth().clickable {}
    .background(color = GradientEnd.copy(alpha = 0.5f))
.border(1.dp, Purple80, RoundedCornerShape(8.dp))
.padding(2.dp,5.dp,2.dp,5.dp)
