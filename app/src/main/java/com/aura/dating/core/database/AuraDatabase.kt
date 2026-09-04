package com.aura.dating.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.aura.dating.core.database.converters.Converters
import com.aura.dating.core.database.dao.BlockedUserDao
import com.aura.dating.core.database.dao.ConversationDao
import com.aura.dating.core.database.dao.DiscoveryDao
import com.aura.dating.core.database.dao.MatchDao
import com.aura.dating.core.database.dao.MessageDao
import com.aura.dating.core.database.dao.ProfileDao
import com.aura.dating.core.database.entity.BlockedUserEntity
import com.aura.dating.core.database.entity.ConversationEntity
import com.aura.dating.core.database.entity.DiscoveryCandidateEntity
import com.aura.dating.core.database.entity.MatchEntity
import com.aura.dating.core.database.entity.MessageEntity
import com.aura.dating.core.database.entity.ProfileEntity

@Database(
    entities = [
        ProfileEntity::class,
        DiscoveryCandidateEntity::class,
        MatchEntity::class,
        ConversationEntity::class,
        MessageEntity::class,
        BlockedUserEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AuraDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun discoveryDao(): DiscoveryDao
    abstract fun matchDao(): MatchDao
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun blockedUserDao(): BlockedUserDao
}
