package com.aura.dating.feature.profile_creation.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.aura.dating.core.designsystem.components.AuraTopBar
import com.aura.dating.core.designsystem.components.InterestChip
import com.aura.dating.core.designsystem.components.PrimaryButton
import com.aura.dating.core.designsystem.theme.AuraRose
import com.aura.dating.core.designsystem.theme.DarkBackground
import com.aura.dating.core.designsystem.theme.Dimens
import com.aura.dating.core.designsystem.theme.PassColor
import com.aura.dating.domain.profile.model.GenderPreference
import com.aura.dating.feature.profile_creation.viewmodel.CreateProfileViewModel
import com.aura.dating.feature.profile_creation.viewmodel.ProfileCreationEvent
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SelectInterestsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToMain: () -> Unit,
    viewModel: CreateProfileViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is ProfileCreationEvent.NavigateToMain -> onNavigateToMain()
                is ProfileCreationEvent.NavigateToDatingPreferences -> onNavigateToMain()
                else -> {}
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
                title = "Step 3 of 4",
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
                    text = "Select Your Interests",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(Dimens.Spacing8))

                Text(
                    text = "Pick up to 8 interests to match with people who share your passions.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(Dimens.Spacing24))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Dimens.Spacing8),
                    verticalArrangement = Arrangement.spacedBy(Dimens.Spacing10),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    uiState.availableInterests.forEach { interest ->
                        val isSelected = uiState.selectedInterestIds.contains(interest.id)
                        InterestChip(
                            name = interest.name,
                            icon = interest.icon,
                            isSelected = isSelected,
                            onClick = { viewModel.toggleInterest(interest.id) }
                        )
                    }
                }

                if (uiState.errorMessage != null) {
                    Spacer(modifier = Modifier.height(Dimens.Spacing16))
                    Text(
                        text = uiState.errorMessage ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PassColor
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.Spacing24))
            }

            Box(modifier = Modifier.padding(horizontal = Dimens.Spacing24)) {
                PrimaryButton(
                    text = "Continue to Preferences",
                    isLoading = uiState.isLoading,
                    onClick = { viewModel.submitInterests() }
                )
            }
        }
    }
}

@Composable
fun DatingPreferencesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToMain: () -> Unit,
    viewModel: CreateProfileViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    var ageRange by remember(uiState.minAgePreference, uiState.maxAgePreference) {
        mutableStateOf(uiState.minAgePreference.toFloat()..uiState.maxAgePreference.toFloat())
    }
    var distance by remember(uiState.maxDistanceKm) {
        mutableFloatStateOf(uiState.maxDistanceKm.toFloat())
    }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            if (event is ProfileCreationEvent.NavigateToMain) {
                onNavigateToMain()
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
                title = "Step 4 of 4",
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
                    text = "Discovery Preferences",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(Dimens.Spacing8))

                Text(
                    text = "Set your matching criteria. You can change these anytime in filters.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(Dimens.Spacing32))

                // Interested In
                Text(
                    text = "Interested In",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(Dimens.Spacing10))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.Spacing8)
                ) {
                    listOf(
                        GenderPreference.ALL to "Everyone",
                        GenderPreference.WOMEN to "Women",
                        GenderPreference.MEN to "Men"
                    ).forEach { (pref, label) ->
                        InterestChip(
                            name = label,
                            isSelected = uiState.genderPreference == pref,
                            onClick = { viewModel.onGenderPreferenceChange(pref) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.Spacing24))

                // Max Distance
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Maximum Distance",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Text(
                        text = "${distance.roundToInt()} km",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AuraRose
                    )
                }
                Slider(
                    value = distance,
                    onValueChange = {
                        distance = it
                        viewModel.onDistanceChange(it.roundToInt())
                    },
                    valueRange = 5f..150f,
                    colors = SliderDefaults.colors(
                        thumbColor = AuraRose,
                        activeTrackColor = AuraRose
                    )
                )

                Spacer(modifier = Modifier.height(Dimens.Spacing24))

                // Age Preference
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Age Preference",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Text(
                        text = "${ageRange.start.roundToInt()} - ${ageRange.endInclusive.roundToInt()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AuraRose
                    )
                }
                RangeSlider(
                    value = ageRange,
                    onValueChange = {
                        ageRange = it
                        viewModel.onAgeRangeChange(it.start.roundToInt(), it.endInclusive.roundToInt())
                    },
                    valueRange = 18f..70f,
                    colors = SliderDefaults.colors(
                        thumbColor = AuraRose,
                        activeTrackColor = AuraRose
                    )
                )

                if (uiState.errorMessage != null) {
                    Spacer(modifier = Modifier.height(Dimens.Spacing16))
                    Text(
                        text = uiState.errorMessage ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PassColor
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.Spacing24))
            }

            Box(modifier = Modifier.padding(horizontal = Dimens.Spacing24)) {
                PrimaryButton(
                    text = "Finish & Discover",
                    isLoading = uiState.isLoading,
                    onClick = { viewModel.submitPreferences() }
                )
            }
        }
    }
}
