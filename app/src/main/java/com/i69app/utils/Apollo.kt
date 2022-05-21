package com.i69app.utils

import android.content.Context
import android.util.Log
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.http.HttpMethod
import com.apollographql.apollo3.api.http.HttpRequest
import com.apollographql.apollo3.api.http.HttpResponse
import com.apollographql.apollo3.network.http.DefaultHttpEngine
import com.apollographql.apollo3.network.http.HttpInterceptor
import com.apollographql.apollo3.network.http.HttpInterceptorChain
import com.apollographql.apollo3.network.http.LoggingInterceptor
import com.apollographql.apollo3.network.okHttpClient
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.internal.concurrent.TaskRunner.Companion.logger
import okio.*
import timber.log.Timber


private var instance: ApolloClient? = null
private var instanceForSubsciption: ApolloClient? = null

fun apolloClientSubscription(context: Context, token: String): ApolloClient {

    if (instanceForSubsciption != null) {
        return instanceForSubsciption!!
    }
    Timber.d("authh $token")
    val okHttpClient =
        OkHttpClient.Builder().addInterceptor(AuthorizationInterceptor(context, token))
            //.addNetworkInterceptor(NetworkInterceptor())
            .build()

    //val logging = LoggingInterceptor()
    instanceForSubsciption = ApolloClient.Builder()
        //.addHttpInterceptor(logging)
        .httpServerUrl("https://admin.chatadmin-mod.click")
        .webSocketServerUrl("wss://admin.chatadmin-mod.click/ws/graphql")
        .wsProtocol(SubscriptionWsProtocol.Factory(connectionPayload = { mapOf("content-type" to "application/json") }))
        //.subscriptionNetworkTransport(WebSocketNetworkTransport.Builder().serverUrl("ws://95.216.208.1:8000/ws/graphql").build())
        .okHttpClient(okHttpClient)
        .build()

    return instanceForSubsciption!!
}

fun apolloClient(context: Context, token: String): ApolloClient {
    if (instance != null) {
        return instance!!
    }

    Timber.d("authh $token")
    val okHttpClient =
        OkHttpClient.Builder().addInterceptor(AuthorizationInterceptor(context, token))
            //.addNetworkInterceptor(NetworkInterceptor())
            .build()

    //val logging = LoggingInterceptor()
    instance = ApolloClient.Builder()
        .serverUrl("https://admin.chatadmin-mod.click")
        //.addHttpInterceptor(AuthorizationHttpInterceptor(token))
        //.addHttpInterceptor(logging)
        //.addHttpInterceptor(LoggingInterceptor())
        .httpMethod(HttpMethod.Post)
        .okHttpClient(okHttpClient)
        .build()



    return instance!!
}

class NetworkInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val t1 = System.nanoTime()
        logger.info(
            String.format(
                "Network_Interceptor request %s on %s%n%s",
                request.url, chain.connection(), request.headers
            )
        )

        val requestBuffer = Buffer();
        request.body?.writeTo(requestBuffer);
        logger.info("Network_Interceptor OkHttp RequestBody>>> ${requestBuffer.readUtf8()}");

        val response = chain.proceed(request);

        val t2 = System.nanoTime()
        logger.info(
            String.format(
                "Network_Interceptor Received response for %s in %.1fms%n%s",
                response.request.url, (t2 - t1) / 1e6, response.headers
            )
        );

        /*val contentType: MediaType? = response.body?.contentType();
        val buffer: BufferedSource = Okio.buffer(GzipSource(response.body?.source()));
        val content: String = buffer.readUtf8();
        logger.info("OkHttp $content");*/

        return response;
    }

}

private class AuthorizationInterceptor(val context: Context, val token: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = if (chain.request().url.toString().contains("/ws/graphql?token=")) {
            chain.request()
        } else
            chain.request().newBuilder()
                .addHeader("Authorization", "Token $token")
                .build()
        return chain.proceed(request)
    }
}

private class AuthorizationHttpInterceptor(val token: String) : HttpInterceptor {
    override suspend fun intercept(
        request: HttpRequest,
        chain: HttpInterceptorChain
    ): HttpResponse {
        return chain.proceed(
            request.newBuilder().addHeader("Authorization", "Bearer $token").build()
        )
    }
}

internal class GzipRequestInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest: Request = chain.request()
        if (originalRequest.body == null || originalRequest.header("Content-Encoding") != null) {
            return chain.proceed(originalRequest)
        }
        val compressedRequest: Request = originalRequest.newBuilder()
            .header("Content-Encoding", "gzip")
            .method(originalRequest.method, gzip(originalRequest.body!!))
            .build()
        return chain.proceed(compressedRequest)
    }

    private fun gzip(body: RequestBody): RequestBody {
        return object : RequestBody() {
            override fun contentType(): MediaType? {
                return body.contentType()
            }

            override fun contentLength(): Long {
                return -1 // We don't know the compressed length in advance!
            }

            @Throws(IOException::class)
            override fun writeTo(sink: BufferedSink) {
                val gzipSink = GzipSink(sink).buffer()
                body.writeTo(gzipSink)
                logger.info(
                    "Network_Interceptor ${
                        String(
                            gzipSink.buffer.readByteArray(),
                            Charsets.UTF_16
                        )
                    }"
                )
                gzipSink.close()
            }
        }
    }
}
