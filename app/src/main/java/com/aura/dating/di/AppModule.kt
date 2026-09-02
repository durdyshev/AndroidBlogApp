package com.aura.dating.di

import android.content.Context
import androidx.room.Room
import com.aura.dating.core.common.dispatcher.CoroutineDispatchersProvider
import com.aura.dating.core.common.dispatcher.DefaultCoroutineDispatchers
import com.aura.dating.core.database.AuraDatabase
import com.aura.dating.core.database.dao.BlockedUserDao
import com.aura.dating.core.database.dao.ConversationDao
import com.aura.dating.core.database.dao.DiscoveryDao
import com.aura.dating.core.database.dao.MatchDao
import com.aura.dating.core.database.dao.MessageDao
import com.aura.dating.core.database.dao.ProfileDao
import com.aura.dating.core.location.FusedLocationProvider
import com.aura.dating.core.location.LocationProvider
import com.aura.dating.core.network.AndroidNetworkMonitor
import com.aura.dating.core.network.NetworkMonitor
import com.aura.dating.core.security.DataStoreTokenStorage
import com.aura.dating.core.security.TokenStorage
import com.aura.dating.data.auth.local.AuthLocalDataSource
import com.aura.dating.data.auth.local.DataStoreAuthLocalDataSource
import com.aura.dating.data.auth.remote.AuthRemoteDataSource
import com.aura.dating.data.auth.remote.SupabaseAuthRemoteDataSource
import com.aura.dating.data.auth.repository.AuthRepositoryImpl
import com.aura.dating.data.chat.local.ChatLocalDataSource
import com.aura.dating.data.chat.local.RoomChatLocalDataSource
import com.aura.dating.data.chat.remote.ChatRemoteDataSource
import com.aura.dating.data.chat.remote.SupabaseChatRemoteDataSource
import com.aura.dating.data.chat.repository.ChatRepositoryImpl
import com.aura.dating.data.discovery.local.DiscoveryLocalDataSource
import com.aura.dating.data.discovery.local.RoomDiscoveryLocalDataSource
import com.aura.dating.data.discovery.remote.DiscoveryRemoteDataSource
import com.aura.dating.data.discovery.remote.SupabaseDiscoveryRemoteDataSource
import com.aura.dating.data.discovery.repository.DiscoveryRepositoryImpl
import com.aura.dating.data.matching.local.MatchingLocalDataSource
import com.aura.dating.data.matching.local.RoomMatchingLocalDataSource
import com.aura.dating.data.matching.remote.MatchingRemoteDataSource
import com.aura.dating.data.matching.remote.SupabaseMatchingRemoteDataSource
import com.aura.dating.data.matching.repository.MatchingRepositoryImpl
import com.aura.dating.data.moderation.local.ModerationLocalDataSource
import com.aura.dating.data.moderation.local.RoomModerationLocalDataSource
import com.aura.dating.data.moderation.remote.ModerationRemoteDataSource
import com.aura.dating.data.moderation.remote.SupabaseModerationRemoteDataSource
import com.aura.dating.data.moderation.repository.ModerationRepositoryImpl
import com.aura.dating.data.notifications.remote.NotificationRemoteDataSource
import com.aura.dating.data.notifications.remote.SupabaseNotificationRemoteDataSource
import com.aura.dating.data.notifications.repository.NotificationRepositoryImpl
import com.aura.dating.data.profile.local.ProfileLocalDataSource
import com.aura.dating.data.profile.local.RoomProfileLocalDataSource
import com.aura.dating.data.location.remote.LocationRemoteDataSource
import com.aura.dating.data.location.remote.SupabaseLocationRemoteDataSource
import com.aura.dating.data.location.repository.LocationRepositoryImpl
import com.aura.dating.data.profile.remote.ProfileRemoteDataSource
import com.aura.dating.data.profile.remote.SupabaseProfileRemoteDataSource
import com.aura.dating.data.profile.repository.ProfileRepositoryImpl
import com.aura.dating.domain.auth.repository.AuthRepository
import com.aura.dating.domain.chat.repository.ChatRepository
import com.aura.dating.domain.discovery.repository.DiscoveryRepository
import com.aura.dating.domain.location.repository.LocationRepository
import com.aura.dating.domain.matching.repository.MatchingRepository
import com.aura.dating.domain.moderation.repository.ModerationRepository
import com.aura.dating.domain.notifications.repository.NotificationRepository
import com.aura.dating.domain.profile.repository.ProfileRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindDispatchers(impl: DefaultCoroutineDispatchers): CoroutineDispatchersProvider

    @Binds
    @Singleton
    abstract fun bindTokenStorage(impl: DataStoreTokenStorage): TokenStorage

    @Binds
    @Singleton
    abstract fun bindNetworkMonitor(impl: AndroidNetworkMonitor): NetworkMonitor

    @Binds
    @Singleton
    abstract fun bindLocationProvider(impl: FusedLocationProvider): LocationProvider

    // Auth
    @Binds
    @Singleton
    abstract fun bindAuthRemote(impl: SupabaseAuthRemoteDataSource): AuthRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindAuthLocal(impl: DataStoreAuthLocalDataSource): AuthLocalDataSource

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    // Profile
    @Binds
    @Singleton
    abstract fun bindProfileRemote(impl: SupabaseProfileRemoteDataSource): ProfileRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindProfileLocal(impl: RoomProfileLocalDataSource): ProfileLocalDataSource

    @Binds
    @Singleton
    abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository

    // Discovery
    @Binds
    @Singleton
    abstract fun bindDiscoveryRemote(impl: SupabaseDiscoveryRemoteDataSource): DiscoveryRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindDiscoveryLocal(impl: RoomDiscoveryLocalDataSource): DiscoveryLocalDataSource

    @Binds
    @Singleton
    abstract fun bindDiscoveryRepository(impl: DiscoveryRepositoryImpl): DiscoveryRepository

    // Location
    @Binds
    @Singleton
    abstract fun bindLocationRemote(impl: SupabaseLocationRemoteDataSource): LocationRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindLocationRepository(impl: LocationRepositoryImpl): LocationRepository

    // Matching
    @Binds
    @Singleton
    abstract fun bindMatchingRemote(impl: SupabaseMatchingRemoteDataSource): MatchingRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindMatchingLocal(impl: RoomMatchingLocalDataSource): MatchingLocalDataSource

    @Binds
    @Singleton
    abstract fun bindMatchingRepository(impl: MatchingRepositoryImpl): MatchingRepository

    // Chat
    @Binds
    @Singleton
    abstract fun bindChatRemote(impl: SupabaseChatRemoteDataSource): ChatRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindChatLocal(impl: RoomChatLocalDataSource): ChatLocalDataSource

    @Binds
    @Singleton
    abstract fun bindChatRepository(impl: ChatRepositoryImpl): ChatRepository

    // Notifications
    @Binds
    @Singleton
    abstract fun bindNotificationRemote(impl: SupabaseNotificationRemoteDataSource): NotificationRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(impl: NotificationRepositoryImpl): NotificationRepository

    // Moderation
    @Binds
    @Singleton
    abstract fun bindModerationRemote(impl: SupabaseModerationRemoteDataSource): ModerationRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindModerationLocal(impl: RoomModerationLocalDataSource): ModerationLocalDataSource

    @Binds
    @Singleton
    abstract fun bindModerationRepository(impl: ModerationRepositoryImpl): ModerationRepository
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AuraDatabase {
        return Room.databaseBuilder(
            context,
            AuraDatabase::class.java,
            "aura_database.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideProfileDao(db: AuraDatabase): ProfileDao = db.profileDao()

    @Provides
    fun provideDiscoveryDao(db: AuraDatabase): DiscoveryDao = db.discoveryDao()

    @Provides
    fun provideMatchDao(db: AuraDatabase): MatchDao = db.matchDao()

    @Provides
    fun provideConversationDao(db: AuraDatabase): ConversationDao = db.conversationDao()

    @Provides
    fun provideMessageDao(db: AuraDatabase): MessageDao = db.messageDao()

    @Provides
    fun provideBlockedUserDao(db: AuraDatabase): BlockedUserDao = db.blockedUserDao()
}
