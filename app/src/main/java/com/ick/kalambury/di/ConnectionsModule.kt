package com.ick.kalambury.di

import android.content.Context
import com.ick.kalambury.service.nearbyconnections.RxClientNearbyConnections
import com.ick.kalambury.service.nearbyconnections.RxHostNearbyConnections
import com.ick.kalambury.service.nearbyconnections.RxNearbyConnections
import com.ick.kalambury.service.websocket.RxWebSocket
import com.ick.kalambury.service.websocket.RxWebSocketImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient

@InstallIn(SingletonComponent::class)
@Module
class ConnectionsModule {

    @Provides
    fun provideRxHostNearbyConnections(@ApplicationContext context: Context): RxHostNearbyConnections {
        return RxNearbyConnections(context)
    }

    @Provides
    fun provideRxClientNearbyConnections(@ApplicationContext context: Context): RxClientNearbyConnections {
        return RxNearbyConnections(context)
    }

    @Provides
    fun provideRxWebSocketConnection(okHttpClient: OkHttpClient): RxWebSocket {
        return RxWebSocketImpl(okHttpClient)
    }

}