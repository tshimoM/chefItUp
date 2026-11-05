package com.chefitup.app.data.sync

import com.chefitup.app.data.firestore.FirestoreUserSync
import com.chefitup.app.data.local.preferences.SessionPreferences
import com.chefitup.app.data.remote.NetworkMonitor
import com.chefitup.app.data.storage.ProfilePhotoStorage
import com.chefitup.app.domain.repository.SyncRepository
import com.chefitup.app.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Watches connectivity and flushes Room + pending blob uploads as soon as
 * the device comes back online — so offline edits behave like online ones after sync.
 */
@Singleton
class OfflineSyncCoordinator @Inject constructor(
    private val networkMonitor: NetworkMonitor,
    private val syncRepository: SyncRepository,
    private val profilePhotoStorage: ProfilePhotoStorage,
    private val firestoreUserSync: FirestoreUserSync,
    private val sessionPreferences: SessionPreferences,
    private val preferencesRepository: UserPreferencesRepository
) {
    fun start(scope: CoroutineScope) {
        scope.launch {
            var wasOnline = networkMonitor.isOnline
            networkMonitor.connectivity
                .distinctUntilChanged()
                .collect { online ->
                    if (online && !wasOnline) {
                        flushPending()
                    }
                    wasOnline = online
                }
        }
        scope.launch {
            if (networkMonitor.isOnline) {
                flushPending()
            }
        }
    }

    private suspend fun flushPending() {
        runCatching { syncRepository.syncNow() }
        runCatching {
            val url = profilePhotoStorage.flushPendingUpload() ?: return@runCatching
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
    }
}
