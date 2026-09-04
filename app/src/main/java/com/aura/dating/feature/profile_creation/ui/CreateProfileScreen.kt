package com.aura.dating.feature.profile_creation.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.aura.dating.core.common.utils.DateTimeUtils
import com.aura.dating.core.designsystem.components.AuraDatePickerDialog
import com.aura.dating.core.designsystem.components.AuraTopBar
import com.aura.dating.core.designsystem.components.InterestChip
import com.aura.dating.core.designsystem.components.PrimaryButton
import com.aura.dating.core.designsystem.theme.AuraRose
import com.aura.dating.core.designsystem.theme.DarkBackground
import com.aura.dating.core.designsystem.theme.Dimens
import com.aura.dating.core.designsystem.theme.PassColor
import com.aura.dating.domain.profile.model.Gender
import com.aura.dating.feature.profile_creation.viewmodel.CreateProfileViewModel
import com.aura.dating.feature.profile_creation.viewmodel.ProfileCreationEvent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CreateProfileScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddPhotos: () -> Unit,
    viewModel: CreateProfileViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDatePickerDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            if (event is ProfileCreationEvent.NavigateToAddPhotos) {
                onNavigateToAddPhotos()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            AuraTopBar(
                title = "Step 1 of 4",
                showBackButton = true,
                onBackClick = onNavigateBack
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.Spacing24)
                    .padding(top = Dimens.Spacing8, bottom = Dimens.Spacing32)
            ) {
                Text(
                    text = "About You",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(Dimens.Spacing8))

                Text(
                    text = "Let others know who you are. Adults 18+ only.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(Dimens.Spacing32))

                // Display Name
                OutlinedTextField(
                    value = uiState.displayName,
                    onValueChange = viewModel::onDisplayNameChange,
                    label = { Text("Display Name") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.White.copy(alpha = 0.7f))
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(Dimens.RadiusMedium),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AuraRose,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(Dimens.Spacing20))

                // Birth Date Picker
                val formattedBirthDate = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(Date(uiState.birthDateMillis))
                val age = DateTimeUtils.calculateAge(uiState.birthDateMillis)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Dimens.RadiusMedium))
                        .clickable { showDatePickerDialog = true }
                ) {
                    OutlinedTextField(
                        value = "$formattedBirthDate ($age years old)",
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        label = { Text("Birthday") },
                        leadingIcon = {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color.White.copy(alpha = 0.7f))
                        },
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.White.copy(alpha = 0.7f))
                        },
                        shape = RoundedCornerShape(Dimens.RadiusMedium),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = Color.White,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = Color.White.copy(alpha = 0.7f),
                            disabledLeadingIconColor = Color.White.copy(alpha = 0.7f),
                            disabledTrailingIconColor = Color.White.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.Spacing20))

                // Gender Selection
                Text(
                    text = "I am a",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(Dimens.Spacing10))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.Spacing8)
                ) {
                    listOf(
                        Gender.WOMAN to "Woman",
                        Gender.MAN to "Man",
                        Gender.NON_BINARY to "Non-Binary"
                    ).forEach { (gender, label) ->
                        InterestChip(
                            name = label,
                            isSelected = uiState.gender == gender,
                            onClick = { viewModel.onGenderChange(gender) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.Spacing20))

                // Bio Field
                OutlinedTextField(
                    value = uiState.bio,
                    onValueChange = viewModel::onBioChange,
                    label = { Text("Short Bio (Optional)") },
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(Dimens.RadiusMedium),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AuraRose,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(Dimens.Spacing24))

                // Location Section
                Text(
                    text = "Your Location",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(Dimens.Spacing8))

                // Country Selector
                var showCountryPicker by remember { mutableStateOf(false) }
                var showRegionPicker by remember { mutableStateOf(false) }
                var showCityPicker by remember { mutableStateOf(false) }

                LocationSelectionRow(
                    label = "Country",
                    value = uiState.selectedCountry?.name ?: "Select Country",
                    isEnabled = true,
                    onClick = { showCountryPicker = true }
                )

                Spacer(modifier = Modifier.height(Dimens.Spacing8))

                LocationSelectionRow(
                    label = "Region",
                    value = uiState.selectedRegion?.name ?: if (uiState.selectedCountry == null) "Select Country first" else "Select Region",
                    isEnabled = uiState.selectedCountry != null,
                    onClick = { showRegionPicker = true }
                )

                Spacer(modifier = Modifier.height(Dimens.Spacing8))

                LocationSelectionRow(
                    label = "City",
                    value = uiState.selectedCity?.name ?: if (uiState.selectedRegion == null) "Select Region first" else "Select City",
                    isEnabled = uiState.selectedRegion != null,
                    onClick = { showCityPicker = true }
                )

                if (uiState.errorMessage != null) {
                    Spacer(modifier = Modifier.height(Dimens.Spacing16))
                    Text(
                        text = uiState.errorMessage ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PassColor
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.Spacing32))

                PrimaryButton(
                    text = "Continue to Photos",
                    isLoading = uiState.isLoading,
                    onClick = { viewModel.submitBasicInfo() }
                )

                if (showCountryPicker) {
                    com.aura.dating.feature.discover.ui.LocationPickerBottomSheet(
                        title = "Select Country",
                        items = uiState.countries.map { com.aura.dating.feature.discover.ui.CountryLocationItem(it) },
                        selectedItem = uiState.selectedCountry?.let { com.aura.dating.feature.discover.ui.CountryLocationItem(it) },
                        isLoading = uiState.isLoadingLocations && uiState.countries.isEmpty(),
                        onItemSelected = { item -> viewModel.selectCountry(item.country) },
                        onDismissRequest = { showCountryPicker = false }
                    )
                }

                if (showRegionPicker) {
                    com.aura.dating.feature.discover.ui.LocationPickerBottomSheet(
                        title = "Select Region",
                        items = uiState.regions.map { com.aura.dating.feature.discover.ui.RegionLocationItem(it) },
                        selectedItem = uiState.selectedRegion?.let { com.aura.dating.feature.discover.ui.RegionLocationItem(it) },
                        isLoading = uiState.isLoadingLocations && uiState.regions.isEmpty(),
                        onItemSelected = { item -> viewModel.selectRegion(item.region) },
                        onDismissRequest = { showRegionPicker = false }
                    )
                }

                if (showCityPicker) {
                    com.aura.dating.feature.discover.ui.LocationPickerBottomSheet(
                        title = "Select City",
                        items = uiState.cities.map { com.aura.dating.feature.discover.ui.CityLocationItem(it) },
                        selectedItem = uiState.selectedCity?.let { com.aura.dating.feature.discover.ui.CityLocationItem(it) },
                        isLoading = uiState.isLoadingLocations && uiState.cities.isEmpty(),
                        onItemSelected = { item -> viewModel.selectCity(item.city) },
                        onDismissRequest = { showCityPicker = false }
                    )
                }

                if (showDatePickerDialog) {
                    AuraDatePickerDialog(
                        initialDateMillis = uiState.birthDateMillis,
                        onDateSelected = { selectedMillis ->
                            viewModel.onBirthDateChange(selectedMillis)
                        },
                        onDismissRequest = {
                            showDatePickerDialog = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LocationSelectionRow(
    label: String,
    value: String,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.RadiusMedium))
            .background(if (isEnabled) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
            .clickable(enabled = isEnabled, onClick = onClick)
            .padding(horizontal = Dimens.Spacing16, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.5f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isEnabled) Color.White else Color.White.copy(alpha = 0.3f)
            )
        }
    }
}
