package com.aura.dating.feature.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.aura.dating.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aura.dating.core.designsystem.components.AuraTopBar
import com.aura.dating.core.designsystem.components.PrimaryButton
import com.aura.dating.core.designsystem.theme.AuraRose
import com.aura.dating.core.designsystem.theme.DarkBackground
import com.aura.dating.core.designsystem.theme.Dimens
import com.aura.dating.core.designsystem.theme.PassColor
import com.aura.dating.feature.auth.viewmodel.AuthEvent
import com.aura.dating.feature.auth.viewmodel.AuthViewModel

@Composable
fun VerificationScreen(
    email: String,
    onNavigateBack: () -> Unit,
    onNavigateToCreateProfile: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is AuthEvent.NavigateToCreateProfile -> onNavigateToCreateProfile()
                is AuthEvent.ShowToast -> {
                    android.widget.Toast.makeText(context, event.message, android.widget.Toast.LENGTH_SHORT).show()
                }
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
                title = "",
                showBackButton = true,
                onBackClick = onNavigateBack
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.Spacing24)
                    .padding(top = Dimens.Spacing16),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.MarkEmailRead,
                    contentDescription = null,
                    tint = AuraRose,
                    modifier = Modifier.padding(Dimens.Spacing16)
                )

                Text(
                    text = stringResource(R.string.verify_your_email),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(Dimens.Spacing8))

                Text(
                    text = stringResource(R.string.verification_code_sent_description, email),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(Dimens.Spacing32))

                OutlinedTextField(
                    value = uiState.verificationCode,
                    onValueChange = viewModel::onVerificationCodeChange,
                    label = { Text(stringResource(R.string.verification_code_label)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(Dimens.RadiusMedium),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AuraRose,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (uiState.errorMessage != null) {
                    Spacer(modifier = Modifier.height(Dimens.Spacing16))
                    Text(
                        text = uiState.errorMessage ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PassColor,
                        textAlign = TextAlign.Center
                    )
                }

                if (uiState.infoMessage != null) {
                    Spacer(modifier = Modifier.height(Dimens.Spacing16))
                    Text(
                        text = uiState.infoMessage ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AuraRose,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.Spacing32))

                PrimaryButton(
                    text = stringResource(R.string.verify_and_continue),
                    isLoading = uiState.isLoading,
                    onClick = { viewModel.verifyEmail(email) }
                )

                Spacer(modifier = Modifier.height(Dimens.Spacing16))

                androidx.compose.material3.TextButton(
                    onClick = { viewModel.resendVerificationCode(email) },
                    enabled = !uiState.isLoading
                ) {
                    Text(
                        text = stringResource(R.string.didnt_receive_code_resend),
                        style = MaterialTheme.typography.labelLarge,
                        color = AuraRose,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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
                title = "",
                showBackButton = true,
                onBackClick = onNavigateBack
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.Spacing24)
                    .padding(top = Dimens.Spacing16)
            ) {
                Text(
                    text = stringResource(R.string.reset_password),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(Dimens.Spacing8))

                Text(
                    text = stringResource(R.string.reset_password_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(Dimens.Spacing32))

                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = viewModel::onEmailChange,
                    label = { Text(stringResource(R.string.email_address)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(Dimens.RadiusMedium),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AuraRose,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (uiState.errorMessage != null) {
                    Spacer(modifier = Modifier.height(Dimens.Spacing16))
                    Text(
                        text = uiState.errorMessage ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PassColor
                    )
                }

                if (uiState.infoMessage != null) {
                    Spacer(modifier = Modifier.height(Dimens.Spacing16))
                    Text(
                        text = uiState.infoMessage ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AuraRose
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.Spacing32))

                PrimaryButton(
                    text = stringResource(R.string.send_reset_link),
                    isLoading = uiState.isLoading,
                    onClick = { viewModel.sendPasswordReset() }
                )
            }
        }
    }
}
