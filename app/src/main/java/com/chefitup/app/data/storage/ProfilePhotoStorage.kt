package com.chefitup.app.data.storage

import android.content.Context
import android.net.Uri
import com.chefitup.app.data.local.preferences.SessionPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase Storage (blob) uploads for user profile photos.
 * Offline: bytes are saved locally and uploaded automatically when connectivity returns.
 */
@Singleton
class ProfilePhotoStorage @Inject constructor(
    @ApplicationContext private val context: Context,
    private val storage: FirebaseStorage,
    private val firebaseAuth: FirebaseAuth,
    private val sessionPreferences: SessionPreferences
) {
    suspend fun uploadProfilePhoto(imageBytes: ByteArray): String {
        val user = firebaseAuth.currentUser
            ?: error("Not signed in")
        val ref = storage.reference.child("users/${user.uid}/profile.jpg")
        ref.putBytes(imageBytes).await()
        val downloadUrl = ref.downloadUrl.await().toString()
        user.updateProfile(
            UserProfileChangeRequest.Builder()
                .setPhotoUri(Uri.parse(downloadUrl))
                .build()
        ).await()
        clearPendingLocalPhoto()
        return downloadUrl
    }

    /**
     * Saves the image under app files so the UI can show it immediately offline.
     * @return file:// URI string for Coil.
     */
    suspend fun queueOfflinePhoto(imageBytes: ByteArray): String {
        val file = pendingFile()
        file.parentFile?.mkdirs()
        file.writeBytes(imageBytes)
        val uri = Uri.fromFile(file).toString()
        sessionPreferences.setPendingProfilePhotoPath(file.absolutePath)
        return uri
    }

    suspend fun flushPendingUpload(): String? {
        val path = sessionPreferences.pendingProfilePhotoPath.first() ?: return null
        val file = File(path)
        if (!file.exists()) {
            sessionPreferences.setPendingProfilePhotoPath(null)
            return null
        }
        return uploadProfilePhoto(file.readBytes())
    }

    suspend fun downloadProfilePhotoUrl(): String? {
        val pending = sessionPreferences.pendingProfilePhotoPath.first()
        if (!pending.isNullOrBlank() && File(pending).exists()) {
            return Uri.fromFile(File(pending)).toString()
        }
        val uid = firebaseAuth.currentUser?.uid ?: return null
        return runCatching {
            storage.reference.child("users/$uid/profile.jpg").downloadUrl.await().toString()
        }.getOrNull()
    }

    private suspend fun clearPendingLocalPhoto() {
        val path = sessionPreferences.pendingProfilePhotoPath.first()
        if (!path.isNullOrBlank()) {
            runCatching { File(path).delete() }
        }
        sessionPreferences.setPendingProfilePhotoPath(null)
    }

    private fun pendingFile(): File =
        File(context.filesDir, "pending_profile_photo.jpg")
}
