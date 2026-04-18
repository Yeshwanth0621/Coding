package com.kkc.clubconnect.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.kkc.clubconnect.ui.theme.Alert
import com.kkc.clubconnect.ui.theme.BackdropEnd
import com.kkc.clubconnect.ui.theme.BackdropMid
import com.kkc.clubconnect.ui.theme.Glow
import com.kkc.clubconnect.ui.theme.GlowSecondary
import com.kkc.clubconnect.ui.theme.Ink
import com.kkc.clubconnect.ui.theme.PanelBottom
import com.kkc.clubconnect.ui.theme.PanelTop
import com.kkc.clubconnect.ui.theme.Paper
import com.kkc.clubconnect.ui.theme.Slate
import com.kkc.clubconnect.ui.theme.SoftLine
import com.kkc.clubconnect.ui.theme.Teal

@Composable
fun ClubBackdrop(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(
            Brush.verticalGradient(
                colors = listOf(Paper, BackdropMid, BackdropEnd),
            ),
        ),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-70).dp, y = (-60).dp)
                .size(260.dp)
                .blur(120.dp)
                .background(Glow.copy(alpha = 0.28f), CircleShape),
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 60.dp, y = 30.dp)
                .size(320.dp)
                .blur(150.dp)
                .background(GlowSecondary.copy(alpha = 0.24f), CircleShape),
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-50).dp, y = 70.dp)
                .size(240.dp)
                .blur(110.dp)
                .background(Teal.copy(alpha = 0.16f), CircleShape),
        )
    }
}

@Composable
fun clubTextFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Ink,
    unfocusedTextColor = Ink,
    disabledTextColor = Slate,
    errorTextColor = Alert,
    focusedContainerColor = PanelTop.copy(alpha = 0.92f),
    unfocusedContainerColor = PanelBottom.copy(alpha = 0.86f),
    disabledContainerColor = PanelBottom.copy(alpha = 0.55f),
    errorContainerColor = PanelBottom.copy(alpha = 0.86f),
    cursorColor = Teal,
    focusedBorderColor = Teal,
    unfocusedBorderColor = SoftLine,
    disabledBorderColor = SoftLine.copy(alpha = 0.45f),
    errorBorderColor = Alert,
    focusedLabelColor = Teal,
    unfocusedLabelColor = Slate,
    disabledLabelColor = Slate.copy(alpha = 0.55f),
    errorLabelColor = Alert,
    focusedPlaceholderColor = Slate,
    unfocusedPlaceholderColor = Slate,
    disabledPlaceholderColor = Slate.copy(alpha = 0.55f),
    errorPlaceholderColor = Alert.copy(alpha = 0.8f),
    focusedLeadingIconColor = Teal,
    unfocusedLeadingIconColor = Slate,
    errorLeadingIconColor = Alert,
    focusedTrailingIconColor = Teal,
    unfocusedTrailingIconColor = Slate,
    errorTrailingIconColor = Alert,
)
