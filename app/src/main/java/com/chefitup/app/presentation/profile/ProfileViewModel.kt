package com.chefitup.app.presentation.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chefitup.app.R
import com.chefitup.app.data.auth.GoogleAuthClient
import com.chefitup.app.data.firestore.FirestoreUserSync
import com.chefitup.app.data.local.preferences.SessionPreferences
import com.chefitup.app.data.remote.NetworkMonitor
import com.chefitup.app.data.storage.ProfilePhotoStorage
import com.chefitup.app.domain.model.BadgeDefinition
import com.chefitup.app.domain.model.UserGamification
import com.chefitup.app.domain.repository.AuthRepository
import com.chefitup.app.domain.repository.GamificationRepository
import com.chefitup.app.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val gamification: UserGamification = UserGamification(),
    val badges: List<BadgeDefinition> = emptyList(),
    val isUploadingPhoto: Boolean = false,
    val photoMessageRes: Int? = null
)

sealed class ProfileEvent {
    data class ShowMessage(val messageRes: Int) : ProfileEvent()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository,
    private val gamificationRepository: GamificationRepository,
    private val googleAuthClient: GoogleAuthClient,
    private val profilePhotoStorage: ProfilePhotoStorage,
    private val firestoreUserSync: FirestoreUserSync,
    private val sessionPreferences: SessionPreferences,
    private val preferencesRepository: UserPreferencesRepository,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ProfileUiState(
            displayName = authRepository.currentUser?.displayName
                ?: authRepository.currentUser?.email?.substringBefore('@').orEmpty(),
            email = authRepository.currentUser?.email.orEmpty(),
            photoUrl = authRepository.currentUser?.photoUrl,
            badges = gamificationRepository.allBadges()
        )
    )
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ProfileEvent>()
    val events: SharedFlow<ProfileEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            gamificationRepository.observeGamification().collect { g ->
                _uiState.update { it.copy(gamification = g) }
            }
        }
        viewModelScope.launch {
            authRepository.authState.collect { user ->
                user?.let {
                    _uiState.update { state ->
                        state.copy(
                            displayName = it.displayName
                                ?: it.email.substringBefore('@'),
                            email = it.email,
                            photoUrl = it.photoUrl ?: state.photoUrl
                        )
                    }
                }
            }
        }
        viewModelScope.launch {
            runCatching {
                val cloudPhoto = firestoreUserSync.pullProfile()?.photoUrl
                val storagePhoto = profilePhotoStorage.downloadProfilePhotoUrl()
                val url = cloudPhoto ?: storagePhoto
                if (!url.isNullOrBlank()) {
                    _uiState.update { it.copy(photoUrl = url) }
                }
            }
        }
    }

    fun uploadProfilePhoto(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingPhoto = true, photoMessageRes = null) }
            val result = runCatching {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: error("Unable to read image")
                require(bytes.size <= 5 * 1024 * 1024) { "Image too large" }
                if (!networkMonitor.isOnline) {
                    val localUri = profilePhotoStorage.queueOfflinePhoto(bytes)
                    return@runCatching localUri to true
                }
                val downloadUrl = profilePhotoStorage.uploadProfilePhoto(bytes)
                pushPhotoToFirestore(downloadUrl)
                downloadUrl to false
            }
            result.onSuccess { (url, queuedOffline) ->
                val message = if (queuedOffline) {
                    R.string.profile_photo_offline_queued
                } else {
                    R.string.profile_photo_success
                }
                _uiState.update {
                    it.copy(
                        isUploadingPhoto = false,
                        photoUrl = url,
                        photoMessageRes = message
                    )
                }
                _events.emit(ProfileEvent.ShowMessage(message))
            }.onFailure {
                _uiState.update {
                    it.copy(
                        isUploadingPhoto = false,
                        photoMessageRes = R.string.profile_photo_error
                    )
                }
                _events.emit(ProfileEvent.ShowMessage(R.string.profile_photo_error))
            }
        }
    }

    private suspend fun pushPhotoToFirestore(downloadUrl: String) {
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
            photoUrl = downloadUrl
        )
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            googleAuthClient.signOut()
            authRepository.logout()
            onLoggedOut()
        }
    }
}
