package com.chefitup.app.data.firestore

import com.chefitup.app.data.local.JsonListConverter
import com.chefitup.app.data.local.dao.FavouriteDao
import com.chefitup.app.data.local.dao.GamificationDao
import com.chefitup.app.data.local.entity.GamificationEntity
import com.chefitup.app.data.local.preferences.SessionPreferences
import com.chefitup.app.data.mapper.RecipeMapper
import com.chefitup.app.data.storage.ProfilePhotoStorage
import com.chefitup.app.domain.gamification.GamificationRules
import com.chefitup.app.domain.model.ThemeMode
import com.chefitup.app.domain.repository.UserPreferencesRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Pulls NoSQL (Firestore) + blob (Storage) data into local Room/DataStore after login.
 */
@Singleton
class CloudUserRestore @Inject constructor(
    private val firestoreUserSync: FirestoreUserSync,
    private val preferencesRepository: UserPreferencesRepository,
    private val sessionPreferences: SessionPreferences,
    private val gamificationDao: GamificationDao,
    private val favouriteDao: FavouriteDao,
    private val profilePhotoStorage: ProfilePhotoStorage,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseMessaging: FirebaseMessaging
) {
    suspend fun restoreAfterSignIn() {
        if (firebaseAuth.currentUser == null) return

        runCatching { restoreProfile() }
        runCatching { restoreGamification() }
        runCatching { restoreFavourites() }
        runCatching { registerFcmToken() }
        runCatching { ensurePhotoUrlInFirestore() }
    }

    private suspend fun restoreProfile() {
        val cloud = firestoreUserSync.pullProfile() ?: return
        sessionPreferences.saveUserProfile(
            name = cloud.name,
            surname = cloud.surname,
            username = cloud.username,
            phoneNumber = cloud.phone,
            email = cloud.email.ifBlank { firebaseAuth.currentUser?.email.orEmpty() }
        )
        val theme = runCatching { ThemeMode.valueOf(cloud.theme) }.getOrDefault(ThemeMode.SYSTEM)
        val current = preferencesRepository.getSnapshot()
        preferencesRepository.applyFromCloud(
            current.copy(
                diet = cloud.diet.ifBlank { current.diet },
                allergies = cloud.allergies.ifEmpty { current.allergies },
                languageCode = cloud.languageCode.ifBlank { current.languageCode },
                themeMode = theme
            )
        )
    }

    private suspend fun restoreGamification() {
        val cloud = firestoreUserSync.pullGamification() ?: return
        val local = gamificationDao.get()
        val localXp = local?.xp ?: 0
        if (cloud.xp < localXp) return
        val state = GamificationRules.buildState(
            xp = cloud.xp,
            recipesCompleted = cloud.recipesCompleted,
            earnedBadgeIds = cloud.unlockedBadges
        )
        gamificationDao.upsert(
            GamificationEntity(
                id = 1,
                xp = state.xp,
                recipesCompleted = state.recipesCompleted,
                earnedBadgesJson = JsonListConverter.stringListToJson(state.earnedBadgeIds),
                viewedRecipeIdsJson = local?.viewedRecipeIdsJson ?: "[]",
                hasUsedMealPlan = local?.hasUsedMealPlan ?: false,
                updatedAtEpochMs = System.currentTimeMillis()
            )
        )
    }

    private suspend fun restoreFavourites() {
        firestoreUserSync.pullFavourites().forEach { summary ->
            favouriteDao.upsert(
                RecipeMapper.summaryToFavourite(summary, System.currentTimeMillis())
            )
        }
    }

    private suspend fun ensurePhotoUrlInFirestore() {
        val existing = firestoreUserSync.pullProfile()?.photoUrl
        if (!existing.isNullOrBlank()) return

        val url = firebaseAuth.currentUser?.photoUrl?.toString()
            ?: profilePhotoStorage.downloadProfilePhotoUrl()
            ?: return
        if (url.isBlank()) return

        val profile = sessionPreferences.userProfile.first()
        val prefs = preferencesRepository.getSnapshot()
        firestoreUserSync.pushProfile(
            name = profile.name,
            surname = profile.surname,
            username = profile.username,
            phone = profile.phoneNumber,
            diet = prefs.diet,
            allergies = prefs.allergies + prefs.intolerances,
            languageCode = prefs.languageCode,
            theme = prefs.themeMode.name,
            photoUrl = url
        )
    }

    private suspend fun registerFcmToken() {
        val token = firebaseMessaging.token.await()
        firestoreUserSync.saveFcmToken(token)
    }
}
