package com.aura.dating.feature.profile.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.aura.dating.core.designsystem.components.AuraTopBar
import com.aura.dating.core.designsystem.components.InterestChip
import com.aura.dating.core.designsystem.components.PrimaryButton
import com.aura.dating.core.designsystem.theme.AuraAmber
import com.aura.dating.core.designsystem.theme.AuraRose
import com.aura.dating.core.designsystem.theme.DarkBackground
import com.aura.dating.core.designsystem.theme.Dimens
import com.aura.dating.feature.profile.viewmodel.ProfileEvent
import com.aura.dating.feature.profile.viewmodel.ProfileViewModel

@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val profile = uiState.myProfile

    var displayName by remember(profile?.displayName) { mutableStateOf(profile?.displayName ?: "") }
    var bio by remember(profile?.bio) { mutableStateOf(profile?.bio ?: "") }
    var selectedCountry by remember(profile?.countryId) {
        mutableStateOf(
            if (profile?.countryId != null) com.aura.dating.domain.location.model.Country(profile.countryId, profile.countryName ?: "Selected Country")
            else null
        )
    }
    var selectedRegion by remember(profile?.regionId) {
        mutableStateOf(
            if (profile?.regionId != null && profile.countryId != null) com.aura.dating.domain.location.model.Region(profile.regionId, profile.countryId, profile.regionName ?: "Selected Region")
            else null
        )
    }
    var selectedCity by remember(profile?.cityId) {
        mutableStateOf(
            if (profile?.cityId != null && profile.regionId != null) com.aura.dating.domain.location.model.City(profile.cityId, profile.regionId, profile.cityName ?: "Selected City")
            else null
        )
    }

    var showCountryPicker by remember { mutableStateOf(false) }
    var showRegionPicker by remember { mutableStateOf(false) }
    var showCityPicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadCountries()
        viewModel.eventFlow.collect { event ->
            if (event is ProfileEvent.NavigateBack) {
                onNavigateBack()
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
                .padding(bottom = Dimens.Spacing32)
        ) {
            AuraTopBar(
                title = "Edit Profile",
                showBackButton = true,
                onBackClick = onNavigateBack
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = Dimens.Spacing24)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(Dimens.Spacing16))

                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Display Name") },
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

                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Bio") },
                    minLines = 4,
                    maxLines = 6,
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

                Text(
                    text = "Home Location",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(Dimens.Spacing8))

                EditProfileLocationTile(
                    label = "Country",
                    value = selectedCountry?.name ?: "Select Country",
                    isEnabled = true,
                    onClick = { showCountryPicker = true }
                )

                Spacer(modifier = Modifier.height(Dimens.Spacing8))

                EditProfileLocationTile(
                    label = "Region",
                    value = selectedRegion?.name ?: if (selectedCountry == null) "Select Country first" else "Select Region",
                    isEnabled = selectedCountry != null,
                    onClick = {
                        selectedCountry?.let { viewModel.loadRegions(it.id) }
                        showRegionPicker = true
                    }
                )

                Spacer(modifier = Modifier.height(Dimens.Spacing8))

                EditProfileLocationTile(
                    label = "City",
                    value = selectedCity?.name ?: if (selectedRegion == null) "Select Region first" else "Select City",
                    isEnabled = selectedRegion != null,
                    onClick = {
                        selectedRegion?.let { viewModel.loadCities(it.id) }
                        showCityPicker = true
                    }
                )

                Spacer(modifier = Modifier.height(Dimens.Spacing32))
            }

            Box(modifier = Modifier.padding(horizontal = Dimens.Spacing24)) {
                PrimaryButton(
                    text = "Save Changes",
                    isLoading = uiState.isLoading,
                    onClick = {
                        viewModel.updateProfile(
                            displayName = displayName,
                            bio = bio,
                            countryId = selectedCountry?.id,
                            regionId = selectedRegion?.id,
                            cityId = selectedCity?.id
                        )
                    }
                )
            }
        }
    }

    if (showCountryPicker) {
        com.aura.dating.feature.discover.ui.LocationPickerBottomSheet(
            title = "Select Country",
            items = uiState.countries.map { com.aura.dating.feature.discover.ui.CountryLocationItem(it) },
            selectedItem = selectedCountry?.let { com.aura.dating.feature.discover.ui.CountryLocationItem(it) },
            isLoading = uiState.isLoadingLocations && uiState.countries.isEmpty(),
            onItemSelected = { item ->
                selectedCountry = item.country
                selectedRegion = null
                selectedCity = null
            },
            onDismissRequest = { showCountryPicker = false }
        )
    }

    if (showRegionPicker) {
        com.aura.dating.feature.discover.ui.LocationPickerBottomSheet(
            title = "Select Region",
            items = uiState.regions.map { com.aura.dating.feature.discover.ui.RegionLocationItem(it) },
            selectedItem = selectedRegion?.let { com.aura.dating.feature.discover.ui.RegionLocationItem(it) },
            isLoading = uiState.isLoadingLocations && uiState.regions.isEmpty(),
            onItemSelected = { item ->
                selectedRegion = item.region
                selectedCity = null
            },
            onDismissRequest = { showRegionPicker = false }
        )
    }

    if (showCityPicker) {
        com.aura.dating.feature.discover.ui.LocationPickerBottomSheet(
            title = "Select City",
            items = uiState.cities.map { com.aura.dating.feature.discover.ui.CityLocationItem(it) },
            selectedItem = selectedCity?.let { com.aura.dating.feature.discover.ui.CityLocationItem(it) },
            isLoading = uiState.isLoadingLocations && uiState.cities.isEmpty(),
            onItemSelected = { item ->
                selectedCity = item.city
            },
            onDismissRequest = { showCityPicker = false }
        )
    }
}

