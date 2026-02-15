package com.cirin0.worktimetracker.features.message.data.repository

import com.cirin0.worktimetracker.core.network.ApiResponse
import com.cirin0.worktimetracker.core.network.apiCall
import com.cirin0.worktimetracker.core.pusher.PusherService
import com.cirin0.worktimetracker.features.message.data.api.MessageApi
import com.cirin0.worktimetracker.features.message.data.model.Message
import com.cirin0.worktimetracker.features.message.data.model.SendMessageRequest
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRepository @Inject constructor(
    private val messageApi: MessageApi,
    private val pusherService: PusherService,
    private val gson: Gson
) {
    suspend fun getMessages(receiverId: Int): ApiResponse<List<Message>> = apiCall {
        messageApi.getMessages(receiverId)
    }

    suspend fun sendMessage(message: String, receiverId: Int): ApiResponse<Message> = apiCall {
        messageApi.sendMessage(SendMessageRequest(message, receiverId))
    }

    fun subscribeToMessages(userId: Int): Flow<Message> {
        return pusherService.subscribeToChannel(
            channelName = "private-chat.$userId",
            eventName = "new-message"
        ) { data ->
            val event = gson.fromJson(
                data,
                com.cirin0.worktimetracker.features.message.data.model.NewMessageEvent::class.java
            )

            val senderId = event.senderId ?: event.user.id
            val receiverId = event.receiverId ?: userId
            val messageId = event.id ?: System.currentTimeMillis().toInt()

            Message(
                id = messageId,
                senderId = senderId,
                receiverId = receiverId,
                message = event.message,
                createdAt = java.text.SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ss",
                    java.util.Locale.getDefault()
                )
                    .format(java.util.Date())
            )
        }
    }

    fun connectPusher(authToken: String?) {
        pusherService.connect(authToken)
    }

    fun disconnectPusher() {
        pusherService.disconnect()
    }

    fun unsubscribeFromMessages(userId: Int) {
        pusherService.unsubscribeFromChannel("private-chat.$userId")
    }

    fun isPusherConnected(): Boolean {
        return pusherService.isConnected()
    }
}



