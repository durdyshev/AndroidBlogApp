package com.aura.dating.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aura.dating.core.designsystem.theme.AuraRose
import com.aura.dating.core.designsystem.theme.Dimens
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSheet(
    minAge: Int,
    maxAge: Int,
    maxDistanceKm: Int,
    selectedGender: String, // "ALL", "WOMEN", "MEN"
    onlyOnline: Boolean,
    onApply: (minAge: Int, maxAge: Int, maxDist: Int, gender: String, online: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var currentAgeRange by remember { mutableStateOf(minAge.toFloat()..maxAge.toFloat()) }
    var currentDistance by remember { mutableFloatStateOf(maxDistanceKm.toFloat()) }
    var currentGender by remember { mutableStateOf(selectedGender) }
    var currentOnline by remember { mutableStateOf(onlyOnline) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.Spacing24)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Discovery Filters",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(Dimens.Spacing24))

            // Interested In (Gender)
            Text(
                text = "Show Me",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(Dimens.Spacing10))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.Spacing8)
            ) {
                listOf("ALL" to "Everyone", "WOMEN" to "Women", "MEN" to "Men").forEach { (key, label) ->
                    InterestChip(
                        name = label,
                        isSelected = currentGender == key,
                        onClick = { currentGender = key },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.Spacing24))

            // Maximum Distance
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Maximum Distance",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${currentDistance.roundToInt()} km",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AuraRose
                )
            }
            Slider(
                value = currentDistance,
                onValueChange = { currentDistance = it },
                valueRange = 2f..150f,
                colors = SliderDefaults.colors(
                    thumbColor = AuraRose,
                    activeTrackColor = AuraRose
                )
            )

            Spacer(modifier = Modifier.height(Dimens.Spacing24))

            // Age Range
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Age Preference",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${currentAgeRange.start.roundToInt()} - ${currentAgeRange.endInclusive.roundToInt()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AuraRose
                )
            }
            RangeSlider(
                value = currentAgeRange,
                onValueChange = { currentAgeRange = it },
                valueRange = 18f..70f,
                colors = SliderDefaults.colors(
                    thumbColor = AuraRose,
                    activeTrackColor = AuraRose
                )
            )

            Spacer(modifier = Modifier.height(Dimens.Spacing20))

            // Only Online Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Online Now",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Only show profiles active recently",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = currentOnline,
                    onCheckedChange = { currentOnline = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = AuraRose
                    )
                )
            }

            Spacer(modifier = Modifier.height(Dimens.Spacing32))

            PrimaryButton(
                text = "Apply Filters",
                onClick = {
                    onApply(
                        currentAgeRange.start.roundToInt(),
                        currentAgeRange.endInclusive.roundToInt(),
                        currentDistance.roundToInt(),
                        currentGender,
                        currentOnline
                    )
                    onDismiss()
                }
            )

            Spacer(modifier = Modifier.height(Dimens.Spacing32))
        }
    }
}
