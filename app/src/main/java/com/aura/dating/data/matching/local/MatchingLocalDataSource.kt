package com.aura.dating.data.matching.local

import com.aura.dating.core.database.dao.MatchDao
import com.aura.dating.core.database.entity.MatchEntity
import com.aura.dating.domain.matching.model.Match
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface MatchingLocalDataSource {
    val matchesFlow: Flow<List<Match>>
    suspend fun saveMatches(matches: List<Match>)
    suspend fun saveMatch(match: Match)
    suspend fun deactivateMatch(matchId: String)
    suspend fun clear()
}

@Singleton
class RoomMatchingLocalDataSource @Inject constructor(
    private val matchDao: MatchDao
) : MatchingLocalDataSource {

    override val matchesFlow: Flow<List<Match>> = matchDao.getMatchesFlow().map { list ->
        list.map { entity ->
            Match(
                id = entity.id,
                matchedUserId = entity.matchedUserId,
                matchedUserName = entity.matchedUserName,
                matchedUserAge = entity.matchedUserAge,
                matchedUserPhotoUrl = entity.matchedUserPhotoUrl,
                matchedUserDistanceKm = entity.matchedUserDistanceKm,
                matchedAtMillis = entity.matchedAtMillis,
                isActive = entity.isActive
            )
        }
    }

    override suspend fun saveMatches(matches: List<Match>) {
        val entities = matches.map { m ->
            MatchEntity(
                id = m.id,
                matchedUserId = m.matchedUserId,
                matchedUserName = m.matchedUserName,
                matchedUserAge = m.matchedUserAge,
                matchedUserPhotoUrl = m.matchedUserPhotoUrl,
                matchedUserDistanceKm = m.matchedUserDistanceKm,
                matchedAtMillis = m.matchedAtMillis,
                isActive = m.isActive
            )
        }
        matchDao.insertMatches(entities)
    }

    override suspend fun saveMatch(match: Match) {
        matchDao.insertMatch(
            MatchEntity(
                id = match.id,
                matchedUserId = match.matchedUserId,
                matchedUserName = match.matchedUserName,
                matchedUserAge = match.matchedUserAge,
                matchedUserPhotoUrl = match.matchedUserPhotoUrl,
                matchedUserDistanceKm = match.matchedUserDistanceKm,
                matchedAtMillis = match.matchedAtMillis,
                isActive = match.isActive
            )
        )
    }

    override suspend fun deactivateMatch(matchId: String) {
        matchDao.deactivateMatch(matchId)
    }

    override suspend fun clear() {
        matchDao.clearMatches()
    }
}
