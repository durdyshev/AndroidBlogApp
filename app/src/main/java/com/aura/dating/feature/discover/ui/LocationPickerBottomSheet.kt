package com.aura.dating.feature.discover.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aura.dating.core.designsystem.theme.AuraRose
import com.aura.dating.core.designsystem.theme.DarkBorder
import com.aura.dating.core.designsystem.theme.DarkCardBackground
import com.aura.dating.core.designsystem.theme.DarkSurface
import com.aura.dating.core.designsystem.theme.Dimens

interface LocationItem {
    val id: String
    val displayName: String
    val extraInfo: String?
}

data class CountryLocationItem(val country: com.aura.dating.domain.location.model.Country) : LocationItem {
    override val id: String get() = country.id
    override val displayName: String get() = country.name
    override val extraInfo: String? get() = country.code
}

data class RegionLocationItem(val region: com.aura.dating.domain.location.model.Region) : LocationItem {
    override val id: String get() = region.id
    override val displayName: String get() = region.name
    override val extraInfo: String? get() = null
}

data class CityLocationItem(val city: com.aura.dating.domain.location.model.City) : LocationItem {
    override val id: String get() = city.id
    override val displayName: String get() = city.name
    override val extraInfo: String? get() = null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : LocationItem> LocationPickerBottomSheet(
    title: String,
    items: List<T>,
    selectedItem: T?,
    isLoading: Boolean = false,
    onRetry: (() -> Unit)? = null,
    onItemSelected: (T) -> Unit,
    onDismissRequest: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredItems = remember(items, searchQuery) {
        val uniqueItems = items.distinctBy { it.displayName.trim().lowercase() }
        if (searchQuery.isBlank()) uniqueItems
        else uniqueItems.filter { it.displayName.contains(searchQuery, ignoreCase = true) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = DarkSurface,
        scrimColor = Color.Black.copy(alpha = 0.6f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = Dimens.Spacing12)
                    .width(40.dp)
                    .height(4.dp)
                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(2.dp))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.Spacing20)
                .padding(bottom = Dimens.Spacing32)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                IconButton(onClick = onDismissRequest) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.Spacing12))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search...", color = Color.White.copy(alpha = 0.4f)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.5f)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AuraRose,
                    unfocusedBorderColor = DarkBorder,
                    focusedContainerColor = DarkCardBackground,
                    unfocusedContainerColor = DarkCardBackground,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(Dimens.Spacing16))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AuraRose)
                }
            } else if (filteredItems.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (items.isEmpty()) "No items available" else "No results found for '$searchQuery'",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    if (items.isEmpty() && onRetry != null) {
                        Spacer(modifier = Modifier.height(Dimens.Spacing12))
                        androidx.compose.material3.TextButton(onClick = onRetry) {
                            Text(
                                text = "Retry",
                                color = AuraRose,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp)
                ) {
                    items(filteredItems, key = { it.id }) { item ->
                        val isSelected = selectedItem?.id == item.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    onItemSelected(item)
                                    onDismissRequest()
                                }
                                .background(if (isSelected) AuraRose.copy(alpha = 0.15f) else Color.Transparent)
                                .padding(horizontal = Dimens.Spacing12, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(DarkCardBackground, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (item.extraInfo != null) Icons.Default.Public else Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = if (isSelected) AuraRose else Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(Dimens.Spacing12))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.displayName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) AuraRose else Color.White
                                )
                                if (!item.extraInfo.isNullOrBlank()) {
                                    Text(
                                        text = item.extraInfo!!,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.5f)
                                    )
                                }
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = AuraRose,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        HorizontalDivider(
                            color = DarkBorder.copy(alpha = 0.5f),
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(horizontal = Dimens.Spacing12)
                        )
                    }
                }
            }
        }
    }
}
