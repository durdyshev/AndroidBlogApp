package com.aura.dating.feature.discover.ui

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.aura.dating.R
import com.aura.dating.core.designsystem.components.AuraTopBar
import com.aura.dating.core.designsystem.components.PrimaryButton
import com.aura.dating.core.designsystem.theme.AuraPrimaryGradient
import com.aura.dating.core.designsystem.theme.AuraRose
import com.aura.dating.core.designsystem.theme.DarkBackground
import com.aura.dating.core.designsystem.theme.DarkBorder
import com.aura.dating.core.designsystem.theme.DarkCardBackground
import com.aura.dating.core.designsystem.theme.Dimens
import com.aura.dating.feature.discover.viewmodel.LocationSearchEvent
import com.aura.dating.feature.discover.viewmodel.LocationSearchViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LocationSearchScreen(
    onNavigateBack: () -> Unit,
    onNavigateToResults: (title: String, subtitle: String) -> Unit,
    viewModel: LocationSearchViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    var showCountryPicker by remember { mutableStateOf(false) }
    var showRegionPicker by remember { mutableStateOf(false) }
    var showCityPicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (uiState.countries.isEmpty()) {
            viewModel.loadCountries()
        }
        viewModel.eventFlow.collect { event ->
            if (event is LocationSearchEvent.NavigateToResults) {
                onNavigateToResults(event.title, event.subtitle)
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
                .padding(bottom = 90.dp)
        ) {
            AuraTopBar(
                title = stringResource(R.string.search_people),
                showBackButton = true,
                onBackClick = onNavigateBack,
                actions = {
                    TextButton(onClick = { viewModel.resetFilters() }) {
                        Text(
                            text = stringResource(R.string.reset),
                            color = AuraRose,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Dimens.Spacing20, vertical = Dimens.Spacing8)
            ) {
                Text(
                    text = stringResource(R.string.find_people_location_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(Dimens.Spacing20))

                // Active Filter Chips
                val hasActiveFilters = uiState.selectedCountry != null ||
                        uiState.selectedRegion != null ||
                        uiState.selectedCity != null ||
                        uiState.minAge > 18 ||
                        uiState.maxAge < 75 ||
                        uiState.gender != "ALL" ||
                        uiState.onlyOnline

                if (hasActiveFilters) {
                    Text(
                        text = stringResource(R.string.active_filters),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(Dimens.Spacing8))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.selectedCountry?.let { country ->
                            ActiveFilterChip(
                                label = country.name,
                                onRemove = { viewModel.removeCountryFilter() }
                            )
                        }
                        uiState.selectedRegion?.let { region ->
                            ActiveFilterChip(
                                label = region.name,
                                onRemove = { viewModel.removeRegionFilter() }
                            )
                        }
                        uiState.selectedCity?.let { city ->
                            ActiveFilterChip(
                                label = city.name,
                                onRemove = { viewModel.removeCityFilter() }
                            )
                        }
                        if (uiState.minAge > 18 || uiState.maxAge < 75) {
                            ActiveFilterChip(
                                label = "${uiState.minAge}–${uiState.maxAge}",
                                onRemove = { viewModel.onAgeRangeChange(18, 75) }
                            )
                        }
                        if (uiState.gender != "ALL") {
                            val genderLabel = when (uiState.gender) {
                                "WOMEN" -> stringResource(R.string.gender_women)
                                "MEN" -> stringResource(R.string.gender_men)
                                "NON_BINARY" -> stringResource(R.string.gender_non_binary)
                                else -> uiState.gender
                            }
                            ActiveFilterChip(
                                label = genderLabel,
                                onRemove = { viewModel.onGenderChange("ALL") }
                            )
                        }
                        if (uiState.onlyOnline) {
                            ActiveFilterChip(
                                label = stringResource(R.string.online_now),
                                onRemove = { viewModel.onOnlineOnlyChange(false) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(Dimens.Spacing20))
                }

                // Section 1: Location Hierarchy
                Text(
                    text = stringResource(R.string.location_hierarchy),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(Dimens.Spacing12))

                LocationSelectorTile(
                    title = stringResource(R.string.country),
                    selectedValue = uiState.selectedCountry?.name ?: stringResource(R.string.select_country),
                    icon = Icons.Default.Public,
                    isEnabled = true,
                    onClick = {
                        if (uiState.countries.isEmpty()) {
                            viewModel.loadCountries(forceRefresh = true)
                        }
                        showCountryPicker = true
                    }
                )

                Spacer(modifier = Modifier.height(Dimens.Spacing12))

                LocationSelectorTile(
                    title = stringResource(R.string.region_province),
                    selectedValue = uiState.selectedRegion?.name ?: if (uiState.selectedCountry == null) stringResource(R.string.select_country_first) else stringResource(R.string.select_region),
                    icon = Icons.Default.Map,
                    isEnabled = uiState.selectedCountry != null,
                    onClick = {
                        uiState.selectedCountry?.let {
                            if (uiState.regions.isEmpty()) {
                                viewModel.loadRegions(it.id, forceRefresh = true)
                            }
                        }
                        showRegionPicker = true
                    }
                )

                Spacer(modifier = Modifier.height(Dimens.Spacing12))

                LocationSelectorTile(
                    title = stringResource(R.string.city),
                    selectedValue = uiState.selectedCity?.name ?: if (uiState.selectedRegion == null) stringResource(R.string.select_region_first) else stringResource(R.string.select_city),
                    icon = Icons.Default.LocationCity,
                    isEnabled = uiState.selectedRegion != null,
                    onClick = {
                        uiState.selectedRegion?.let {
                            if (uiState.cities.isEmpty()) {
                                viewModel.loadCities(it.id, forceRefresh = true)
                            }
                        }
                        showCityPicker = true
                    }
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Section 2: Gender
                Text(
                    text = stringResource(R.string.looking_for),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(Dimens.Spacing12))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val genders = listOf(
                        "WOMEN" to stringResource(R.string.gender_women),
                        "MEN" to stringResource(R.string.gender_men),
                        "ALL" to stringResource(R.string.gender_everyone)
                    )
                    genders.forEach { (key, label) ->
                        val isSelected = uiState.gender == key
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) AuraRose else DarkBorder,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .background(if (isSelected) AuraRose.copy(alpha = 0.15f) else DarkCardBackground)
                                .clickable { viewModel.onGenderChange(key) }
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) AuraRose else Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Section 3: Age Range
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.age_preference),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${uiState.minAge} – ${uiState.maxAge}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AuraRose
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.Spacing8))

                RangeSlider(
                    value = uiState.minAge.toFloat()..uiState.maxAge.toFloat(),
                    onValueChange = { range ->
                        viewModel.onAgeRangeChange(range.start.toInt(), range.endInclusive.toInt())
                    },
                    valueRange = 18f..75f,
                    steps = 56,
                    colors = SliderDefaults.colors(
                        thumbColor = AuraRose,
                        activeTrackColor = AuraRose,
                        inactiveTrackColor = DarkBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Section 4: Online Now Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(
                            width = 1.dp,
                            color = if (uiState.onlyOnline) AuraRose.copy(alpha = 0.5f) else DarkBorder,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .background(if (uiState.onlyOnline) AuraRose.copy(alpha = 0.08f) else DarkCardBackground)
                        .clickable { viewModel.onOnlineOnlyChange(!uiState.onlyOnline) }
                        .padding(horizontal = Dimens.Spacing16, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        if (uiState.onlyOnline) Color(0xFF4CAF50) else Color.White.copy(alpha = 0.3f),
                                        CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.online_now),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.online_now_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                    Switch(
                        checked = uiState.onlyOnline,
                        onCheckedChange = { viewModel.onOnlineOnlyChange(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = AuraRose,
                            uncheckedThumbColor = Color.White.copy(alpha = 0.7f),
                            uncheckedTrackColor = DarkBorder
                        )
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.Spacing20))
            }
        }

        // Bottom Search Button
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = DarkBackground.copy(alpha = 0.95f),
            shadowElevation = 8.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.Spacing20, vertical = Dimens.Spacing16)
            ) {
                PrimaryButton(
                    text = stringResource(R.string.search_people),
                    onClick = { viewModel.executeSearch(isNewSearch = true) },
                    isLoading = uiState.isSearching,
                    leadingIcon = Icons.Default.Search,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // Country Picker Sheet
    if (showCountryPicker) {
        LocationPickerBottomSheet(
            title = stringResource(R.string.select_country),
            items = uiState.countries.map { CountryLocationItem(it) },
            selectedItem = uiState.selectedCountry?.let { CountryLocationItem(it) },
            isLoading = uiState.isLoadingLocations,
            onRetry = { viewModel.loadCountries(forceRefresh = true) },
            onItemSelected = { item ->
                viewModel.selectCountry(item.country)
            },
            onDismissRequest = { showCountryPicker = false }
        )
    }

    // Region Picker Sheet
    if (showRegionPicker) {
        LocationPickerBottomSheet(
            title = stringResource(R.string.select_region),
            items = uiState.regions.map { RegionLocationItem(it) },
            selectedItem = uiState.selectedRegion?.let { RegionLocationItem(it) },
            isLoading = uiState.isLoadingLocations,
            onRetry = { uiState.selectedCountry?.let { viewModel.loadRegions(it.id, forceRefresh = true) } },
            onItemSelected = { item ->
                viewModel.selectRegion(item.region)
            },
            onDismissRequest = { showRegionPicker = false }
        )
    }

    // City Picker Sheet
    if (showCityPicker) {
        LocationPickerBottomSheet(
            title = stringResource(R.string.select_city),
            items = uiState.cities.map { CityLocationItem(it) },
            selectedItem = uiState.selectedCity?.let { CityLocationItem(it) },
            isLoading = uiState.isLoadingLocations,
            onRetry = { uiState.selectedRegion?.let { viewModel.loadCities(it.id, forceRefresh = true) } },
            onItemSelected = { item ->
                viewModel.selectCity(item.city)
            },
            onDismissRequest = { showCityPicker = false }
        )
    }
}

@Composable
private fun LocationSelectorTile(
    title: String,
    selectedValue: String,
    icon: ImageVector,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(
                width = 1.dp,
                color = if (isEnabled) DarkBorder else DarkBorder.copy(alpha = 0.3f),
                shape = RoundedCornerShape(14.dp)
            )
            .background(if (isEnabled) DarkCardBackground else DarkCardBackground.copy(alpha = 0.4f))
            .clickable(enabled = isEnabled, onClick = onClick)
            .padding(horizontal = Dimens.Spacing16, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    if (isEnabled) AuraRose.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.05f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isEnabled) AuraRose else Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.5f)
            )
            Text(
                text = selectedValue,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (isEnabled) Color.White else Color.White.copy(alpha = 0.3f)
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = if (isEnabled) Color.White.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.2f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun ActiveFilterChip(
    label: String,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(AuraRose.copy(alpha = 0.15f))
            .border(1.dp, AuraRose.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .padding(horizontal = Dimens.Spacing12, vertical = Dimens.Spacing6),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = AuraRose
        )
        Spacer(modifier = Modifier.width(Dimens.Spacing6))
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = stringResource(R.string.remove_filter),
            tint = AuraRose,
            modifier = Modifier
                .size(16.dp)
                .clickable(onClick = onRemove)
        )
    }
}
