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
    @param:Named("active_domain") private val domain: String
) {
    private var pusher: Pusher? = null
    private val channels = mutableMapOf<String, Channel>()

    fun connect(authToken: String?) {
        if (pusher != null) {
            return
        }
        
        val authUrl = "$domain/broadcasting/auth"
        println("🔐 Broadcasting auth URL: $authUrl")
        
        val authorizer = object : HttpChannelAuthorizer(authUrl) {
            private val httpClient = OkHttpClient()
            
            override fun authorize(
                channelName: String?,
                socketId: String?
            ): String? {
                try {
                    println("📡 Authorizing channel: $channelName, socketId: $socketId")
                    
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
                    val responseBody = response.body?.string()
                    
                    println("📥 Auth response code: ${response.code}")
                    println("📥 Auth response body: $responseBody")
                    
                    if (!response.isSuccessful) {
                        println("❌ Auth failed with code ${response.code}")
                        return null
                    }
                    
                    return responseBody
                } catch (e: Exception) {
                    println("❌ Auth exception: ${e.message}")
                    e.printStackTrace()
                    return null
                }
            }
        }
        
        authToken?.let { token ->
            println("🔑 Auth token set for broadcasting: ${token.take(20)}...")
        } ?: run {
            println("⚠️ No auth token provided for broadcasting")
        }

        val options = PusherOptions().apply {
            setCluster(Constants.Reverb.CLUSTER)
            setHost(Constants.Reverb.HOST)
            setWsPort(Constants.Reverb.PORT)
            setWssPort(Constants.Reverb.PORT)
            isUseTLS = false
            channelAuthorizer = authorizer
        }

        pusher = Pusher(Constants.Reverb.APP_KEY, options).apply {
            connect(object : ConnectionEventListener {
                override fun onConnectionStateChange(change: ConnectionStateChange) {
                    println("🔄 Reverb state: ${change.previousState} -> ${change.currentState}")
                    if (change.currentState == ConnectionState.CONNECTED) {
                        println("✅ Pusher CONNECTED! Ready to receive events")
                    }
                }

                override fun onError(message: String, code: String?, e: Exception?) {
                    println("❌ Reverb error: $message, code: $code")
                    e?.printStackTrace()
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
                        e.printStackTrace()
                    }
                }

                override fun onAuthenticationFailure(message: String?, e: Exception?) {
                    e?.printStackTrace()
                    close(Exception("Authentication failed: $message", e))
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
                    e.printStackTrace()
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