@Composable
private fun EditProfileLocationTile(
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

@Composable
fun EditPhotosScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val photos = uiState.myProfile?.photos ?: emptyList()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let { viewModel.uploadPhoto(context, it) }
        }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = Dimens.Spacing32)
        ) {
            AuraTopBar(
                title = "Edit Photos",
                showBackButton = true,
                onBackClick = onNavigateBack
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.Spacing24)
            ) {
                Text(
                    text = "Manage Profile Photos",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(Dimens.Spacing8))
                Text(
                    text = "Tap a photo to set as primary, or tap + to upload new pictures.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(Dimens.Spacing24))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.Spacing10),
                    verticalArrangement = Arrangement.spacedBy(Dimens.Spacing10),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(6) { index ->
                        val photo = photos.getOrNull(index)

                        Box(
                            modifier = Modifier
                                .aspectRatio(0.8f)
                                .clip(RoundedCornerShape(Dimens.RadiusMedium))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                .border(
                                    1.dp,
                                    if (photo?.isPrimary == true) AuraRose else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                    RoundedCornerShape(Dimens.RadiusMedium)
                                )
                                .then(
                                    if (photo == null && !uiState.isUploadingPhoto) {
                                        Modifier.clickable { photoPickerLauncher.launch("image/*") }
                                    } else if (photo != null) {
                                        Modifier.clickable { viewModel.setPrimaryPhoto(photo) }
                                    } else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (photo != null) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(photo.photoUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                if (photo.isPrimary) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(Dimens.Spacing6)
                                            .clip(CircleShape)
                                            .background(AuraAmber)
                                            .padding(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = "Primary",
                                            tint = Color.Black,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { viewModel.deletePhoto(photo) },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.6f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Delete",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add",
                                    tint = AuraRose,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }

                if (uiState.isUploadingPhoto) {
                    Spacer(modifier = Modifier.height(Dimens.Spacing16))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = AuraRose, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(Dimens.Spacing8))
                        Text(
                            text = "Uploading photo...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditInterestsScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentSelected = remember(uiState.myProfile?.interests) {
        uiState.myProfile?.interests?.map { it.id }?.toMutableSet() ?: mutableSetOf()
    }
    var selectedIds by remember { mutableStateOf(currentSelected) }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            if (event is ProfileEvent.NavigateBack) {
                onNavigateBack()
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
                .padding(bottom = Dimens.Spacing32)
        ) {
            AuraTopBar(
                title = "Edit Interests",
                showBackButton = true,
                onBackClick = onNavigateBack
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = Dimens.Spacing24)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Select Interests (${selectedIds.size}/8)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(Dimens.Spacing16))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Dimens.Spacing8),
                    verticalArrangement = Arrangement.spacedBy(Dimens.Spacing10),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    uiState.availableInterests.forEach { interest ->
                        val isSelected = selectedIds.contains(interest.id)
                        InterestChip(
                            name = interest.name,
                            icon = interest.icon,
                            isSelected = isSelected,
                            onClick = {
                                val updated = selectedIds.toMutableSet()
                                if (isSelected) updated.remove(interest.id)
                                else if (updated.size < 8) updated.add(interest.id)
                                selectedIds = updated
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.Spacing32))
            }

            Box(modifier = Modifier.padding(horizontal = Dimens.Spacing24)) {
                PrimaryButton(
                    text = "Save Interests",
                    isLoading = uiState.isLoading,
                    onClick = { viewModel.updateInterests(selectedIds.toList()) }
                )
            }
        }
    }
}
