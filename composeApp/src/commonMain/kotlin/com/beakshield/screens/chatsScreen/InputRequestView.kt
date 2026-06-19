package com.beakshield.screens.chatsScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beakshield.borderColor
import com.beakshield.cardColor
import com.beakshield.dawsonGold
import com.beakshield.dawsonNavy
import com.beakshield.textPrimaryColor
import com.beakshield.websocket.UserInputRequest

@Composable
fun PendingInputRequestSegment(
    request: UserInputRequest,
    onApprove: () -> Unit,
    onDeny: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(dawsonNavy)
            .border(
                width = 1.dp,
                color = dawsonGold.copy(alpha = 0.7f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Text(
            text = request.type.name.lowercase().replaceFirstChar { it.uppercase() },
            color = dawsonGold,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            modifier = Modifier.padding(top = 6.dp),
            text = request.prompt,
            color = textPrimaryColor,
            fontSize = 14.sp,
            lineHeight = 18.sp
        )

        if (request.type == UserInputRequest.ReqType.PERMISSION) {
            Row(
                modifier = Modifier.padding(top = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PendingRequestButton(
                    text = "Approve",
                    onClick = onApprove
                )

                PendingRequestButton(
                    text = "Deny",
                    onClick = onDeny
                )
            }
        }
    }
}

@Composable
private fun PendingRequestButton(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(cardColor)
            .border(
                width = 1.dp,
                color = borderColor.copy(alpha = 0.85f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textPrimaryColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}