package com.aura.dating.feature.chat.ui

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aura.dating.core.designsystem.components.Avatar
import com.aura.dating.core.designsystem.components.ConfirmationDialog
import com.aura.dating.core.designsystem.components.MessageBubble
import com.aura.dating.core.designsystem.components.MessageBubbleType
import com.aura.dating.core.designsystem.components.MessageDeliveryStatus
import com.aura.dating.core.designsystem.theme.AuraPrimaryGradient
import com.aura.dating.core.designsystem.theme.AuraRose
import com.aura.dating.core.designsystem.theme.DarkBackground
import com.aura.dating.core.designsystem.theme.Dimens
import com.aura.dating.core.designsystem.theme.PassColor
import com.aura.dating.domain.chat.model.MessageStatus
import com.aura.dating.domain.chat.model.MessageType
import com.aura.dating.feature.chat.viewmodel.ConversationEvent
import com.aura.dating.feature.chat.viewmodel.ConversationViewModel
import com.aura.dating.feature.settings.ui.ReportUserSheet

@Composable
fun ConversationScreen(
    onNavigateBack: () -> Unit,
    onNavigateToUserProfile: (String) -> Unit,
    viewModel: ConversationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val listState = rememberLazyListState()

    var showMenu by remember { mutableStateOf(false) }
    var showUnmatchDialog by remember { mutableStateOf(false) }
    var showBlockDialog by remember { mutableStateOf(false) }
    var showReportSheet by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let { viewModel.sendImage(context, it) }
        }
    )

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is ConversationEvent.NavigateBack -> onNavigateBack()
                is ConversationEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.lastIndex)
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
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            // Header Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.Spacing16, vertical = Dimens.Spacing8),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(Dimens.Spacing8))

                    Avatar(
                        imageUrl = uiState.matchPhotoUrl,
                        name = uiState.matchName,
                        size = 40.dp
                    )

                    Spacer(modifier = Modifier.width(Dimens.Spacing12))

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { /* open profile */ }
                    ) {
                        Text(
                            text = uiState.matchName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (uiState.isPartnerTyping) {
                            Text(
                                text = "typing...",
                                style = MaterialTheme.typography.bodySmall,
                                color = AuraRose
                            )
                        }
                    }

                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Menu",
                                tint = Color.White
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Unmatch") },
                                onClick = {
                                    showMenu = false
                                    showUnmatchDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Report User") },
                                onClick = {
                                    showMenu = false
                                    showReportSheet = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Block User", color = PassColor) },
                                onClick = {
                                    showMenu = false
                                    showBlockDialog = true
                                }
                            )
                        }
                    }
                }
            }

            // Message List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.Spacing12, vertical = Dimens.Spacing8)
            ) {
                items(uiState.messages, key = { it.id }) { message ->
                    val isFromMe = message.senderId == uiState.currentUserId
                    val bubbleType = when (message.messageType) {
                        MessageType.TEXT -> MessageBubbleType.TEXT
                        MessageType.IMAGE -> MessageBubbleType.IMAGE
                        MessageType.SYSTEM -> MessageBubbleType.SYSTEM
                    }
                    val deliveryStatus = when (message.status) {
                        MessageStatus.SENDING -> MessageDeliveryStatus.SENDING
                        MessageStatus.SENT -> MessageDeliveryStatus.SENT
                        MessageStatus.DELIVERED -> MessageDeliveryStatus.DELIVERED
                        MessageStatus.READ -> MessageDeliveryStatus.READ
                        MessageStatus.FAILED -> MessageDeliveryStatus.FAILED
                    }

                    MessageBubble(
                        content = message.content,
                        timestampMillis = message.createdAtMillis,
                        isFromMe = isFromMe,
                        messageType = bubbleType,
                        mediaUrl = message.mediaUrl,
                        status = deliveryStatus
                    )
                }

                if (uiState.isPartnerTyping) {
                    item {
                        Row(
                            modifier = Modifier.padding(Dimens.Spacing8),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Avatar(
                                imageUrl = uiState.matchPhotoUrl,
                                name = uiState.matchName,
                                size = 28.dp
                            )
                            Spacer(modifier = Modifier.width(Dimens.Spacing8))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(Dimens.RadiusMedium))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = Dimens.Spacing12, vertical = Dimens.Spacing6)
                            ) {
                                Text(
                                    text = "•••",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = AuraRose
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Input Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.Spacing12, vertical = Dimens.Spacing8),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { photoPickerLauncher.launch("image/*") },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        if (uiState.isUploadingImage) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = AuraRose,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = "Send Photo",
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(Dimens.Spacing8))

                    OutlinedTextField(
                        value = uiState.inputText,
                        onValueChange = viewModel::onInputTextChange,
                        placeholder = { Text("Type a message...") },
                        maxLines = 4,
                        shape = RoundedCornerShape(Dimens.RadiusExtraLarge),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { viewModel.sendMessage() }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AuraRose,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(Dimens.Spacing8))

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .then(
                                if (uiState.inputText.isNotBlank()) Modifier.background(AuraPrimaryGradient)
                                else Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                            )
                            .clickable(enabled = uiState.inputText.isNotBlank()) {
                                viewModel.sendMessage()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Unmatch Confirmation Dialog
        if (showUnmatchDialog) {
            ConfirmationDialog(
                title = "Unmatch ${uiState.matchName}?",
                message = "You won't be able to message each other anymore, and this conversation will be removed.",
                confirmText = "Unmatch",
                isDestructive = true,
                onConfirm = {
                    showUnmatchDialog = false
                    viewModel.unmatch(uiState.conversationId)
                },
                onDismiss = { showUnmatchDialog = false }
            )
        }

        // Block Confirmation Dialog
        if (showBlockDialog) {
            ConfirmationDialog(
                title = "Block ${uiState.matchName}?",
                message = "They will not be able to see your profile, send you messages, or appear in your discovery stack.",
                confirmText = "Block",
                isDestructive = true,
                onConfirm = {
                    showBlockDialog = false
                    viewModel.blockUser(uiState.matchName)
                },
                onDismiss = { showBlockDialog = false }
            )
        }

        // Report Sheet
        if (showReportSheet) {
            ReportUserSheet(
                reportedUserName = uiState.matchName,
                onSubmitReport = { reason, details ->
                    viewModel.reportUser(uiState.matchName, reason, details)
                    showReportSheet = false
                },
                onDismiss = { showReportSheet = false }
            )
        }
    }
}
