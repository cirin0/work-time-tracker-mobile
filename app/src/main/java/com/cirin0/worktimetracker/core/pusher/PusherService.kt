package com.cirin0.worktimetracker.core.pusher

import com.cirin0.worktimetracker.core.utils.Constants
import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import com.pusher.client.channel.Channel
import com.pusher.client.channel.PrivateChannelEventListener
import com.pusher.client.channel.PusherEvent
import com.pusher.client.connection.ConnectionEventListener
import com.pusher.client.connection.ConnectionState
import com.pusher.client.connection.ConnectionStateChange
import com.pusher.client.util.HttpChannelAuthorizer
import jakarta.inject.Named
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PusherService @Inject constructor(
    @param:Named("active_domain") private val domain: String,
    @param:Named("no_auth_client") private val httpClient: OkHttpClient
) {
    private var pusher: Pusher? = null
    private val channels = mutableMapOf<String, Channel>()

    fun connect(authToken: String?) {
        if (pusher != null) {
            return
        }

        val authUrl = "$domain/broadcasting/auth"

        val authorizer = object : HttpChannelAuthorizer(authUrl) {
            override fun authorize(
                channelName: String?,
                socketId: String?
            ): String? {
                try {
                    val body = FormBody.Builder()
                        .add("socket_id", socketId ?: "")
                        .add("channel_name", channelName ?: "")
                        .build()

                    val request = Request.Builder()
                        .url(authUrl)
                        .post(body)
                        .apply {
                            authToken?.let { token ->
                                addHeader("Authorization", "Bearer $token")
                            }
                        }
                        .build()

                    val response = httpClient.newCall(request).execute()
                    val responseBody = response.body.string()

                    if (!response.isSuccessful) {
                        return null
                    }

                    return responseBody
                } catch (e: Exception) {
                    return null
                }
            }
        }

        val options = PusherOptions().apply {
            setHost("realtime-pusher.ably.io")
            setWsPort(443)
            setWssPort(443)
            isUseTLS = true
            channelAuthorizer = authorizer
        }

        pusher = Pusher(Constants.Ably.PUBLIC_KEY, options).apply {
            connect(object : ConnectionEventListener {
                override fun onConnectionStateChange(change: ConnectionStateChange) {
                    // Connection state changed
                }

                override fun onError(message: String, code: String?, e: Exception?) {
                    // Connection error occurred
                }
            }, ConnectionState.ALL)
        }
    }

    fun disconnect() {
        channels.clear()
        pusher?.disconnect()
        pusher = null
    }

    fun unsubscribeFromChannel(channelName: String) {
        pusher?.unsubscribe(channelName)
        channels.remove(channelName)
    }

    fun <T> subscribeToChannel(
        channelName: String,
        eventName: String,
        parser: (String) -> T
    ): Flow<T> = callbackFlow {
        val isPrivate = channelName.startsWith("private-")

        val existingChannel = channels[channelName]
        val channel = if (existingChannel != null) {
            existingChannel
        } else {
            val newChannel = if (isPrivate) {
                pusher?.subscribePrivate(channelName)
            } else {
                pusher?.subscribe(channelName)
            } ?: run {
                close(IllegalStateException("Pusher not connected"))
                return@callbackFlow
            }
            channels[channelName] = newChannel
            newChannel
        }

        if (isPrivate) {
            val listener = object : PrivateChannelEventListener {
                override fun onEvent(event: PusherEvent?) {
                    try {
                        if (event?.eventName != eventName) {
                            return
                        }

                        event.data?.let { data ->
                            val parsedData = parser(data)
                            trySend(parsedData)
                        }
                    } catch (e: Exception) {
                        // Parsing error
                    }
                }

                override fun onAuthenticationFailure(message: String?, e: Exception?) {
                    close(Exception("Authentication failed"))
                }

                override fun onSubscriptionSucceeded(channelName: String?) {
                }
            }
            channel.bind(eventName, listener)
        } else {
            channel.bind(eventName) { event ->
                try {
                    val data = parser(event.data)
                    trySend(data)
                } catch (e: Exception) {
                    // Parsing error
                }
            }
        }

        awaitClose {
            pusher?.unsubscribe(channelName)
            channels.remove(channelName)
        }
    }

    fun isConnected(): Boolean {
        return pusher?.connection?.state == ConnectionState.CONNECTED
    }
}




