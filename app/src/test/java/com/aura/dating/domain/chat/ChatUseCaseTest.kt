package com.aura.dating.domain.chat

import com.aura.dating.core.common.result.AppError
import com.aura.dating.core.common.result.Result
import com.aura.dating.domain.chat.model.Message
import com.aura.dating.domain.chat.model.MessageStatus
import com.aura.dating.domain.chat.model.MessageType
import com.aura.dating.domain.chat.repository.ChatRepository
import com.aura.dating.domain.chat.usecase.MarkMessagesAsReadUseCase
import com.aura.dating.domain.chat.usecase.SendMessageUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ChatUseCaseTest {

    private val chatRepository: ChatRepository = mockk(relaxed = true)

    private lateinit var sendMessageUseCase: SendMessageUseCase
    private lateinit var markMessagesAsReadUseCase: MarkMessagesAsReadUseCase

    @Before
    fun setUp() {
        sendMessageUseCase = SendMessageUseCase(chatRepository)
        markMessagesAsReadUseCase = MarkMessagesAsReadUseCase(chatRepository)
    }

    @Test
    fun `send message with valid text returns success and sends to repository`() = runTest {
        // Given
        val conversationId = "conv-1"
        val content = "Hey! Nice to match with you :)"
        val expectedMessage = Message(
            id = "msg-1",
            conversationId = conversationId,
            senderId = "me",
            content = content,
            messageType = MessageType.TEXT,
            status = MessageStatus.SENT
        )

        coEvery { chatRepository.sendMessage(conversationId, content) } returns Result.Success(expectedMessage)

        // When
        val result = sendMessageUseCase(conversationId, content)

        // Then
        assertTrue(result is Result.Success)
        assertEquals("msg-1", (result as Result.Success).data.id)
        coVerify(exactly = 1) { chatRepository.sendMessage(conversationId, content) }
    }

    @Test
    fun `send message with blank text returns ValidationError without repository call`() = runTest {
        // When
        val result = sendMessageUseCase("conv-1", "   ")

        // Then
        assertTrue(result is Result.Error)
        val error = (result as Result.Error).error
        assertTrue(error is AppError.ValidationError)
        coVerify(exactly = 0) { chatRepository.sendMessage(any(), any()) }
    }

    @Test
    fun `mark messages as read triggers repository call`() = runTest {
        // Given
        coEvery { chatRepository.markMessagesAsRead("conv-1") } returns Result.Success(Unit)

        // When
        val result = markMessagesAsReadUseCase("conv-1")

        // Then
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { chatRepository.markMessagesAsRead("conv-1") }
    }
}
