package com.beakshield.screens.knowledgeScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import beakshield.composeapp.generated.resources.Res
import beakshield.composeapp.generated.resources.knowledge_bg
import com.beakshield.borderColor
import com.beakshield.cardColor
import com.beakshield.composables.BasicBox
import com.beakshield.primaryColor
import com.beakshield.textColor
import com.beakshield.textSecondaryColor
import org.jetbrains.compose.resources.painterResource

@Preview
@Composable
fun KnowledgeBannerView(
    modifier: Modifier = Modifier,
    popularSearches: List<String> = listOf("USBManager", "Kotlin Coroutines", "Compose Navigation", "Email Tone"),
    onSearch: (String) -> Unit = {},
    onPopularSearchClick: (String) -> Unit = {}
) {
    val padBetween = 12

    BasicBox(
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Image(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(12.dp)),
                painter = painterResource(Res.drawable.knowledge_bg),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alpha = 0.4f
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 40.dp, vertical = 45.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Search the Kingdom's Knowledge",
                    fontFamily = FontFamily.Serif,
                    color = textColor,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
                Text(
                    modifier = Modifier.padding(top = 6.dp),
                    text = "Find anything Dawson and your agents have learned.",
                    fontFamily = FontFamily.SansSerif,
                    color = textSecondaryColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(28.dp))
                KnowledgeSearchBar(
                    modifier = Modifier.fillMaxWidth(),
                    onSearch = onSearch
                )
                Spacer(Modifier.height((padBetween * 2).dp))
                PopularSearchesRow(
                    popularSearches = popularSearches,
                    onPopularSearchClick = onPopularSearchClick
                )
            }
        }
    }
}

@Composable
private fun KnowledgeSearchBar(
    modifier: Modifier = Modifier,
    placeholderText: String = "Search knowledge...",
    onSearch: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = (1.5).dp,
                color = primaryColor.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
            .background(cardColor.copy(0.9f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(22.dp),
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = textColor
            )
            Spacer(modifier = Modifier.width(12.dp))
            BasicTextField(
                modifier = Modifier.weight(1f),
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                textStyle = TextStyle(
                    color = textColor,
                    fontSize = 15.sp
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        if (text.isNotEmpty()) {
                            onSearch(text)
                        }
                    }
                ),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (text.isEmpty()) {
                            Text(
                                text = placeholderText,
                                style = TextStyle(
                                    color = textSecondaryColor,
                                    fontSize = 15.sp
                                )
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
    }
}

@Composable
private fun PopularSearchesRow(
    popularSearches: List<String>,
    onPopularSearchClick: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.padding(end = 10.dp),
            text = "Popular searches:",
            color = textSecondaryColor,
            fontSize = 12.sp
        )
        popularSearches.forEach { search ->
            PopularSearchChip(
                modifier = Modifier.padding(end = 10.dp),
                text = search,
                onClick = { onPopularSearchClick(search) }
            )
        }
    }
}

@Composable
private fun PopularSearchChip(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(cardColor.copy(alpha = 0.9f))
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal
        )
    }
}